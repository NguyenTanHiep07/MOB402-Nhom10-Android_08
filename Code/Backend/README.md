# GoDrop Delivery Backend

Backend REST dùng chung cho ứng dụng Client, Delivery và Admin. Server sử dụng Spring Boot 3, Java 21, MongoDB (Spring Data MongoDB), JWT và Swagger/OpenAPI.

## Phạm vi đã triển khai

- Đăng nhập bằng JWT và phân quyền `CLIENT`, `DELIVERY`, `ADMIN`.
- Khách tự đăng ký bằng số điện thoại; endpoint công khai chỉ tạo tài khoản `CLIENT`.
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
- Lưu trữ trên MongoDB với bộ sinh ID `Long` tự tăng tuần tự đảm bảo tương thích 100% với Android app.
- Seeder thêm dữ liệu mẫu khi `DEMO_ENABLED=true`; mặc định tắt để bảo vệ database thật.

Auto Assignment và FCM là P1 nên chưa triển khai.

## Yêu cầu môi trường

- Java 21.
- MongoDB Atlas (Cloud) hoặc MongoDB local replica set qua Docker Compose trong thư mục này. Giao dịch đa tài liệu không chạy trên MongoDB standalone.

## Chạy local

Tại thư mục `Code/Backend`:

1. Chạy `./setup-local.sh` để tạo `.env` nếu chưa có, rồi kiểm tra `MONGODB_URI`. Script không sửa `.env` hiện hữu. Nếu dùng MongoDB Atlas, thay URI local bằng URI riêng của bạn:
   ```bash
   MONGODB_URI=mongodb+srv://<username>:<password>@cluster.mongodb.net/delivery_db?retryWrites=true&w=majority
   JWT_SECRET=your_32_characters_long_jwt_secret_key_here
   DEMO_ENABLED=false
   DEMO_PASSWORD=<mật khẩu mạnh, chỉ dùng khi bật demo>
   ```

2. Nếu dùng MongoDB local, khởi động và khởi tạo replica set một lần (không xóa volume/dữ liệu):
   ```bash
   docker compose up -d --wait
   docker compose exec mongodb mongosh --quiet --eval 'rs.initiate({_id:"rs0",members:[{_id:0,host:"localhost:27017"}]})'
   ```
   Nếu replica set đã khởi tạo, **không chạy lại** `rs.initiate`; kiểm tra bằng `docker compose exec mongodb mongosh --quiet --eval 'rs.status().ok'`. URI local cần có `?replicaSet=rs0`. Script `setup-local.sh` không sửa `.env` hiện hữu, nên nếu file đã có, tự kiểm tra URI mà không chia sẻ bí mật ra log. Nếu dùng Atlas, bỏ qua Docker. Khởi động server:
   ```bash
   ./gradlew bootRun
   # Windows: gradlew.bat bootRun
   ```

Swagger: `http://localhost:8080/swagger-ui.html`

OpenAPI JSON để import vào Postman: `http://localhost:8080/v3/api-docs`

Android Emulator dùng base URL: `http://10.0.2.2:8080/api/`. Điện thoại thật phải dùng địa chỉ IP LAN của máy chạy server và hai thiết bị phải cùng mạng. Swagger chỉ là công cụ thử API; không cần mở tab web để Android hoạt động.

## Tài khoản mẫu

Chỉ có tài khoản mẫu khi bật `DEMO_ENABLED=true` trên database demo riêng. Đăng nhập bằng **số điện thoại**, không phải username. Mật khẩu tài khoản seed mới lấy từ `DEMO_PASSWORD` trong `.env`; tài khoản đã có giữ mật khẩu cũ. Script không ghi đè `.env` và không reset tài khoản/database hiện có.

| Username nội bộ | Số điện thoại đăng nhập | Password | Role |
|---|---|---|
| `client1`…`client5` | `0123456789`, `0987654321`, `0903000003`, `0904000004`, `0905000005` | Theo `.env` khi seed mới | CLIENT |
| `shipper1`…`shipper4` | `0111222333`, `0444555666`, `0913000003`, `0914000004` | Theo `.env` khi seed mới | DELIVERY |
| `shipper5`…`shipper7` | `0915000005`, `0916000006`, `0917000007` | Theo `.env` khi seed mới | DELIVERY |
| `admin` | `0000000000` | Theo `.env` khi seed mới | ADMIN |

Seeder tạo 15 đơn mẫu có tọa độ quanh TP.HCM, gồm Open Pool, đơn đang giao ở nhiều trạng thái,
đơn đã giao và đơn đã hủy. Thời gian được phân bổ trong nhiều ngày để thử lịch sử/thu nhập.
Một số đơn có dữ liệu Reject để kiểm tra việc ẩn đơn theo từng tài xế, Reliability Score và khóa tạm thời.

Không bật seed demo trên database thật. MongoDB local từ Compose chỉ lắng nghe `127.0.0.1` và không có xác thực, chỉ dùng để phát triển. Dùng MongoDB Atlas hoặc máy chủ được bảo vệ và HTTPS nếu triển khai ngoài mạng nội bộ.

## Biến môi trường

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/delivery_db?replicaSet=rs0` | Connection URI tới MongoDB replica set/Atlas |
| `JWT_SECRET` | Bắt buộc | Khóa ký JWT, tối thiểu 32 ký tự |
| `DEMO_ENABLED` | `false` | Bật seed dữ liệu giả, chỉ trên database demo riêng |
| `DEMO_PASSWORD` | Bắt buộc khi bật seed | Mật khẩu cho tài khoản mẫu chưa tồn tại |
| `JWT_EXPIRATION_MS` | `86400000` | Thời hạn token, mặc định 24 giờ |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Browser origin được phép gọi API; Android native không cần CORS |
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

Chạy toàn bộ test backend từ thư mục `Code/Backend`:

```bash
./gradlew test
```

Bộ test P0 bao phủ phân quyền/JSON 401-403, hai tài xế cùng nhận một đơn (chỉ một người thắng),
Reject không làm mất đơn khỏi Open Pool chung, Reliability Score, quyền sở hữu khi cập nhật trạng thái
và quyền xem lịch sử đơn.

Lệnh trên chạy bộ test sẵn có, không tự xác nhận luồng thiết bị/backend/database thật. Kiểm tra tích hợp trên MongoDB kiểm thử riêng; không dùng database demo đang có đơn. Luồng hủy cho phép cả `DA_DEN_NHA_HANG`, nhưng chặn từ `DA_LAY_HANG` trở đi.

Các index unique mới của `ratings.deliveryRequestId` và cặp `order_rejections(deliveryRequestId, driverId)` có thể xung đột với index không-unique hoặc dữ liệu trùng của database cũ. Ứng dụng **không tự xóa index hoặc dữ liệu**. Hãy sao lưu, kiểm tra trùng và lên kế hoạch migration riêng trước khi dùng database đã có dữ liệu.
