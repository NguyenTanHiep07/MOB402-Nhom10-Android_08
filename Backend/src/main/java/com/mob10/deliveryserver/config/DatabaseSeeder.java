package com.mob10.deliveryserver.config;

import com.mob10.deliveryserver.domain.*;
import com.mob10.deliveryserver.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

@Component
public class DatabaseSeeder implements ApplicationRunner {
    private static final String[][] DEMO_ROUTES = {
            {"10.7768890", "106.7008060", "10.7826810", "106.6957540"},
            {"10.7814720", "106.6994600", "10.7756580", "106.6872960"},
            {"10.7797840", "106.6990180", "10.7751100", "106.6865810"},
            {"10.8273320", "106.6902650", "10.8382430", "106.6717570"},
            {"10.7721530", "106.6980430", "10.8014040", "106.7129500"},
            {"10.7528960", "106.6677690", "10.7556410", "106.6636500"},
            {"10.7826070", "106.6932660", "10.7707070", "106.7049860"},
            {"10.7740330", "106.7038020", "10.8017480", "106.7140730"},
            {"10.7877930", "106.6791970", "10.7558500", "106.6680430"},
            {"10.7713660", "106.7042150", "10.7959890", "106.7152400"},
            {"10.7546930", "106.6697540", "10.7517690", "106.6592490"},
            {"10.7737270", "106.6670060", "10.7709300", "106.6710840"},
            {"10.8015000", "106.6535740", "10.8039720", "106.6590430"},
            {"10.7971250", "106.6831460", "10.7992650", "106.6760780"},
            {"10.7903720", "106.6684030", "10.7999490", "106.7146710"}
    };

    private final UserRepository users;
    private final DeliveryRequestRepository orders;
    private final StatusHistoryRepository histories;
    private final RejectionReasonRepository reasons;
    private final OrderRejectionRepository rejections;
    private final DriverStatisticsRepository statistics;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository users, DeliveryRequestRepository orders, StatusHistoryRepository histories,
                          RejectionReasonRepository reasons, OrderRejectionRepository rejections,
                          DriverStatisticsRepository statistics, PasswordEncoder passwordEncoder) {
        this.users = users; this.orders = orders; this.histories = histories; this.reasons = reasons;
        this.rejections = rejections; this.statistics = statistics; this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User client1 = seedUser("client1", "Nguyễn Văn A", "0123456789", Role.CLIENT, null);
        User client2 = seedUser("client2", "Trần Thị B", "0987654321", Role.CLIENT, null);
        User client3 = seedUser("client3", "Hoàng Minh Anh", "0903000003", Role.CLIENT, null);
        User client4 = seedUser("client4", "Võ Thu Hà", "0904000004", Role.CLIENT, null);
        User client5 = seedUser("client5", "Đặng Quốc Bảo", "0905000005", Role.CLIENT, null);
        User shipper1 = seedUser("shipper1", "Lê Văn C", "0111222333", Role.DELIVERY, "29A-123.45");
        User shipper2 = seedUser("shipper2", "Phạm Văn D", "0444555666", Role.DELIVERY, "29B-678.90");
        User shipper3 = seedUser("shipper3", "Trần Minh Khoa", "0913000003", Role.DELIVERY, "51H-246.80");
        User shipper4 = seedUser("shipper4", "Ngô Thành Long", "0914000004", Role.DELIVERY, "59C-135.79");
        User shipper5 = seedUser("shipper5", "Bùi Anh Tuấn", "0915000005", Role.DELIVERY, "30G-112.23");
        User shipper6 = seedUser("shipper6", "Đỗ Quang Huy", "0916000006", Role.DELIVERY, "43D-445.56");
        User shipper7 = seedUser("shipper7", "Phan Nhật Minh", "0917000007", Role.DELIVERY, "50H-778.89");
        seedUser("admin", "Quản trị viên", "0000000000", Role.ADMIN, null);

        RejectionReason vehicleIssue = seedReason("VEHICLE_ISSUE", "Xe gặp sự cố", true, 0, true);
        seedReason("EMERGENCY", "Tình huống khẩn cấp", true, 0, true);
        seedReason("PACKAGE_UNSAFE", "Hàng hóa không an toàn hoặc sai quy định", true, 0, true);
        seedReason("TOO_FAR", "Khoảng cách quá xa", false, 10, false);
        RejectionReason busy = seedReason("BUSY", "Đang bận", false, 10, false);
        seedReason("LOW_FEE", "Phí giao hàng thấp", false, 10, false);
        seedReason("OTHER", "Lý do khác", false, 10, true);

        DriverStatistics stats1 = seedStatistics(shipper1);
        DriverStatistics stats2 = seedStatistics(shipper2);
        DriverStatistics stats3 = seedStatistics(shipper3);
        DriverStatistics stats4 = seedStatistics(shipper4);
        DriverStatistics stats5 = seedStatistics(shipper5);
        DriverStatistics stats6 = seedStatistics(shipper6);
        seedStatistics(shipper7);

        if (orders.count() == 0) {
            DeliveryRequest order1 = seedOrder(client1, "Nhà hàng ABC", "123 Nguyễn Văn A, Quận 1",
                    "Nguyễn Văn B", "456 Lê Văn B, Quận 3", "Cơm gà", "FOOD", "2.50", "0.50", 1,
                    false, false, "Nhiều cơm", 0, 0);
            DeliveryRequest order2 = seedOrder(client2, "Văn phòng Sao Việt", "18 Nguyễn Thị Minh Khai, Quận 1",
                    "Lê Thanh Mai", "91 Cách Mạng Tháng 8, Quận 3", "Hồ sơ hợp đồng", "DOCUMENT", "3.20", "0.30", 1,
                    false, true, "Giao trước 17 giờ", 1, 0);
            DeliveryRequest order3 = seedOrder(client3, "Hoa Tươi Mộc Lan", "52 Hai Bà Trưng, Quận 1",
                    "Phạm Ngọc Linh", "120 Võ Văn Tần, Quận 3", "Bó hoa sinh nhật", "FLOWER", "2.80", "1.00", 1,
                    true, false, "Không làm dập hoa", 2, 1);
            DeliveryRequest order4 = seedOrder(client4, "Siêu thị An Khang", "10 Phan Văn Trị, Gò Vấp",
                    "Vũ Hoàng Nam", "225 Quang Trung, Gò Vấp", "Thực phẩm khô", "GROCERY", "5.10", "4.00", 2,
                    false, false, null, 3, 2);
            seedOrder(client5, "TechZone", "65 Lê Lợi, Quận 1",
                    "Đỗ Minh Tâm", "42 Nguyễn Gia Trí, Bình Thạnh", "Bàn phím cơ", "ELECTRONIC", "6.40", "1.20", 1,
                    true, true, "Xin gọi trước khi giao", 4, 0);

            DeliveryRequest order6 = seedOrder(client1, "Bếp Nhà Mây", "88 Trần Hưng Đạo, Quận 5",
                    "Nguyễn Thảo Vy", "12 Hồng Bàng, Quận 5", "Bánh ngọt", "FOOD", "2.10", "1.50", 1,
                    true, false, null, 5, 0);
            accept(order6, shipper1, stats1);

            DeliveryRequest order7 = seedOrder(client2, "Phở Hòa", "260C Pasteur, Quận 3",
                    "Trần Thị C", "789 Lý Tự Trọng, Quận 1", "Phở bò", "FOOD", "4.00", "0.60", 2,
                    false, false, null, 6, 0);
            accept(order7, shipper2, stats2);
            advance(order7, shipper2, DeliveryStatus.DA_DEN_NHA_HANG, "Tài xế đã đến nhà hàng");

            DeliveryRequest order8 = seedOrder(client3, "Nhà sách Ánh Dương", "40 Nguyễn Huệ, Quận 1",
                    "Hoàng Gia Hân", "150 Điện Biên Phủ, Bình Thạnh", "Sách giáo trình", "BOOK", "4.70", "2.20", 1,
                    false, false, null, 7, 1);
            accept(order8, shipper3, stats3);
            advance(order8, shipper3, DeliveryStatus.DA_DEN_NHA_HANG, "Tài xế đã đến điểm lấy hàng");
            advance(order8, shipper3, DeliveryStatus.DA_LAY_HANG, "Tài xế đã lấy hàng");

            DeliveryRequest order9 = seedOrder(client4, "Cửa hàng Mẹ và Bé", "19 Lý Chính Thắng, Quận 3",
                    "Võ Minh Châu", "330 Nguyễn Trãi, Quận 5", "Tã em bé", "HOUSEHOLD", "5.30", "3.50", 2,
                    false, true, null, 8, 0);
            accept(order9, shipper4, stats4);
            advance(order9, shipper4, DeliveryStatus.DA_DEN_NHA_HANG, "Tài xế đã đến cửa hàng");
            advance(order9, shipper4, DeliveryStatus.DA_LAY_HANG, "Tài xế đã lấy hàng");
            advance(order9, shipper4, DeliveryStatus.DA_DEN_KHACH_HANG, "Tài xế đã đến địa chỉ người nhận");

            DeliveryRequest order10 = seedOrder(client5, "Cà phê Ban Mai", "25 Hồ Tùng Mậu, Quận 1",
                    "Đặng Hải Yến", "77 Nguyễn Cửu Vân, Bình Thạnh", "Cà phê rang xay", "BEVERAGE", "4.20", "1.00", 3,
                    false, false, null, 9, 1);
            deliver(order10, shipper1, stats1);

            DeliveryRequest order11 = seedOrder(client1, "Tiệm bánh An Nhiên", "62 Nguyễn Trãi, Quận 5",
                    "Lê Thu Trang", "11 Tản Đà, Quận 5", "Bánh kem", "FOOD", "2.60", "2.00", 1,
                    true, true, "Đã thanh toán", 10, 3);
            deliver(order11, shipper2, stats2);

            DeliveryRequest order12 = seedOrder(client2, "Nhà thuốc Bình An", "101 Thành Thái, Quận 10",
                    "Trần Văn Phúc", "56 Sư Vạn Hạnh, Quận 10", "Thực phẩm chức năng", "MEDICAL", "3.10", "0.80", 1,
                    false, false, null, 11, 6);
            deliver(order12, shipper3, stats3);

            DeliveryRequest order13 = seedOrder(client3, "Cửa hàng Nội Thất Xanh", "72 Cộng Hòa, Tân Bình",
                    "Nguyễn Quốc Hưng", "15 Út Tịch, Tân Bình", "Đèn bàn", "HOUSEHOLD", "3.80", "2.50", 1,
                    true, false, null, 12, 2);
            cancel(order13, client3, "Khách hàng đổi lịch và hủy đơn");

            DeliveryRequest order14 = seedOrder(client4, "Pet House", "45 Phan Đình Phùng, Phú Nhuận",
                    "Võ Thanh Tùng", "210 Hoàng Văn Thụ, Phú Nhuận", "Thức ăn thú cưng", "PET", "3.50", "5.00", 1,
                    false, false, null, 13, 1);
            accept(order14, shipper5, stats5);
            cancel(order14, client4, "Khách hàng hủy sau khi tài xế nhận");
            shipper5.setDriverAvailability(DriverAvailability.AVAILABLE);

            DeliveryRequest order15 = seedOrder(client5, "Shop Thời Trang Mộc", "90 Lê Văn Sỹ, Phú Nhuận",
                    "Bùi Khánh Ngân", "36 Trường Sa, Bình Thạnh", "Áo khoác", "FASHION", "4.90", "0.70", 1,
                    false, true, "Giao giờ hành chính", 14, 4);
            reject(order1, shipper5, busy, stats5, null);
            reject(order2, shipper5, busy, stats5, null);
            reject(order4, shipper5, busy, stats5, null);
            reject(order15, shipper5, busy, stats5, null);
            reject(order3, shipper6, vehicleIssue, stats6, "Xe đang bảo dưỡng");
            stats5.lockUntil(Instant.now().plus(Duration.ofMinutes(30)));
            shipper1.setDriverAvailability(DriverAvailability.BUSY);
            shipper2.setDriverAvailability(DriverAvailability.BUSY);
            shipper3.setDriverAvailability(DriverAvailability.BUSY);
            shipper4.setDriverAvailability(DriverAvailability.BUSY);
            shipper5.setDriverAvailability(DriverAvailability.AVAILABLE);
            shipper6.setDriverAvailability(DriverAvailability.OFFLINE);

        }
    }

    private User seedUser(String username, String fullName, String phone, Role role, String plate) {
        return users.findByUsername(username).orElseGet(() -> users.save(
                new User(username, passwordEncoder.encode("123456"), fullName, phone, role, plate)));
    }

    private RejectionReason seedReason(String code, String label, boolean valid, int points, boolean noteRequired) {
        return reasons.findById(code).orElseGet(() ->
                reasons.save(new RejectionReason(code, label, valid, points, noteRequired)));
    }

    private DriverStatistics seedStatistics(User driver) {
        return statistics.findById(driver.getId()).orElseGet(() -> statistics.save(new DriverStatistics(driver)));
    }

    private DeliveryRequest seedOrder(User client, String sender, String pickup, String recipient,
                                      String destination, String packageName, String packageType,
                                      String distanceValue, String weightValue, int quantity,
                                      boolean fragile, boolean express, String note,
                                      int routeIndex, int daysAgo) {
        String[] route = DEMO_ROUTES[routeIndex];
        Instant createdAt = Instant.now().minus(Duration.ofDays(daysAgo)).minus(Duration.ofHours(6));
        BigDecimal distance = money(new BigDecimal(distanceValue));
        BigDecimal weight = money(new BigDecimal(weightValue));
        DeliveryRequest order = new DeliveryRequest(client, distance, pickup, destination,
                coordinate(route[0]), coordinate(route[1]), coordinate(route[2]), coordinate(route[3]),
                sender, "0901234567", recipient, "0987654321", note, null, createdAt);
        order.addPackage(new PackageItem(packageName, packageType, weight, quantity, note, fragile, express));

        BigDecimal baseFee = money(new BigDecimal("15000"));
        BigDecimal distanceFee = money(distance.multiply(new BigDecimal("5000")));
        BigDecimal weightFee = money(weight.multiply(BigDecimal.valueOf(quantity)).multiply(new BigDecimal("3000")));
        BigDecimal optionalFee = BigDecimal.ZERO;
        if (fragile) optionalFee = optionalFee.add(new BigDecimal("5000"));
        if (express) optionalFee = optionalFee.add(new BigDecimal("10000"));
        optionalFee = money(optionalFee);
        order.applyFees(baseFee, distanceFee, weightFee, optionalFee,
                money(baseFee.add(distanceFee).add(weightFee).add(optionalFee)));
        orders.save(order);
        histories.save(new StatusHistory(order, null, DeliveryStatus.CHO_TIEP_NHAN, client,
                "Đơn hàng được tạo", createdAt));
        return order;
    }

    private void accept(DeliveryRequest order, User driver, DriverStatistics driverStats) {
        Instant occurredAt = order.getUpdatedAt().plus(Duration.ofMinutes(20));
        order.assignDriver(driver, occurredAt);
        driver.setDriverAvailability(DriverAvailability.BUSY);
        driverStats.recordAcceptance();
        histories.save(new StatusHistory(order, DeliveryStatus.CHO_TIEP_NHAN, DeliveryStatus.DA_CHAP_NHAN,
                driver, "Tài xế đã nhận đơn", occurredAt));
    }

    private void advance(DeliveryRequest order, User driver, DeliveryStatus nextStatus, String note) {
        DeliveryStatus previous = order.getStatus();
        Instant occurredAt = order.getUpdatedAt().plus(Duration.ofMinutes(30));
        order.changeStatus(nextStatus, occurredAt);
        histories.save(new StatusHistory(order, previous, nextStatus, driver, note, occurredAt));
    }

    private void deliver(DeliveryRequest order, User driver, DriverStatistics driverStats) {
        accept(order, driver, driverStats);
        advance(order, driver, DeliveryStatus.DA_DEN_NHA_HANG, "Tài xế đã đến điểm lấy hàng");
        advance(order, driver, DeliveryStatus.DA_LAY_HANG, "Tài xế đã lấy hàng");
        advance(order, driver, DeliveryStatus.DA_DEN_KHACH_HANG, "Tài xế đã đến địa chỉ người nhận");
        advance(order, driver, DeliveryStatus.DA_GIAO, "Đơn hàng đã giao thành công");
        driver.setDriverAvailability(DriverAvailability.AVAILABLE);
    }

    private void cancel(DeliveryRequest order, User client, String note) {
        DeliveryStatus previous = order.getStatus();
        Instant occurredAt = order.getUpdatedAt().plus(Duration.ofMinutes(10));
        order.changeStatus(DeliveryStatus.DA_HUY, occurredAt);
        histories.save(new StatusHistory(order, previous, DeliveryStatus.DA_HUY, client, note, occurredAt));
    }

    private void reject(DeliveryRequest order, User driver, RejectionReason reason,
                        DriverStatistics driverStats, String note) {
        Instant rejectedAt = order.getCreatedAt().plus(Duration.ofMinutes(15));
        OrderRejection rejection = rejections.save(new OrderRejection(order, driver, reason, note, rejectedAt));
        driverStats.recordRejection(reason.getPenaltyPoints(), rejection.isPenaltyApplied());
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal coordinate(String value) {
        return new BigDecimal(value).setScale(7, RoundingMode.HALF_UP);
    }
}
