# MongoDB data model

Backend hiện dùng Spring Data MongoDB, không dùng PostgreSQL, JPA hoặc Flyway. `MONGODB_URI` chọn database đích; không chạy kiểm thử tạo/hủy đơn trên database demo đang sử dụng.

| Collection | Nội dung |
|---|---|
| `users` | Tài khoản, vai trò, số điện thoại, trạng thái tài xế và hồ sơ |
| `delivery_requests` | Đơn, hai địa chỉ/tọa độ, giá, trạng thái, kiện hàng nhúng và ảnh xác nhận |
| `status_histories` | Lịch sử chuyển trạng thái |
| `rejection_reasons` | Danh mục lý do từ chối |
| `order_rejections` | Lần từ chối của tài xế đối với đơn |
| `driver_statistics` | Số liệu nhận/từ chối, điểm tin cậy và khóa tạm |
| `ratings` | Đánh giá sau giao |
| `account_challenges` | Xác minh email/khôi phục tài khoản |
| `password_recovery_limits` | Giới hạn yêu cầu khôi phục |
| `database_sequences` | Bộ đếm ID `Long` của các collection chính |

`User` và `DeliveryRequest` dùng ID `Long`; `DeliveryRequest` có trường `@Version` cho optimistic locking. Quan hệ đến user/đơn sử dụng `@DocumentReference` và một số trường ID riêng để truy vấn. Kiện hàng được nhúng trong đơn. Giá dùng `BigDecimal`; thời gian là `Instant` theo UTC.

`MongoIndexConfig` tạo index cần thiết khi ứng dụng khởi động, gồm unique index cho `users.username`, `users.phoneNumber`, `ratings.deliveryRequestId` và cặp `order_rejections(deliveryRequestId, driverId)`. Ứng dụng không tự xóa index cũ. Nếu database cũ có index không tương thích hoặc dữ liệu trùng, cần kiểm tra và lập kế hoạch migration/sao lưu riêng trước khi sửa index.

MongoDB không tự cung cấp khóa ngoại hoặc cascade như SQL. Các thao tác ghi nhiều collection dùng MongoDB transaction, nên server phải nối tới replica set/Atlas; `findByIdForUpdate` của repository chỉ đọc theo ID, không phải khóa SQL. Nhận đơn dùng cập nhật có điều kiện trên trạng thái tài xế, `DeliveryRequest` dùng optimistic locking, còn unique index là tuyến bảo vệ cuối cho đánh giá/từ chối trùng. Cần kiểm thử tích hợp riêng trên replica set và thiết bị trước nghiệm thu.
