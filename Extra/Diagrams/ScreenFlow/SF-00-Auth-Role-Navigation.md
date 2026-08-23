# SF-00 — Authentication and role navigation

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

## Mapping source

- `AuthViewModel`: khởi tạo, restore session, login và logout.
- `UserRepository`: login bằng `UserDao`, lưu/xóa session và kiểm tra invalid session.
- `DataStoreSessionStorage`: chỉ lưu `current_user_id`.
- `destinationFor(Role?)`: ánh xạ role thành destination.
- `DeliveryApp`: render Login hoặc đúng Home.
- `DashboardScaffold`: shell chung của ba Home.
