# Android_UTH_08 — GoDrop Delivery App

Hướng dẫn mới: [Cấu hình Gmail, khôi phục mật khẩu và sửa hồ sơ/ảnh đại diện](Backend/docs/EMAIL_ACCOUNT_GUIDE.md).

MOB402 · Nhóm 10. Ứng dụng **Android native bằng Kotlin + Android SDK**, Compose Material 3 và màn đăng nhập XML. Backend Java 21/Spring Boot và PostgreSQL là nguồn dữ liệu chung cho khách hàng, tài xế và admin. Không có Python trong phần triển khai sản phẩm.

## Thành viên

| Thành viên | MSSV | Phụ trách theo phân công tuần 4 mới |
|---|---|---|
| Nguyễn Tấn Hiệp | 087205010642 | Backend, tích hợp và nghiệm thu |
| Nguyễn Quốc Thịnh | 052206007772 | REST API/DTO/repository, luồng dữ liệu Android |
| Huỳnh Nhật Nam | 080206015277 | Shipper, Maps/GPS; trước đó Room/ERD |
| Nguyễn Lâm Hữu Hùng | 079205019508 | Client, Admin, Rating |

Đánh giá công việc theo yêu cầu, không suy ra mức đóng góp cá nhân chỉ từ số commit. Mỗi thành viên dùng tài khoản Git cá nhân và commit message mô tả rõ thay đổi; không dùng “update”, “fix”, “final” đơn lẻ.

## Chức năng và phạm vi

- **Client:** đăng nhập, chọn địa chỉ từ gợi ý thật, nhập liên hệ/kiện hàng, xem khoảng cách và phí server, xác nhận tạo đơn, hủy trước lấy hàng, xem đơn/trạng thái/lịch sử, nhận thông báo trong app khi trạng thái đổi và đánh giá sau giao.
- **Delivery:** nhận/từ chối đơn trong danh sách đơn chờ, nhận thông báo khi có đơn mới, gọi nhanh người gửi/người nhận qua màn quay số, cập nhật đúng thứ tự, xem lịch sử/thu nhập/điểm tin cậy, đổi trạng thái làm việc, bản đồ hai điểm và định vị theo yêu cầu khi đã cấu hình Maps.
- **Admin (mở rộng):** xem tổng quan, tìm/lọc đơn theo mã, trạng thái, khách hàng hoặc shipper; xem chi tiết, người dùng, tài xế và cảnh báo. Các màn admin chỉ đọc; không có sửa/xóa tài khoản, sửa giá hoặc phân công lại.
- Dữ liệu mẫu được seed riêng ở backend khi bật `DEMO_ENABLED`; Android không tự sinh tài khoản/đơn giả. Mọi vai trò gọi cùng backend/database. Room lưu hồ sơ tài khoản cục bộ; các repository Room cũ còn được giữ để kiểm thử/mốc tuần trước, không phải nguồn đơn hàng của luồng REST đang dùng.

## Môi trường

- Android Studio hỗ trợ Android Gradle Plugin của project, JDK **21** để chạy đồng thời Android/backend; bytecode Android target Java 11.
- Android SDK compile/target **36**, min **24**; máy ảo hoặc thiết bị thật.
- Docker Desktop chạy PostgreSQL 16; Internet để tra địa chỉ/định tuyến Photon/OSRM.
- Maps trong app cần Google Play Services và Google Maps API key của nhóm. Không cần key để đăng nhập/xem đơn hoặc mở chỉ đường bằng ứng dụng ngoài.

## Chạy backend và Android

Tại thư mục repository:

```bash
cd Backend
./setup-local.sh
docker compose up -d
../Code/gradlew bootRun
```

`setup-local.sh` tạo `.env` bị Git bỏ qua, sinh JWT secret/mật khẩu mới. Nếu container PostgreSQL của dự án đã tồn tại, script giữ mật khẩu database đó. **Không đổi mật khẩu của các tài khoản đã có.** Tài khoản seed mới dùng `DEMO_PASSWORD` trong `Backend/.env`; các tài khoản demo cũ tiếp tục dùng mật khẩu đã được tạo trước đó. Không đưa `.env` hoặc token vào commit/ảnh minh chứng.

Khi `DEMO_ENABLED=true`, backend thêm đúng một lần lô 20 đơn demo đa trạng thái, không xóa đơn hiện có và không nhân bản sau mỗi lần khởi động. Lô này phủ đủ 8 trạng thái, chia đơn hoàn tất cho cả 7 shipper, giữ tối đa một đơn hoạt động cho mỗi shipper và tạo tình huống `shipper7` từ chối nhiều lần: 60 điểm tin cậy, bị khóa tạm thời và xuất hiện trong tab Cảnh báo của Admin.

Mở **Code** bằng Android Studio, Sync và Run `app`. Backend phải đang chạy. Địa chỉ mặc định máy ảo là `http://10.0.2.2:8080/api/`. Máy thật cần cùng mạng với máy chạy backend:

```bash
cd Code
./gradlew :app:assembleDebug -PAPI_BASE_URL=http://DIA_CHI_IP_LAN:8080/api/
```

Bản debug cho phép HTTP để demo LAN. Bản release chặn HTTP; cấu hình API HTTPS trước khi phát hành.

Tài khoản demo: `client1`…`client5`, `shipper1`…`shipper7`, `admin`. Đăng nhập bằng **username**, không dùng số điện thoại thay username. Đổi vai trò bằng Đăng xuất rồi đăng nhập tài khoản tương ứng.

**Swagger có tác dụng gì?** `http://localhost:8080/swagger-ui/index.html` là tài liệu và công cụ thử API. Không phải website bắt buộc mở để Android chạy. Docker ở cấu hình này chạy database; lệnh `bootRun` chạy backend. Android gọi backend trực tiếp dù đã đóng tab Swagger.

Nếu muốn thử Swagger: mở `POST /api/auth/login` → Try it out → nhập username/password → Execute → lấy `accessToken` trong **Response body** mã 200 → Authorize (HTTP Bearer: dán chuỗi token, không thêm dấu ngoặc kép) → gọi API theo quyền của tài khoản.

## Chỉ đường với Google Maps

GoDrop mở Google Maps bằng [Maps URLs](https://developers.google.com/maps/documentation/urls/get-started), không cần API key, Maps SDK hay Google Cloud billing. Nếu chưa cài Google Maps, ứng dụng mở liên kết chỉ đường trên trình duyệt.

Demo: đăng nhập tài xế → Đang giao → chọn đơn → Chỉ đường đến điểm lấy. Sau khi lấy hàng, nút đổi thành Chỉ đường đến điểm giao. Đích đến ưu tiên tọa độ hợp lệ, nếu thiếu dùng địa chỉ. Google Maps tự xử lý vị trí hiện tại và quyền định vị; nếu chưa có vị trí, nhập điểm xuất phát trên Maps. Quay lại GoDrop để cập nhật trạng thái. Không có bản đồ nhúng hoặc theo dõi GPS trực tiếp trong GoDrop.

## Kiến trúc, storage và luồng

### Trải nghiệm giao hàng

- Trang chủ tài xế ưu tiên thẻ chuyến hiện tại với địa chỉ cần đến, phí giao, nút chỉ đường/liên hệ và mở tiến trình. Khi thao tác đang gửi, các nút cập nhật bị khóa và có trạng thái xử lý.
- Khách hàng mở chi tiết đơn để xem hành trình từng bước, thời gian thực tế từ lịch sử và ảnh đại diện tài xế (chữ viết tắt khi chưa có ảnh). Đơn hủy hiển thị riêng, không giả lập đã giao.
- Tài xế đến điểm giao → chụp ảnh kiện hàng bằng ứng dụng máy ảnh → xem lại → xác nhận giao. App dùng FileProvider trong vùng lưu trữ riêng, nén JPEG và gửi qua `POST /api/driver/orders/{id}/complete-with-photo`. Backend kiểm tra ảnh, quyền tài xế, trạng thái hợp lệ rồi lưu ảnh và trạng thái trong cùng transaction. Gửi lại khi mất phản hồi không tạo thêm mốc giao thành công. API cập nhật trạng thái thông thường cũng yêu cầu có ảnh khi hoàn tất.
- `GET /api/orders/{id}/delivery-photo` trả ảnh cho người có quyền xem đơn. `GET /api/orders/{id}/driver-avatar` trả ảnh tài xế của đơn. Ảnh giao lưu trong PostgreSQL, không kèm trong danh sách đơn để tránh tải nặng. Đơn demo cũ có thể chưa có ảnh. Ảnh chụp tạm được xóa khi xác nhận thành công hoặc bấm Bỏ ảnh; có thể phục hồi khi mở lại màn.
- Thông báo trong ứng dụng có thời gian, trạng thái chưa đọc và mở đúng đơn khi bấm. Tài xế nhận thông báo đơn đã được người khác nhận sẽ thấy thông báo hết khả dụng. Hiện thông báo phát sinh khi app tải lại dữ liệu trong phiên sử dụng, chưa phải push notification khi đóng app.
- Khung chờ có hiệu ứng nhẹ; giao thành công chỉ hiện sau khi backend xác nhận. Không cần quyền CAMERA vì app ủy quyền chụp cho ứng dụng máy ảnh; nếu không có máy ảnh hoặc người dùng hủy, đơn giữ nguyên trạng thái.

```text
Activity / Compose / XML
       ↓ thao tác       ↑ StateFlow, collectAsStateWithLifecycle
ViewModel / SavedStateHandle
       ↓
Repository → Retrofit / OkHttp → Spring Boot REST → JPA / Flyway → PostgreSQL
       └── Room (hồ sơ), DataStore (userId), private Preferences (token)
```

- Điều hướng: Login → chọn màn theo role. Client: Home → Create → Confirmation → Orders/Tracking → Detail/Rating; Profile → Logout. Tài xế: Trang chủ/Đơn chờ/Đang giao/Lịch sử/Hồ sơ. Admin: Overview/Orders/Users/Drivers/Alerts.
- Form tạo đơn và bản nháp xác nhận dùng SavedStateHandle; tab và bộ lọc Admin dùng rememberSaveable. Flow được quan sát theo lifecycle. Client cập nhật khi màn đang hoạt động mỗi 10 giây; driver mỗi 15 giây, có nút tải lại. Lần tải đầu đặt mốc dữ liệu hiện tại; các lần tải sau tạo thông báo trong app cho trạng thái đơn thay đổi hoặc đơn chờ mới, tránh báo hàng loạt dữ liệu seed cũ.
- Network sử dụng coroutine, timeout kết nối/đọc/ghi và nút retry. Cancellation được truyền tiếp. Mất mạng hiển thị lỗi; không tạo đơn giả/offline rồi báo thành công. HTTP 401 đưa người dùng về đăng nhập.
- Room v6 có migration từ v5 giữ dữ liệu và xóa password local cũ, export schema tại `Code/app/schemas`. Chưa có migration từ bản lịch sử v1–v4; cần sao lưu/chuyển đổi riêng trước khi nâng từ các bản đó.
- Liên hệ nhanh dùng `ACTION_DIAL`, chỉ mở màn quay số với số đã điền sẵn nên không cần quyền `CALL_PHONE`. Không có tác vụ cần WorkManager, foreground service, camera, microphone hoặc theo dõi GPS nền trong phạm vi hiện tại. GoDrop chỉ khai báo quyền INTERNET; chỉ đường được xử lý bởi Google Maps hoặc trình duyệt.

## Data model và API

User có role CLIENT/DELIVERY/ADMIN, trạng thái hoạt động và availability. DeliveryRequest thuộc một client, có tối đa một driver, địa chỉ/tọa độ hai điểm, liên hệ, giá, trạng thái và thời gian. PackageItem thuộc đơn. StatusHistory lưu trạng thái trước/sau, người cập nhật và thời gian. Rating gắn với đơn hoàn tất; RejectionReason/OrderRejection/DriverStatistics lưu lý do từ chối và độ tin cậy.

API đầy đủ tại `/v3/api-docs`; các nhóm chính: `/api/auth/login`, `/api/orders`, `/api/driver/**`, `/api/admin/**`, `/api/ratings`, `/api/locations/autocomplete`, `/api/routes/estimate`. Phân quyền và quyền sở hữu được kiểm tra ở backend, không chỉ ẩn nút trên Android. Nhận đơn khóa order rồi driver trong transaction; đồng thời chỉ một tài xế nhận một đơn, một tài xế chỉ có một đơn hoạt động.

## Trạng thái và tính phí

```text
CHO_TIEP_NHAN → DA_CHAP_NHAN → DA_DEN_NHA_HANG → DA_LAY_HANG → DANG_VAN_CHUYEN → DA_DEN_KHACH_HANG → DA_GIAO
Pending         Accepted      Đến điểm lấy      Picked Up      In Transit        Tới điểm giao       Delivered
```

Hủy (`DA_HUY`/Cancelled) được phép ở ba trạng thái trước lấy hàng. Sau `DA_LAY_HANG` không hủy. Server chặn nhảy cóc, cập nhật bởi tài xế khác và hủy bởi khách khác. Hai mốc đã lấy hàng và đang vận chuyển được tách riêng; Flyway V4 nâng constraint mà giữ dữ liệu cũ. **Cập nhật Android và khởi động lại backend cùng phiên bản** trước demo luồng trạng thái mới.

Phí backend: 15.000đ cơ bản + 5.000đ × km + 3.000đ × tổng kg; phụ phí theo loại hàng, xem [PricingService](Backend/src/main/java/com/mob10/deliveryserver/service/PricingService.java) là nguồn quy tắc chính thức. Khoảng cách lấy từ OSRM; server tính lại khi tạo đơn, không tin giá do client gửi. Màn xác nhận dùng báo giá server; gián đoạn nhà cung cấp có thể khiến không lấy được báo giá/tạo đơn.

## Kiểm thử và bàn giao

```bash
cd Code
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
cd ../Backend
../Code/gradlew test
```

Giữ bộ test gốc Java/Kotlin của nhóm; các test đã có được cập nhật khi hợp đồng nghiệp vụ thay đổi. Theo yêu cầu không thêm file test vào đồ án, các ca kiểm tra bổ sung và script PostgreSQL/UI của đợt rà soát được đặt ngoài repository, không có `run-db-tests.sh` trong dự án. Không dùng database demo để chạy các ca nhận/hủy đơn kiểm tra. Kết quả kiểm tra bổ sung được ghi riêng trong báo cáo nghiệm thu, không được hiểu là bộ test đi kèm repository.

- [Kết quả kiểm tra hồi quy tuần 3–5](Extra/Testing/W345-01-Regression.md)
- `Code`: Android Studio project; `Backend`: API; `DOCX`: nơi nộp báo cáo Word/PDF; `Extra`: sơ đồ/bằng chứng; `PPTX`: nơi nộp trình chiếu. **Kiểm tra 04/09: `Report-Android_08.docx`, `Report-Android_08.pdf` và `Presentation-Android_08.pptx` đều là file rỗng 0 byte, chưa phải tài liệu đã hoàn thành.** Các sơ đồ kiến trúc Room tuần trước chỉ là lịch sử; kiến trúc REST hiện tại được mô tả ở trên và trong API_CONTRACT.

## Video demo

**Chưa có link video Public/Unlisted được xác minh.** Nhóm cần quay đủ ba vai trò, tạo → nhận → giao → xem lịch sử/đánh giá, hủy trước pickup và ít nhất một ca lỗi, có âm thanh hoặc chú thích; sau đó dán link ở đây. Không coi build/test tự động là thay thế video và nghiệm thu UI.
