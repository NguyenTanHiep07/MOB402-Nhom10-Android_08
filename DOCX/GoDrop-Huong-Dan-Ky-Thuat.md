# GoDrop - Hướng Dẫn Kỹ Thuật Tổng Hợp

Tài liệu này tổng hợp cấu trúc, kiến trúc và các API của hệ thống GoDrop, bao gồm Android App, Backend Server và cơ sở dữ liệu MongoDB.

## 1. Tổng quan kiến trúc Android – Backend – MongoDB

- **Android App:** Xây dựng bằng Kotlin và Jetpack Compose. Sử dụng MVI/MVVM architecture với các luồng riêng biệt: Client, Driver, Admin.
- **Backend:** Spring Boot (Java), phân phối REST API. Bảo mật bằng JWT Authentication.
- **Cơ sở dữ liệu:** MongoDB, lưu trữ dưới dạng Document với các relations tham chiếu theo ID hoặc Embedded.

## 2. Cài đặt và chạy dự án

### Backend
Yêu cầu: Java 21, MongoDB đang chạy ở `localhost:27017`.
```bash
cd Backend
./gradlew bootRun
```
Ứng dụng sẽ chạy tại `http://localhost:8080`.

### Android App
- Mở thư mục `Code` bằng Android Studio.
- Sync Gradle và chạy trên Emulator hoặc thiết bị thật (API >= 26).

## 3. Đăng nhập, JWT và phân quyền

- Backend cung cấp API `/api/auth/login` (cho cả user thường, tài xế và admin).
- JWT token được trả về có thời hạn 30 ngày (cấu hình trong `application.properties`).
- Quyền truy cập: Các endpoint được phân cấp bảo mật `@PreAuthorize` theo `role` (ví dụ `ROLE_CUSTOMER`, `ROLE_DRIVER`, `ROLE_ADMIN`).

## 4. API tài khoản và chỉnh sửa hồ sơ

- **Cập nhật hồ sơ:** `PUT /api/auth/profile`
- **Đổi mật khẩu:** `PUT /api/auth/change-password`
- Dữ liệu gửi lên là JSON. Server sẽ kiểm tra trùng lặp email/SĐT khi cập nhật. 

## 5. Email xác minh, OTP và khôi phục mật khẩu

- **Gửi OTP Email:** `POST /api/auth/forgot-password` (Hệ thống tạo mã OTP 6 số, hiệu lực 15 phút, giới hạn số lần gửi bằng bảng `password_recovery_limits`).
- **Xác nhận OTP:** `POST /api/auth/verify-reset-otp`
- **Đặt lại mật khẩu:** `POST /api/auth/reset-password`

## 6. API tạo, nhận và theo dõi đơn

- **Tạo đơn mới:** `POST /api/delivery/requests`
- **Tài xế nhận đơn:** `POST /api/delivery/requests/{id}/accept`
  - *Lưu ý kỹ thuật:* Hiện tại quá trình nhận đơn sử dụng logic `findById` và cập nhật thông thường, chưa áp dụng cơ chế khóa atomic (pessimistic locking), do đó có thể xảy ra race-condition nếu nhiều tài xế cùng ấn nhận 1 đơn.
- **Theo dõi đơn:** `GET /api/delivery/requests/{id}`
- **Cập nhật trạng thái (Pickup, Delivered...):** `PUT /api/delivery/requests/{id}/status`
- Giao hàng có kèm ảnh (Proof of delivery) được xử lý thông qua việc truyền URL hoặc file đính kèm tuỳ logic controller hiện tại.

## 7. API Admin và đánh giá tài xế

- **Admin Dashboard:** Các API tổng hợp doanh thu, lượng người dùng, đơn hàng (`/api/admin/...`).
- **Đánh giá tài xế (Rating):** `POST /api/delivery/requests/{id}/ratings`
  - Khách hàng có thể chấm điểm tài xế (1-5 sao) sau khi đơn hoàn thành. 

## 8. MongoDB collections và index

Các Collections chính:
- `users`: Tài khoản, role. (Index unique `username`, `phoneNumber`).
- `delivery_requests`: Đơn hàng, toạ độ, danh sách kiện hàng `packages` (embedded). (Index non-unique trên `status`, `createdAt`).
- `status_histories`: Lịch sử trạng thái đơn.
- `order_rejections`: Đơn bị tài xế từ chối. *(Lưu ý: chưa có unique index ép ràng buộc 1 cặp đơn-tài xế)*.
- `ratings`: Đánh giá. *(Lưu ý: chưa có unique index chống đánh giá nhiều lần ở tầng DB, phải xử lý logic)*.
- `driver_statistics`: Chỉ số Reliability.
- `password_recovery_limits`: Quản lý giới hạn số lần xin OTP theo email/IP.

## 9. Bản đồ, tìm địa chỉ và tính phí

- Ứng dụng Android tích hợp Google Maps Compose để hiển thị toạ độ.
- Tính phí được thực hiện tự động dựa trên khoảng cách (haversine formula) và biểu phí định nghĩa ở server.

## 10. Tài khoản demo, kiểm thử và xử lý lỗi

- **Demo Admin:** `admin` / `admin`
- **Demo Customer:** `customer` / `password`
- **Demo Driver:** `driver` / `password`
- Các lỗi trả về cấu trúc thống nhất: `{ "timestamp": "...", "status": 4xx, "error": "...", "message": "..." }`.
