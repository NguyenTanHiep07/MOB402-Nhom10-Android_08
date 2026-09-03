# GoDrop Delivery Backend

Backend REST dùng chung cho ứng dụng Client, Delivery và Admin. Server sử dụng Spring Boot 3, Java 21, PostgreSQL, Flyway, JWT và Swagger/OpenAPI.

## Phạm vi đã triển khai

- Đăng nhập bằng JWT và phân quyền `CLIENT`, `DELIVERY`, `ADMIN`.
- Client tạo, xem, hủy đơn của chính mình và xem lịch sử trạng thái.
- Delivery xem Open Pool/My Orders, nhận đơn atomic, từ chối theo lý do, cập nhật đúng chuỗi trạng thái.
- Reject không đổi trạng thái đơn; đơn chỉ bị ẩn với tài xế đã Reject và vẫn hiện cho tài xế khác.
- Lý do hợp lệ không trừ điểm. Lý do không hợp lệ trừ điểm Reliability Score.
- Ba lần Reject bị phạt trong 24 giờ sẽ khóa nhận đơn 30 phút.
- Admin xem người dùng, tài xế, đơn và danh sách cảnh báo Reliability Score.
- Client đánh giá tài xế sau khi giao thành công; mỗi đơn chỉ được đánh giá một lần.
- Khách sở hữu đơn, tài xế đã giao và Admin có thể xem đánh giá; API có thống kê sao trung bình theo tài xế.
- Client tìm địa chỉ thật trong Việt Nam và nhận ước lượng quãng đường chạy xe/thời gian/phí từ backend.
- Khi tạo đơn, backend tự tính lại quãng đường và phí, không tin số km do Android gửi lên.
- Flyway tự tạo schema; seeder chỉ thêm dữ liệu mẫu khi dữ liệu chưa tồn tại.

Auto Assignment và FCM là P1 nên chưa triển khai.

## Yêu cầu môi trường

- Java 21.
- Docker Desktop để chạy PostgreSQL, hoặc một PostgreSQL 16 đang hoạt động.

## Chạy local

Tại thư mục `Backend`:

```bash
cp .env.example .env
docker compose up -d
../Code/gradlew clean bootRun
```

Swagger: `http://localhost:8080/swagger-ui.html`

OpenAPI JSON để import vào Postman: `http://localhost:8080/v3/api-docs`

Android Emulator dùng base URL: `http://10.0.2.2:8080/`. Điện thoại thật phải dùng địa chỉ IP LAN của máy chạy server và hai thiết bị phải cùng mạng.

## Tài khoản mẫu

| Username | Password | Role |
|---|---|---|
| `client1` | `123456` | CLIENT |
| `client2` | `123456` | CLIENT |
| `client3` - `client5` | `123456` | CLIENT |
| `shipper1` - `shipper4` | `123456` | DELIVERY, `BUSY`, đang có đơn mẫu |
| `shipper5` | `123456` | DELIVERY, Reliability 60 và đang bị khóa nhận đơn mẫu |
| `shipper6` | `123456` | DELIVERY, `OFFLINE` |
| `shipper7` | `123456` | DELIVERY, `AVAILABLE`, dùng để thử Accept |
| `admin` | `123456` | ADMIN |

Seeder tạo 15 đơn mẫu có tọa độ quanh TP.HCM, gồm Open Pool, đơn đang giao ở nhiều trạng thái,
đơn đã giao và đơn đã hủy. Thời gian được phân bổ trong nhiều ngày để thử lịch sử/thu nhập.
Một số đơn có dữ liệu Reject để kiểm tra việc ẩn đơn theo từng tài xế, Reliability Score và khóa tạm thời.

Đổi `JWT_SECRET`, mật khẩu database và tài khoản mẫu trước khi dùng ngoài môi trường demo.

## Biến môi trường

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/delivery_db` | JDBC URL |
| `DB_USERNAME` | `delivery_user` | User PostgreSQL |
| `DB_PASSWORD` | `delivery_password` | Password PostgreSQL |
| `JWT_SECRET` | Chuỗi demo trong `application.yml` | Khóa ký JWT, tối thiểu 32 ký tự |
| `JWT_EXPIRATION_MS` | `86400000` | Thời hạn token, mặc định 24 giờ |
| `CORS_ALLOWED_ORIGINS` | `*` | Origin được phép gọi API |
| `SERVER_PORT` | `8080` | Port server |

Chi tiết endpoint xem tại [docs/API_CONTRACT.md](docs/API_CONTRACT.md) và Swagger.

## Kiểm thử

Chạy toàn bộ test backend từ thư mục `Backend`:

```bash
../Code/gradlew clean test
```

Bộ test P0 bao phủ phân quyền/JSON 401-403, hai tài xế cùng nhận một đơn (chỉ một người thắng),
Reject không làm mất đơn khỏi Open Pool chung, Reliability Score, quyền sở hữu khi cập nhật trạng thái
và quyền xem lịch sử đơn.
