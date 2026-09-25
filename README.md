# GoDrop Delivery App — Android_08

GoDrop là ứng dụng Android native hỗ trợ quy trình giao hàng giữa khách hàng, tài xế và quản trị viên. Ứng dụng được viết bằng Kotlin/Jetpack Compose; backend dùng Java 21, Spring Boot và MongoDB.

| Thông tin | Giá trị |
|---|---|
| Môn học | MOB402 |
| Nhóm | Nhóm 10 |
| Mã đề tài | Android_08 |
| Tên sản phẩm | GoDrop Delivery App |
| Nền tảng | Android native, không phải Web App |
| GitHub | [NguyenTanHiep07/MOB402-Nhom10-Android_08](https://github.com/NguyenTanHiep07/MOB402-Nhom10-Android_08) |
| Tên gói nộp Course | `MOB402-Nhom10-Android_08.7z` |

## 1. Mục tiêu và phạm vi

Mục tiêu của dự án là xây dựng một hệ thống giao hàng có dữ liệu dùng chung, xử lý đúng quyền của ba vai trò và duy trì lịch sử trạng thái đơn hàng. Android gọi REST API thật; dữ liệu nghiệp vụ chính được lưu trên MongoDB, không dùng danh sách giả trong giao diện để báo thao tác thành công.

### Chức năng theo vai trò

- **Khách hàng (Client):** đăng ký, đăng nhập, sửa hồ sơ/ảnh đại diện, khôi phục mật khẩu, tìm địa chỉ, xem báo giá, tạo và hủy đơn trước khi lấy hàng, theo dõi tiến trình, xem lịch sử, nhận thông báo trong ứng dụng và đánh giá sau giao.
- **Tài xế (Delivery/Driver):** xem đơn chờ, nhận hoặc từ chối đơn, xem chuyến hiện tại, cập nhật trạng thái đúng thứ tự, gọi người gửi/người nhận, mở chỉ đường, chụp ảnh xác nhận giao, xem lịch sử/thu nhập/điểm tin cậy và đổi trạng thái làm việc.
- **Quản trị viên (Admin):** xem tổng quan, tìm/lọc và xem chi tiết toàn bộ đơn hàng, người dùng, tài xế và cảnh báo vi phạm.

### Ngoài phạm vi hiện tại

- Màn Admin chỉ đọc; chưa có sửa/xóa tài khoản, sửa bảng giá hoặc phân công lại đơn.
- Chỉ đường mở Google Maps hoặc trình duyệt bằng Maps URL; ứng dụng không nhúng Maps SDK và không theo dõi GPS nền.
- Thông báo được tạo khi ứng dụng tải lại dữ liệu trong phiên sử dụng; chưa có push notification khi ứng dụng đã đóng.
- Không hỗ trợ tạo đơn offline. Khi mất mạng, ứng dụng hiển thị lỗi và cho phép thử lại thay vì ghi nhận thành công giả.

## 2. Thành viên và phân công

| Thành viên | MSSV | Công việc chính |
|---|---|---|
| Nguyễn Tấn Hiệp | 087205010642 | Backend Spring Boot, bảo mật/JWT, MongoDB, tích hợp hệ thống, cấu hình môi trường và nghiệm thu |
| Nguyễn Quốc Thịnh | 052206007772 | REST API, DTO/repository, kết nối Retrofit và luồng dữ liệu Android |
| Huỳnh Nhật Nam | 080206015277 | Chức năng tài xế, chỉ đường Google Maps, luồng trạng thái giao hàng; Room/ERD ở giai đoạn đầu |
| Nguyễn Lâm Hữu Hùng | 079205019508 | Giao diện và chức năng khách hàng, Admin, đánh giá tài xế |

Mỗi thành viên sử dụng tài khoản Git cá nhân và commit phần việc của mình. Commit message phải mô tả rõ thay đổi, không dùng riêng các nội dung chung chung như `update`, `fix` hoặc `final`.

## 3. Đối chiếu yêu cầu chung

| Nhóm yêu cầu | Cách dự án đáp ứng | Bằng chứng chính |
|---|---|---|
| Ứng dụng Android | Kotlin, Android SDK và Jetpack Compose; chạy trên máy ảo hoặc thiết bị thật | `Code/Frontend` |
| Kiến trúc | UI, ViewModel/state holder, repository, nguồn local/remote được tách lớp | Sơ đồ bên dưới và `Code/Frontend/app/src/main` |
| Lifecycle và trạng thái | StateFlow, `collectAsStateWithLifecycle`, SavedStateHandle; có loading/content/empty/success/error | Các ViewModel và màn hình Compose |
| Dữ liệu | MongoDB là nguồn nghiệp vụ chung; Room v6 lưu dữ liệu local có cấu trúc; DataStore lưu `userId`; Preferences riêng lưu token | `data/local`, `data/session`, backend domain/repository |
| File/phần cứng | FileProvider tạo URI ảnh giao hàng trong app storage; chụp ảnh qua ứng dụng camera ngoài | `DeliveryPhotoPanel.kt`, `delivery_photo_paths.xml` |
| Network | Retrofit/OkHttp, coroutine, timeout, cancellation, xử lý lỗi và retry tại giao diện | `RetrofitClient.kt`, các repository/ViewModel |
| Bảo mật | JWT, phân quyền backend, kiểm tra quyền sở hữu, secret qua biến môi trường, tắt HTTP log chứa dữ liệu người dùng | `security`, `application.yml`, `.gitignore` |
| Tính toàn vẹn dữ liệu | Backend kiểm tra chuyển trạng thái; transaction/atomic update cho các thao tác phù hợp; kiểm soát nhận đơn đồng thời | Service backend và automated tests |
| Kiểm thử | Unit/repository/service/security tests, một instrumented race-condition test và 12 test case giao diện có ảnh | Mục 12 và `Extra` |
| Báo cáo/demo | DOCX, PPTX và thư mục bằng chứng đã có; video và link chia sẻ phải hoàn thiện trước khi nộp | Mục 13–15 |

## 4. Công nghệ và thư viện

### Android

- Kotlin 2.1.20, Android Gradle Plugin 8.13.2.
- Jetpack Compose Material 3 và Navigation Compose.
- ViewModel, StateFlow, lifecycle-runtime-compose và SavedStateHandle.
- Room 2.6.1/KSP cho dữ liệu cục bộ có cấu trúc.
- DataStore Preferences cho định danh phiên; SharedPreferences private cho access token cần đọc đồng bộ từ interceptor.
- Retrofit 2.11.0, Gson và OkHttp 4.12.0 cho REST API.
- Kotlin Coroutines cho tác vụ bất đồng bộ.
- Lottie Compose 6.6.2 cho hiệu ứng giao diện.
- JUnit, coroutine-test, Room testing, Robolectric, AndroidX Test và Espresso cho kiểm thử.

### Backend và dịch vụ ngoài

- Java 21, Spring Boot 3.4.5.
- Spring Web, Validation, Security, Mail và Spring Data MongoDB.
- JWT (`jjwt`) cho xác thực và phân quyền.
- Springdoc OpenAPI/Swagger cho tài liệu và thử API.
- MongoDB 7; Docker Compose dùng cho database local.
- Photon để gợi ý địa chỉ, OSRM để ước lượng tuyến đường và Google Maps URL để mở chỉ đường.

## 5. Kiến trúc và luồng dữ liệu

```text
Activity / Jetpack Compose
        │ thao tác                 ▲ UI state
        ▼                          │
ViewModel / SavedStateHandle / StateFlow
        │
        ▼
Repository ── Retrofit / OkHttp ── Spring Boot REST
    │                                  │
    ├── Room (dữ liệu local)           ├── Service / transaction
    ├── DataStore (userId)             ├── Spring Security / JWT
    └── Preferences (token)            └── Spring Data MongoDB ── MongoDB
```

- Backend là nguồn chính cho tài khoản, đơn hàng, trạng thái, đánh giá và thống kê.
- Repository che giấu chi tiết nguồn dữ liệu với ViewModel.
- Form tạo đơn và báo giá dùng SavedStateHandle để giữ dữ liệu khi tái tạo màn hình. Bộ lọc/tab phù hợp dùng `rememberSaveable`.
- Client tải lại dữ liệu khi màn hình đang hoạt động theo chu kỳ 10 giây; driver theo chu kỳ 15 giây và có nút tải lại.
- Room đang ở schema v6, có migration từ các phiên bản v1–v5 lên v6 và export schema tại `Code/Frontend/app/schemas`.

### Luồng điều hướng

```text
Đăng nhập
├── Client: Home → Create → Confirmation → Orders/Tracking → Detail → Rating
│           └── Profile → Edit account / Recovery / Logout
├── Driver: Home → Open orders → Active order → Delivery history → Profile
└── Admin: Overview → Orders → Users → Drivers → Alerts
```

## 6. Data model và trạng thái đơn hàng

- `User`: thông tin tài khoản, vai trò `CLIENT`/`DELIVERY`/`ADMIN`, trạng thái hoạt động và availability.
- `DeliveryRequest`: khách hàng, tối đa một tài xế, địa chỉ/toạ độ hai đầu, liên hệ, phí, trạng thái và thời gian.
- `PackageItem`: thông tin kiện hàng thuộc một yêu cầu giao.
- `StatusHistory`: trạng thái trước/sau, người cập nhật và thời điểm.
- `Rating`: đánh giá gắn với đơn đã giao.
- `RejectionReason`, `OrderRejection`, `DriverStatistics`: lý do từ chối, lịch sử từ chối và độ tin cậy của tài xế.

```text
CHO_TIEP_NHAN → DA_CHAP_NHAN → DA_DEN_NHA_HANG → DA_LAY_HANG
      → DANG_VAN_CHUYEN → DA_DEN_KHACH_HANG → DA_GIAO
```

`DA_HUY` chỉ được phép ở ba trạng thái trước khi lấy hàng. Backend chặn nhảy cóc trạng thái, khách hàng khác hủy đơn, tài xế khác cập nhật đơn và nhiều tài xế nhận cùng một đơn.

Phí do backend tính lại: 15.000đ cơ bản + 5.000đ/km + 3.000đ/kg, cộng phụ phí theo loại hàng. Android không được tự quyết định số tiền cuối cùng.

## 7. Storage, permission, phần cứng và tác vụ nền

| Thành phần | Cách sử dụng |
|---|---|
| MongoDB | Dữ liệu nghiệp vụ dùng chung giữa các vai trò |
| Room | Dữ liệu local có cấu trúc và hồ sơ tài khoản; schema v6 |
| DataStore | Lưu `userId` của phiên đăng nhập |
| SharedPreferences private | Lưu access token, loại token và thời gian hết hạn |
| App storage + FileProvider | Ảnh chụp giao hàng tạm thời; URI chỉ được cấp quyền cho ứng dụng camera |
| Permission | Chỉ khai báo `INTERNET`; không yêu cầu `CAMERA`, `CALL_PHONE`, vị trí, microphone hoặc quyền bộ nhớ |
| Camera | Gọi ứng dụng camera ngoài; nếu không có camera hoặc người dùng hủy thì đơn giữ nguyên |
| Gọi điện | `ACTION_DIAL` chỉ mở màn quay số, không tự gọi |
| Chỉ đường | Google Maps hoặc trình duyệt xử lý vị trí và quyền của chính ứng dụng đó |
| Tác vụ nền | Không dùng WorkManager/foreground service vì không có công việc trì hoãn hoặc theo dõi nền trong phạm vi hiện tại |

Ảnh tạm được xóa sau khi xác nhận giao thành công hoặc khi người dùng bỏ ảnh. Ảnh giao hàng được gửi qua `POST /api/driver/orders/{id}/complete-with-photo`; backend kiểm tra ảnh, quyền tài xế và trạng thái trước khi lưu ảnh cùng kết quả giao.

## 8. API chính

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Các nhóm endpoint chính:

- `/api/auth/login`, `/api/auth/register`: đăng nhập và đăng ký Client.
- `/api/account/**`, `/api/auth/recovery/**`: hồ sơ và khôi phục tài khoản.
- `/api/orders/**`: tạo, liệt kê, xem chi tiết/lịch sử và hủy đơn.
- `/api/driver/**`: đơn chờ, nhận/từ chối, cập nhật trạng thái, ảnh giao hàng và thống kê.
- `/api/admin/**`: tổng quan, đơn hàng, người dùng, tài xế và cảnh báo.
- `/api/ratings/**`: gửi và đọc đánh giá.
- `/api/locations/autocomplete`, `/api/routes/estimate`: địa chỉ và ước lượng tuyến đường.

Phân quyền và quyền sở hữu được kiểm tra tại backend, không chỉ dựa vào việc ẩn nút trên Android.

## 9. Yêu cầu môi trường

- Android Studio hỗ trợ Android Gradle Plugin 8.13.2.
- JDK 21 để chạy Android build tools và backend; Android bytecode target Java 11.
- Android SDK compile/target 36, min SDK 24.
- Android Emulator hoặc thiết bị thật từ Android 7.0 trở lên.
- Docker Desktop và Docker Compose nếu chạy MongoDB local, hoặc một MongoDB Atlas URI.
- Internet để gọi Photon/OSRM và mở Google Maps.

## 10. Cài đặt và chạy

### 10.1 Backend và MongoDB local

Từ thư mục repository:

```bash
cd Code/Backend
./setup-local.sh
docker compose up -d --wait
docker compose exec mongodb mongosh --quiet --eval 'try { rs.status().ok } catch (e) { rs.initiate({_id:"rs0",members:[{_id:0,host:"localhost:27017"}]}) }'
./gradlew bootRun
```

`setup-local.sh` tạo `Code/Backend/.env` với quyền file hạn chế nếu file chưa tồn tại. File chứa secret này bị `.gitignore` loại khỏi Git. Mặc định `DEMO_ENABLED=false`; chỉ bật demo trên database riêng.

Các biến cấu hình quan trọng:

| Biến | Mục đích |
|---|---|
| `MONGODB_URI` | Kết nối MongoDB local hoặc Atlas |
| `JWT_SECRET` | Khóa ký JWT, bắt buộc lấy từ môi trường |
| `DEMO_ENABLED`, `DEMO_PASSWORD` | Bật và bảo vệ dữ liệu demo |
| `MAIL_ENABLED`, `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Cấu hình email khôi phục tài khoản |
| `PHOTON_BASE_URL`, `OSRM_BASE_URL` | Dịch vụ địa chỉ và định tuyến |

Khi `DEMO_ENABLED=true`, backend seed một lần bộ dữ liệu đa trạng thái; không xóa dữ liệu có sẵn và không nhân bản sau mỗi lần khởi động.

### 10.2 Android

Mở `Code/Frontend` bằng Android Studio, Sync Gradle và chạy module `app`. Backend phải đang hoạt động.

- Máy ảo Android dùng mặc định: `http://10.0.2.2:8080/api/`.
- Thiết bị thật phải cùng mạng với máy chạy backend:

```bash
cd Code/Frontend
./gradlew :app:assembleDebug -PAPI_BASE_URL=http://DIA_CHI_IP_LAN:8080/api/
```

Debug cho phép HTTP để demo LAN. Release chỉ build khi URL là HTTPS và có đủ `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`. Không đưa keystore hoặc mật khẩu vào repository.

Tài khoản demo gồm `client1`…`client5`, `shipper1`…`shipper7` và `admin`. Đăng nhập bằng số điện thoại tương ứng; xem chi tiết trong [hướng dẫn backend](Code/Backend/README.md). Không ghi mật khẩu thật vào README hoặc ảnh minh chứng.

## 11. Xử lý lỗi và bảo mật

- Form và DTO kiểm tra dữ liệu trước khi lưu/xử lý; backend tiếp tục xác thực dữ liệu nhận từ Android.
- OkHttp đặt timeout kết nối/đọc/ghi 30 giây và call timeout 45 giây. Coroutine cancellation được truyền tiếp.
- Giao diện hiển thị loading, dữ liệu, danh sách rỗng, thành công và lỗi; nút thao tác bị khóa khi request đang chạy để tránh gửi lặp.
- Lỗi mạng không tạo dữ liệu thành công giả; người dùng có thể thử lại. HTTP 401 xóa phiên phù hợp và đưa về đăng nhập.
- Logging interceptor đặt `NONE`, tránh ghi token, địa chỉ hoặc dữ liệu khách hàng vào Logcat.
- Secret và cấu hình nhạy cảm dùng biến môi trường hoặc `.env` bị ignore; repository không lưu password, private key hoặc keystore.
- Backend dùng JWT, Spring Security, kiểm tra vai trò/quyền sở hữu và atomic/transaction cho thao tác cần tính nhất quán.
- MongoDB local chỉ bind cổng `127.0.0.1:27017` trong Docker Compose.

## 12. Kiểm thử

### 12.1 Automated tests

```bash
cd Code/Frontend
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug

cd ../Backend
./gradlew test
```

Instrumented test về cạnh tranh nhận/hủy đơn cần emulator hoặc thiết bị:

```bash
cd Code/Frontend
./gradlew :app:connectedDebugAndroidTest
```

Bộ test hiện có kiểm tra tính phí, tạo/hủy/nhận đơn, quyền chủ sở hữu, chuyển trạng thái, lịch sử, đánh giá, repository Admin/location, ViewModel, tái tạo form, Room migration, bảo mật API, timeout/location client, database seeder và tình huống cạnh tranh.

### 12.2 Functional test và bằng chứng

- Môi trường đã ghi nhận: Android Emulator Pixel 10, Android 15; backend Spring Boot 3.4.5 và MongoDB.
- Ngày kiểm thử trong báo cáo hiện tại: 11/09/2026.
- Có 12 test case giao diện cho đăng nhập đa vai trò, Client, Driver và Admin; kết quả thực tế đều được ghi là ổn định.
- Báo cáo chi tiết: [Extra/BaoCao_KiemThu.xlsx](Extra/BaoCao_KiemThu.xlsx).
- Ảnh minh chứng: [Extra/TestEvidence](Extra/TestEvidence).

Trước khi nộp cuối kỳ, nhóm cần bổ sung hoặc quay lại bằng chứng cho các ca chưa thể hiện đầy đủ trong báo cáo 12 test case hiện tại:

- Tạo đơn, hủy đơn, đánh giá và giao hàng kèm ảnh.
- Dữ liệu nhập không hợp lệ và chuyển trạng thái không hợp lệ.
- Mất mạng/timeout, camera không khả dụng hoặc người dùng hủy chụp.
- Tái tạo Activity/màn hình sau thay đổi cấu hình hoặc quay lại từ background.
- Tập dữ liệu đại diện/lớn hơn và thao tác lặp lại.
- Ghi đủ thiết bị, phiên bản Android, dữ liệu đầu vào, kết quả mong đợi, kết quả thực tế và file minh chứng cho từng ca.

Không chạy test thay đổi dữ liệu trên database demo đang dùng để trình bày; dùng database kiểm thử riêng.

## 13. Cấu trúc repository và tài liệu bàn giao

```text
MOB402-Nhom10-Android_08/
├── Code/
│   ├── Frontend/          # Android Studio project
│   └── Backend/           # Spring Boot REST API
├── DOCX/
│   ├── Report-Android_08.docx
│   └── GoDrop-Huong-Dan-Ky-Thuat.docx
├── Extra/
│   ├── BaoCao_KiemThu.xlsx
│   ├── Diagrams/          # ERD, use case, activity, state, architecture, screen flow
│   ├── TestEvidence/      # Ảnh minh chứng test case
│   └── Video/             # Thông tin video
├── PPTX/
│   └── Presentation-Android_08.pptx
├── .gitignore
└── README.md
```

`.gitignore` loại trừ `build/`, `.gradle/`, `.kotlin/`, `.idea/`, `local.properties`, APK/AAB, log, file tạm, `.env`, API key, password, secret, private key và keystore. Gradle wrapper vẫn được giữ để giảng viên có thể build dự án.

## 14. Video demo

**Link YouTube Public/Unlisted: chưa được nhóm cung cấp.**

Video cuối kỳ phải:

- Demo đủ ba vai trò và toàn bộ chức năng bắt buộc.
- Có ít nhất một tình huống lỗi/thất bại phù hợp.
- Thể hiện luồng tạo → nhận → giao → lịch sử/đánh giá và hủy trước khi lấy hàng.
- Có âm thanh hoặc chú thích giải thích thao tác.
- Mỗi thành viên trình bày phần việc của mình và hiển thị khuôn mặt làm minh chứng.
- Ghi cùng link chia sẻ trong README và báo cáo Word trước khi nộp.

## 15. Checklist trước khi nộp Course

- [ ] Cập nhật link video YouTube trong README và báo cáo Word.
- [ ] Kiểm tra báo cáo Word đúng mẫu, không quá 15 trang và có đủ mục bắt buộc.
- [ ] Kiểm tra PowerPoint mở bình thường và sẵn sàng thuyết trình.
- [ ] Bổ sung kết quả kiểm thử còn thiếu và đối chiếu từng ảnh minh chứng.
- [ ] Chạy automated tests, build debug và lint trên phiên bản cuối.
- [ ] Xóa `build/`, `.gradle/`, `.kotlin/`, `local.properties`, APK/AAB, log và file tạm trước khi đóng gói.
- [ ] Kiểm tra không có `.env`, password, token, API secret, private key hoặc keystore trong Git/gói nộp.
- [ ] Kiểm tra GitHub đã có đầy đủ commit của từng thành viên và branch chính ở trạng thái mới nhất.
- [ ] Nén toàn bộ source theo đúng tên `MOB402-Nhom10-Android_08.7z`.
- [ ] Nộp source, DOCX, PPTX và link GitHub trước khi Course đóng.
