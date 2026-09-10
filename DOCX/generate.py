import os
import re
import zlib
import base64
import subprocess

MD_CONTENT = """---
title: "GoDrop - Hướng Dẫn Kỹ Thuật"
author: "Nhóm 10"
date: "Tháng 09/2026"
---

# 1. Giới thiệu dự án GoDrop

GoDrop là hệ thống giao hàng nội bộ hỗ trợ ứng dụng đa nền tảng cho Client (Khách hàng), Delivery (Tài xế) và Admin (Quản trị viên). Dự án bao gồm một ứng dụng di động Android (viết bằng Kotlin/Jetpack Compose) và một Backend RESTful API (viết bằng Spring Boot 3, Java 21, MongoDB).

Hệ thống cho phép người dùng tạo đơn hàng, hệ thống tự động tính toán phí và khoảng cách, tài xế nhận đơn và cập nhật trạng thái đơn hàng một cách liên tục.

# 2. Thành viên và phạm vi công việc

Dự án được xây dựng và phát triển bởi Nhóm 10 - MOB402. Phạm vi công việc bao gồm:
- Phát triển giao diện người dùng di động với Jetpack Compose.
- Phát triển API Backend với Spring Boot.
- Triển khai cơ sở dữ liệu MongoDB để lưu trữ dữ liệu người dùng, đơn hàng và hồ sơ tài xế.

# 3. Chức năng cốt lõi của ứng dụng

- **Xác thực và Phân quyền**: Đăng nhập qua JWT với vai trò rõ ràng (CLIENT, DELIVERY, ADMIN).
- **Quản lý đơn hàng (Client)**: Tạo đơn, xem chi tiết, hủy đơn, đánh giá tài xế.
- **Xử lý đơn hàng (Delivery)**: Xem danh sách đơn đang chờ, nhận đơn, từ chối đơn (có lý do), cập nhật trạng thái giao hàng.
- **Quản trị (Admin)**: Xem danh sách tài khoản, đơn hàng, tài xế và cảnh báo độ tin cậy.

# 4. Kiến trúc Android – Backend – MongoDB

Dự án áp dụng kiến trúc Client-Server tiêu chuẩn:
- **Android**: Sử dụng mô hình MVVM với Jetpack Compose, Room (lưu session cục bộ) và Retrofit để gọi API.
- **Backend**: Spring Boot 3 chia lớp Controller, Service, Repository. 
- **Database**: MongoDB lưu trữ toàn bộ dữ liệu (Người dùng, Đơn hàng, Đánh giá, Lịch sử trạng thái).

## Sơ đồ kiến trúc Auth và Navigation (Android)
```mermaid
flowchart TB
    subgraph Presentation[Presentation / Jetpack Compose]
        MAIN[MainActivity]
        APP[DeliveryApp]
        LOGIN[XML Login via AndroidView]
        NAV[destinationFor Role]
        CLIENT[ClientFeatureFlow]
        DRIVER[DriverHomeScreen]
        ADMIN[AdminHomeScreen]
        DESIGN[GoDrop Theme + Components]
    end

    subgraph State[State holders]
        AUTHVM[AuthViewModel]
        ADMINVM[AdminViewModel]
        DRIVERVM[DriverViewModel]
    end

    subgraph Data[Data layer]
        INIT[DatabaseInitializer]
        USERREPO[UserRepository]
        SESSION[DataStoreSessionStorage]
        DELIVERYREPO[DeliveryRepository]
        USERDAO[UserDao]
        OTHERDAO[Delivery / Package / History DAO]
        ROOM[(AppDatabase / Room)]
        DATASTORE[(Preferences DataStore)]
    end

    MAIN --> AUTHVM
    MAIN --> APP
    APP --> LOGIN
    APP --> NAV
    NAV --> CLIENT
    NAV --> DRIVER
    NAV --> ADMIN
    DESIGN -.styles.-> LOGIN
    DESIGN -.styles.-> CLIENT
    DESIGN -.styles.-> DRIVER
    DESIGN -.styles.-> ADMIN

    AUTHVM --> INIT
    AUTHVM --> USERREPO
    ADMIN --> ADMINVM
    DRIVER --> DRIVERVM
    ADMINVM --> USERREPO
    ADMINVM --> DELIVERYREPO
    DRIVERVM --> DELIVERYREPO
    USERREPO --> USERDAO
    USERREPO --> SESSION
    DELIVERYREPO --> OTHERDAO
    USERDAO --> ROOM
    SESSION --> DATASTORE
    OTHERDAO --> ROOM
    INIT --> ROOM
```

# 5. Cài đặt và chạy dự án

## Yêu cầu môi trường
- Java 21
- MongoDB (Atlas hoặc Local)

## Thiết lập Backend
1. Tạo file `.env` tại thư mục `Backend` với cấu hình:
   ```dotenv
   MONGODB_URI=mongodb://localhost:27017/delivery_db
   JWT_SECRET=khoa_bi_mat_dai_hon_32_ky_tu
   DEMO_ENABLED=true
   DEMO_PASSWORD=123456
   ```
2. Chạy Backend bằng lệnh:
   ```bash
   ./gradlew bootRun -p .
   ```
Swagger UI sẽ có sẵn tại: `http://localhost:8080/swagger-ui.html`

# 6. Đăng nhập, JWT và phân quyền

Hệ thống sử dụng **JWT (JSON Web Token)** để phân quyền. 
- Thời hạn token (JWT Expiration) được lấy từ `application.yml`, mặc định là `86400000` ms (24 giờ).
- Sau khi đăng nhập thành công (`POST /api/auth/login`), server trả về `accessToken`.
- Các Role hợp lệ hiện hành: `CLIENT`, `DELIVERY`, `ADMIN`. Phân quyền được server kiểm tra trên từng endpoint.

# 7. Điều hướng theo CLIENT, DELIVERY và ADMIN

Tùy theo Role của người dùng, ứng dụng Android điều hướng tới màn hình tương ứng. Quá trình kiểm tra Role và điều hướng diễn ra như sau:

## Sơ đồ luồng Điều hướng
```mermaid
flowchart TD
    START([Mở ứng dụng]) --> INIT[Khởi tạo tài khoản mẫu]
    INIT -->|Thành công| READ_SESSION[Đọc userId từ DataStore]
    INIT -. Lỗi / hiện thông báo .-> LOGIN
    READ_SESSION -->|Không có session| LOGIN[Login]
    READ_SESSION -->|Có userId| CHECK_USER[Truy vấn UserDao theo id]
    CHECK_USER -->|User không còn tồn tại| INVALID[Clear invalid session]
    INVALID --> LOGIN
    CHECK_USER -->|User hợp lệ| ROUTE{Role hiện tại trong Room}
    LOGIN -->|Thiếu trường hoặc sai tài khoản| ERROR[Hiển thị lỗi]
    ERROR -->|Nhập lại| LOGIN
    LOGIN -->|Đúng tài khoản Room| SAVE[Save userId vào DataStore]
    SAVE --> ROUTE
    ROUTE -->|CLIENT| CLIENT[Client Home]
    ROUTE -->|DELIVERY| DELIVERY[Delivery Home]
    ROUTE -->|ADMIN| ADMIN[Admin Home]
    CLIENT -->|Đăng xuất| LOGOUT[Clear DataStore session]
    DELIVERY -->|Đăng xuất| LOGOUT
    ADMIN -->|Đăng xuất| LOGOUT
    LOGOUT --> CLEAR_STATE[Clear currentUser]
    CLEAR_STATE --> LOGIN
```

# 8. Hồ sơ, email, OTP và khôi phục mật khẩu

- **Liên kết email**: Người dùng vào Hồ sơ, cung cấp email và mật khẩu. Hệ thống gửi OTP xác minh qua Spring Mail.
- **Đổi mật khẩu**: Sử dụng số điện thoại (tên đăng nhập) để gửi mã khôi phục tới email đã xác minh. OTP có hiệu lực 10 phút.
- Giới hạn hệ thống: Cho phép gửi mã khôi phục 3 lần/giờ, tối đa 5 lần đoán sai mã. 

# 9. API tài khoản

| Method | Endpoint | Quyền | Ý nghĩa |
| --- | --- | --- | --- |
| GET | `/api/account` | Đã đăng nhập | Xem hồ sơ (username, role, ảnh đại diện, email...) |
| PUT | `/api/account` | Đã đăng nhập | Sửa hồ sơ, mật khẩu |
| POST | `/api/account/email/request` | Đã đăng nhập | Gửi mã xác minh email |
| POST | `/api/account/email/verify` | Đã đăng nhập | Nhập mã xác minh email |
| POST | `/api/auth/recovery/request` | Public | Yêu cầu gửi OTP quên mật khẩu |

# 10. API đơn hàng

Mọi endpoint đơn hàng đều yêu cầu JWT (ngoại trừ route công khai nếu có).

| Method | Endpoint | Role | Ý nghĩa |
|---|---|---|---|
| POST | `/api/orders` | CLIENT | Tạo đơn cho khách |
| GET | `/api/orders` | CLIENT/DELIVERY/ADMIN | Lấy danh sách đơn theo phạm vi quyền |
| GET | `/api/orders/{id}` | Theo quyền xem | Chi tiết đơn |
| GET | `/api/orders/{id}/history` | Theo quyền xem | Lịch sử đơn |
| POST | `/api/orders/{id}/cancel` | CLIENT | Hủy đơn khi chưa được xử lý |

Khi tạo đơn, client gửi toạ độ, nhưng backend sẽ **tự động tính toán lại lộ trình và cước phí**, không phụ thuộc vào `distanceKm` do Android gửi lên để tránh gian lận.

# 11. Luồng khách hàng

- **Tạo đơn**: Client nhập địa chỉ lấy/giao hàng. Backend ước tính phí dựa trên tọa độ, tính theo công thức demo (ví dụ: 15.000đ phí cơ bản + 5.000đ/km + phí khối lượng/hỏa tốc).
- **Hủy đơn**: Client có thể hủy đơn khi đơn ở trạng thái `CHO_TIEP_NHAN`, `DA_CHAP_NHAN` hoặc `DA_DEN_NHA_HANG`. Sau khi tài xế lấy hàng, không thể hủy.

# 12. Luồng tài xế

- **Nhận đơn (Accept)**: Tài xế có thể chấp nhận đơn trong danh sách Open Pool. Đơn sẽ cập nhật trạng thái `DA_CHAP_NHAN`. 
- **Từ chối (Reject)**: Tài xế từ chối đơn, đơn sẽ biến mất khỏi màn hình tài xế đó nhưng vẫn nằm trong Open Pool cho người khác.
- **Cập nhật trạng thái**: Tài xế cập nhật đúng chuỗi trạng thái:
  `CHO_TIEP_NHAN` -> `DA_CHAP_NHAN` -> `DA_DEN_NHA_HANG` -> `DA_LAY_HANG` -> `DANG_VAN_CHUYEN` -> `DA_DEN_KHACH_HANG` -> `DA_GIAO`.
- **Reliability Score (Độ tin cậy)**: Từ chối đơn với lý do không chính đáng sẽ bị trừ điểm. Ba lần Reject phạt trong 24 giờ sẽ bị khóa quyền nhận đơn 30 phút.

# 13. Luồng quản trị viên

Admin cung cấp quyền quản trị bao quát toàn hệ thống:
- **Tài khoản**: Xem danh sách tất cả người dùng và vai trò.
- **Đơn hàng**: Giám sát toàn bộ các đơn hàng hiện có.
- **Cảnh báo**: Xem danh sách tài xế có `Reliability Score < 70` hoặc đang bị khóa do từ chối quá nhiều lần.

# 14. Đánh giá tài xế

Khách hàng chỉ có thể đánh giá tài xế sau khi đơn hàng đạt trạng thái `DA_GIAO`.
- Mỗi đơn hàng chỉ được đánh giá 1 lần.
- Endpoint `POST /api/ratings` yêu cầu khách sở hữu đơn và cung cấp số sao (1-5) cùng nhận xét.
- API tính toán điểm số trung bình (average rating) cho mỗi tài xế dựa trên lịch sử đánh giá.

# 15. MongoDB collections và indexes

Hệ thống sử dụng MongoDB làm cơ sở dữ liệu chính.
Các Collection chính:
- `users`: Thông tin tài khoản, role, biển số xe, reliability score.
- `delivery_requests`: Lưu thông tin đơn hàng, điểm lấy, điểm giao, phí và tài xế phụ trách.
- `packages`: Các gói/kiện hàng thuộc về một đơn.
- `status_histories`: Ghi chép lịch sử đổi trạng thái của đơn hàng.
- `order_rejections`: Lưu trữ lịch sử từ chối đơn hàng của tài xế.
- `ratings`: Lưu thông tin đánh giá.

Dữ liệu được truy vấn nhanh chóng với Document references và query thông thường, tương thích hoàn toàn với cấu trúc entity của Android.

# 16. Kiểm thử và tài khoản demo

Tài khoản được seed sẵn bởi `DatabaseSeeder` khi bật `DEMO_ENABLED=true` và sử dụng mật khẩu ở biến `DEMO_PASSWORD`.
- **CLIENT**: `client1` đến `client5`.
- **DELIVERY**: `shipper1` đến `shipper7` (bao gồm các trạng thái BUSY, OFFLINE, AVAILABLE và cả tài xế đang bị khóa do điểm phạt).
- **ADMIN**: `admin`.

# 17. Các lỗi thường gặp và cách xử lý

- `401 UNAUTHORIZED`: Token hết hạn hoặc sai. Xử lý: Người dùng cần đăng nhập lại.
- `403 NOT_ORDER_OWNER / NOT_ASSIGNED_DRIVER`: Thao tác trên đơn hàng không thuộc quyền sở hữu của người dùng.
- `409 ORDER_ALREADY_TAKEN`: Tài xế cố gắng nhận đơn đã bị người khác nhận trước.
- `409 INVALID_STATUS_TRANSITION`: Tài xế cập nhật trạng thái nhảy bước (ví dụ: từ `DA_CHAP_NHAN` sang thẳng `DA_GIAO`).
- `423 DRIVER_TEMPORARILY_LOCKED`: Tài xế tạm khóa do vi phạm điểm tin cậy.

# 18. Phụ lục API contract và sơ đồ

## Sơ đồ luồng đăng nhập UseCase
```mermaid
flowchart LR
    USER[Người dùng] --> LOGIN((Đăng nhập))
    LOGIN -.include.-> VALIDATE((Kiểm tra dữ liệu nhập))
    LOGIN -.include.-> AUTH((Xác thực Room))
    AUTH -.include.-> ROUTE((Xác định role))
    ROUTE --> CLIENT[Client Home]
    ROUTE --> DELIVERY[Delivery Home]
    ROUTE --> ADMIN[Admin Home]
```

Toàn bộ API được tài liệu hoá bằng Swagger UI.
Base URL: `http://localhost:8080/api`

---
*Tài liệu được sinh tự động nhằm đảm bảo đồng bộ với cấu trúc Code Backend (MongoDB) hiện tại.*
"""

def get_kroki_url(mermaid_code):
    encoded = base64.urlsafe_b64encode(zlib.compress(mermaid_code.encode('utf-8'), 9)).decode('ascii')
    return f"https://kroki.io/mermaid/png/{encoded}"

def process_markdown(md):
    pattern = r'```mermaid(.*?)```'
    
    def replacer(match):
        code = match.group(1).strip()
        img_url = get_kroki_url(code)
        return f"![Sơ đồ Mermaid]({img_url})"
        
    return re.sub(pattern, replacer, md, flags=re.DOTALL)

def main():
    print("Đang xử lý Markdown và tạo ảnh sơ đồ...")
    processed_md = process_markdown(MD_CONTENT)
    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    md_file = os.path.join(script_dir, "GoDrop-Huong-Dan-Ky-Thuat.md")
    docx_file = os.path.join(script_dir, "GoDrop-Huong-Dan-Ky-Thuat.docx")
    
    with open(md_file, "w", encoding="utf-8") as f:
        f.write(processed_md)
        
    print(f"Đã tạo file {md_file}. Đang tiến hành chạy Pandoc...")
    
    try:
        # Gọi pandoc để convert
        subprocess.run(["pandoc", md_file, "-o", docx_file, "--toc"], check=True)
        print(f"✅ Đã tạo thành công {docx_file}")
        
        # Xóa file Markdown tạm
        if os.path.exists(md_file):
            os.remove(md_file)
            print(f"Đã xóa file Markdown tạm: {md_file}")
    except FileNotFoundError:
        print("⚠️ Không tìm thấy công cụ pandoc!")
        print("Vui lòng cài đặt pandoc (vd: sudo apt install pandoc) hoặc tự chạy lệnh:")
        print(f"cd DOCX && pandoc GoDrop-Huong-Dan-Ky-Thuat.md -o GoDrop-Huong-Dan-Ky-Thuat.docx --toc")
    except Exception as e:
        print(f"⚠️ Có lỗi xảy ra khi gọi pandoc: {e}")

if __name__ == "__main__":
    main()
