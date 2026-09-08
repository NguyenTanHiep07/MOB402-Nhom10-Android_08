package com.mob10.deliveryserver.controller;

import com.mob10.deliveryserver.domain.*;
import com.mob10.deliveryserver.dto.OrderDtos.*;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.DeliveryRequestRepository;
import com.mob10.deliveryserver.security.AuthenticatedUser;
import com.mob10.deliveryserver.service.DriverOrderService;
import com.mob10.deliveryserver.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api")
public class DeliveryPhotoController {
    private final DeliveryRequestRepository orders;
    private final DriverOrderService driver;
    private final OrderService access;
    private final com.mob10.deliveryserver.repository.UserRepository users;
    public DeliveryPhotoController(DeliveryRequestRepository orders, DriverOrderService driver, OrderService access, com.mob10.deliveryserver.repository.UserRepository users) {
        this.orders = orders; this.driver = driver; this.access = access; this.users = users;
    }
    public record PhotoRequest(@NotBlank @Size(max = 700000) String image) {}
    public record PhotoResponse(String image) {}

    @GetMapping("/orders/{id}/driver-avatar")
    public PhotoResponse driverAvatar(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        var order = access.detail(user, id);
        if (order.deliveryPerson() == null) return new PhotoResponse(null);
        return new PhotoResponse(users.findById(order.deliveryPerson().id()).map(User::getAvatarBase64).orElse(null));
    }

    @GetMapping("/orders/{id}/delivery-photo")
    @Transactional(readOnly = true)
    public PhotoResponse photo(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        access.detail(user, id); // Apply the same client/assigned-driver/admin ownership rules.
        return new PhotoResponse(orders.findById(id).orElseThrow().getDeliveryPhoto());
    }

    @PostMapping("/driver/orders/{id}/complete-with-photo")
    @Transactional
    public OrderResponse complete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
                                  @Valid @RequestBody PhotoRequest input) {
        if (user.role() != Role.DELIVERY) throw new ApiException(HttpStatus.FORBIDDEN, "NOT_A_DRIVER", "Chức năng chỉ dành cho tài xế");
        var order = orders.findByIdForUpdate(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));

        // Kiểm tra ownership trực tiếp từ locked order (không gọi access.detail lần 2 để tránh conflict)
        if (order.getDeliveryPerson() == null || !order.getDeliveryPerson().getId().equals(user.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_ASSIGNED_DRIVER", "Chỉ tài xế đang phụ trách mới có thể xác nhận giao hàng");
        }

        // Idempotent retry: nếu đã giao thành công rồi, trả về response hiện tại
        if (order.getStatus() == DeliveryStatus.DA_GIAO && order.getDeliveryPhoto() != null) {
            return access.detail(user, id);
        }

        // Validate order phải ở trạng thái DA_DEN_KHACH_HANG mới được xác nhận giao
        if (order.getStatus() != DeliveryStatus.DA_DEN_KHACH_HANG) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_STATUS_FOR_PHOTO",
                "Chỉ có thể xác nhận giao hàng khi đã đến điểm giao (trạng thái hiện tại: " + order.getStatus() + ")");
        }

        String normalized;
        try {
            byte[] bytes = Base64.getDecoder().decode(input.image());
            if (bytes.length > 500000) throw new IllegalArgumentException("Ảnh quá lớn");
            try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                if (stream == null) throw new IllegalArgumentException("Không đọc được stream ảnh");
                var readers = ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) throw new IllegalArgumentException("Không có image reader");
                var reader = readers.next();
                try {
                    reader.setInput(stream);
                    String format = reader.getFormatName();
                    if (!format.equalsIgnoreCase("JPEG") && !format.equalsIgnoreCase("JPG")) {
                        throw new IllegalArgumentException("Ảnh không phải JPEG: " + format);
                    }
                    if (reader.getWidth(0) > 1600 || reader.getHeight(0) > 1600) {
                        throw new IllegalArgumentException("Ảnh quá lớn (max 1600x1600)");
                    }
                    var image = reader.read(0);
                    var out = new java.io.ByteArrayOutputStream();
                    // ImageIO.write có thể trả về false trong môi trường headless server
                    // nếu không có JPEG writer registered → fallback dùng byte gốc đã validate
                    boolean written = ImageIO.write(image, "jpeg", out);
                    if (!written || out.size() == 0) {
                        // Fallback: dùng trực tiếp byte gốc (đã validate JPEG hợp lệ ở trên)
                        normalized = Base64.getEncoder().encodeToString(bytes);
                    } else {
                        if (out.size() > 700000) throw new IllegalArgumentException("Ảnh sau xử lý quá lớn");
                        normalized = Base64.getEncoder().encodeToString(out.toByteArray());
                    }
                } finally { reader.dispose(); }
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHOTO", "Ảnh không hợp lệ hoặc quá lớn. Hãy chụp lại.");
        }

        order.setDeliveryPhoto(normalized);
        orders.save(order); // Persist photo first so updateStatus can verify it's set.
        return driver.updateStatus(user, id, new UpdateStatusRequest(DeliveryStatus.DA_GIAO, "Đã giao hàng, có ảnh xác nhận"));
    }
}
