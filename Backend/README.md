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
./setup-local.sh
docker compose up -d
../Code/gradlew bootRun
```

Swagger: `http://localhost:8080/swagger-ui.html`

OpenAPI JSON để import vào Postman: `http://localhost:8080/v3/api-docs`

Android Emulator dùng base URL: `http://10.0.2.2:8080/api/`. Điện thoại thật phải dùng địa chỉ IP LAN của máy chạy server và hai thiết bị phải cùng mạng. Swagger chỉ là công cụ thử API; không cần mở tab web để Android hoạt động.

## Tài khoản mẫu

Mật khẩu của tài khoản seed **mới** lấy từ `DEMO_PASSWORD` trong `.env`; tài khoản đã có giữ mật khẩu cũ. Script không ghi đè `.env` và không reset tài khoản/database hiện có.

| Username | Password | Role |
|---|---|---|
| `client1` - `client5` | Theo `.env` khi seed mới | CLIENT |
| `shipper1` - `shipper4` | Theo `.env` khi seed mới | DELIVERY, `BUSY`, đang có đơn mẫu |
| `shipper5` | Theo `.env` khi seed mới | DELIVERY, Reliability 60 và đang bị khóa nhận đơn mẫu |
| `shipper6` | Theo `.env` khi seed mới | DELIVERY, `OFFLINE` |
| `shipper7` | Theo `.env` khi seed mới | DELIVERY, `AVAILABLE`, dùng để thử Accept |
| `admin` | Theo `.env` khi seed mới | ADMIN |

Seeder tạo 15 đơn mẫu có tọa độ quanh TP.HCM, gồm Open Pool, đơn đang giao ở nhiều trạng thái,
đơn đã giao và đơn đã hủy. Thời gian được phân bổ trong nhiều ngày để thử lịch sử/thu nhập.
Một số đơn có dữ liệu Reject để kiểm tra việc ẩn đơn theo từng tài xế, Reliability Score và khóa tạm thời.

Đổi `JWT_SECRET`, mật khẩu database và tài khoản mẫu trước khi dùng ngoài môi trường demo.

## Biến môi trường

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/delivery_db` | JDBC URL |
| `DB_USERNAME` | `delivery_user` | User PostgreSQL |
| `DB_PASSWORD` | Lấy từ `POSTGRES_PASSWORD` nếu không đặt riêng | Password PostgreSQL |
| `JWT_SECRET` | Bắt buộc, script tự sinh | Khóa ký JWT, tối thiểu 32 ký tự |
| `DEMO_ENABLED` | `false` | Bật seed dữ liệu giả cho demo; setup-local đặt true |
| `DEMO_PASSWORD` | Bắt buộc khi bật seed | Mật khẩu cho tài khoản mẫu chưa tồn tại |
| `JWT_EXPIRATION_MS` | `86400000` | Thời hạn token, mặc định 24 giờ |
| `CORS_ALLOWED_ORIGINS` | `*` | Origin được phép gọi API |
| `SERVER_PORT` | `8080` | Port server |
| `PHOTON_BASE_URL` | `https://photon.komoot.io` | Provider autocomplete địa chỉ OpenStreetMap |
| `OSRM_BASE_URL` | `https://router.project-osrm.org` | Provider tính tuyến đường chạy xe |
| `LOCATION_REQUEST_TIMEOUT_MS` | `8000` | Timeout gọi provider bản đồ |
| `LOCATION_USER_AGENT` | `GoDrop-UTH-08/1.0-student-project` | User-Agent nhận diện project khi gọi provider |

Các URL mặc định là public demo server, phù hợp bài tập và demo lưu lượng thấp nhưng không có cam kết uptime.
Nếu triển khai thực tế, cấu hình provider riêng hoặc dịch vụ bản đồ có SLA. Android chỉ gọi GoDrop backend,
không phụ thuộc trực tiếp vào provider bên ngoài.

Chi tiết endpoint xem tại [docs/API_CONTRACT.md](docs/API_CONTRACT.md) và Swagger.

## Dữ liệu demo đa trạng thái

Khi `app.demo.enabled=true`, `DatabaseSeeder` giữ dữ liệu đang có và thêm đúng một lần lô 20 đơn được đánh dấu `Lô dữ liệu demo đa trạng thái 04/09`. Dữ liệu có Pending, Accepted, At Pickup, Picked Up, In Transit, At Customer, Delivered và Cancelled; các đơn hoàn tất được phân bố trên 7 shipper. `shipper7` có bốn lần từ chối bị phạt trong lô, còn 60 điểm và bị khóa tạm thời để Admin có cảnh báo thật. Seeder kiểm tra marker nên restart backend không tạo thêm lô thứ hai.

## Kiểm thử

Chạy toàn bộ test backend từ thư mục `Backend`:

```bash
../Code/gradlew clean test
```

Bộ test P0 bao phủ phân quyền/JSON 401-403, hai tài xế cùng nhận một đơn (chỉ một người thắng),
Reject không làm mất đơn khỏi Open Pool chung, Reliability Score, quyền sở hữu khi cập nhật trạng thái
và quyền xem lịch sử đơn.

Đợt rà soát 04/09 chạy thêm ca đồng thời trên PostgreSQL thật trong môi trường tạm, không dùng database demo. Script và source kiểm tra bổ sung được đặt ngoài repository theo yêu cầu, không có `run-db-tests.sh` đi kèm. Lệnh trên chỉ chạy bộ test gốc của nhóm. Bản 04/09 bổ sung `DANG_VAN_CHUYEN` sau `DA_LAY_HANG`, Flyway V4 nâng constraint giữ dữ liệu; cần restart backend và dùng Android cùng bản. Luồng hủy cho phép cả `DA_DEN_NHA_HANG`, nhưng chặn từ `DA_LAY_HANG` trở đi.
