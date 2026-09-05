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
    private final org.springframework.jdbc.core.JdbcTemplate db;
    public DeliveryPhotoController(DeliveryRequestRepository orders, DriverOrderService driver, OrderService access, org.springframework.jdbc.core.JdbcTemplate db) {
        this.orders = orders; this.driver = driver; this.access = access; this.db = db;
    }
    public record PhotoRequest(@NotBlank @Size(max = 700000) String image) {}
    public record PhotoResponse(String image) {}

    @GetMapping("/orders/{id}/driver-avatar")
    @Transactional(readOnly = true)
    public PhotoResponse driverAvatar(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        var order = access.detail(user, id);
        if (order.deliveryPerson() == null) return new PhotoResponse(null);
        return new PhotoResponse(db.queryForObject("SELECT avatar_base64 FROM users WHERE id=?", String.class, order.deliveryPerson().id()));
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
        access.detail(user, id);
        String normalized;
        try {
            byte[] bytes = Base64.getDecoder().decode(input.image());
            if (bytes.length > 500000) throw new IllegalArgumentException();
            try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers = ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) throw new IllegalArgumentException();
                var reader = readers.next();
                try {
                    reader.setInput(stream);
                    if (!reader.getFormatName().equalsIgnoreCase("JPEG") || reader.getWidth(0) > 1600 || reader.getHeight(0) > 1600) throw new IllegalArgumentException();
                    var image = reader.read(0);
                    var out = new java.io.ByteArrayOutputStream();
                    ImageIO.write(image, "jpg", out); // Remove metadata and retain only decoded image content.
                    if (out.size() > 700000) throw new IllegalArgumentException();
                    normalized = Base64.getEncoder().encodeToString(out.toByteArray());
                } finally { reader.dispose(); }
            }
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHOTO", "Ảnh không hợp lệ hoặc quá lớn. Hãy chụp lại.");
        }
        // A retry after a lost response must not complete the order a second time.
        if (order.getStatus() == DeliveryStatus.DA_GIAO && order.getDeliveryPhoto() != null &&
            order.getDeliveryPerson().getId().equals(user.id())) return access.detail(user, id);
        order.setDeliveryPhoto(normalized);
        return driver.updateStatus(user, id, new UpdateStatusRequest(DeliveryStatus.DA_GIAO, "Đã giao hàng, có ảnh xác nhận"));
    }
}
