# W345-01 — Core/Auth/Session/Navigation regression

Ngày cập nhật: 18/08/2026  
Nhánh: `feature/core-integration-v3`  
Baseline đã tích hợp: `origin/main` tại commit `a671fb3`

## Phạm vi Hiệp đã kiểm tra

- Login đọc tài khoản thật từ `UserDao`/Room.
- CLIENT, DELIVERY và ADMIN ánh xạ qua `Role.kt` và `AppDestination.kt` hiện có.
- Login thành công lưu `userId` vào Preferences DataStore.
- Khởi động lại đọc session và truy vấn lại user/role từ Room.
- Session trỏ tới user không còn tồn tại sẽ bị xóa và fallback về Login.
- Logout dùng chung cho cả ba Home, xóa DataStore rồi clear `currentUser`.
- Luồng Client mới trên `main` vẫn nhận đúng `currentUser` sau tích hợp.
- `gradlew` được khôi phục executable bit sau khi đồng bộ `main`.

## Kết quả tự động

Chạy bằng JDK của Android Studio:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Kết quả ngày 18/08/2026:

- Unit test: 17 passed, 0 failed.
- `assembleDebug`: thành công.
- `lintDebug`: thành công, không có lỗi chặn build.
- Các warning lint còn lại thuộc UI/icon/dependency cũ, ngoài phạm vi W345-01.

## Kết quả runtime trên emulator

Thiết bị: Pixel 7, Android API 37.1.

- CLIENT `0123456789`: vào đúng **Khu vực khách hàng**, hiển thị Nguyễn Văn A.
- DELIVERY `0111222333`: vào đúng **Khu vực tài xế**, hiển thị Lê Văn C.
- ADMIN `0000000000`: vào đúng **Trung tâm quản trị**, hiển thị Quản trị viên.
- Force-stop và mở lại sau khi login ADMIN: session được restore về ADMIN, không hiện Login.
- Logout ADMIN và mở lại: vẫn ở Login, chứng minh session đã được xóa.
- Logout CLIENT và DELIVERY đều quay về Login.

## Regression do từng thành viên xác nhận

| Phần | Người phụ trách | Trạng thái tổng hợp |
|---|---|---|
| Core/Auth/Session/Role navigation | Nguyễn Tấn Hiệp | Automated checks passed |
| Client flow | Hữu Hùng | Auth landing/logout passed; cần người phụ trách xác nhận toàn bộ nghiệp vụ Client |
| Delivery flow | Thịnh Nguyễn | Auth landing/logout passed; cần người phụ trách xác nhận toàn bộ nghiệp vụ Delivery |
| Database complete v3 | Nhật Nam | Audit riêng: 12 test pass và build được khi override JDK; còn blocker JDK path cục bộ nên chưa đưa vào baseline |

## Việc còn chờ trước khi chốt sprint

- Nhận kết quả regression từ từng thành viên, Hiệp chỉ tổng hợp.
- Yêu cầu người phụ trách bỏ `org.gradle.java.home=C:/Program Files/Android/Android Studio/jbr`
  khỏi `Code/gradle.properties`; đây là đường dẫn máy cá nhân và sẽ làm hỏng build macOS/Linux.
- Khi `feature/database-complete-v3` được sửa, duyệt và merge vào `main`, pull lại rồi chạy toàn bộ lệnh kiểm tra trên thêm một lần.
