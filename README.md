# Android_08 — Delivery App

## 1. Thông tin dự án
- Course Code: MOB402
- Group Code: Nhom10
- Project Code: Android_08
- Repository: MOB402-Nhom10-Android-08

## 2. Giới thiệu
Ứng dụng Android hỗ trợ tạo, phân công, theo dõi và cập nhật yêu cầu giao hàng.

## 3. Các vai trò
- Khách hàng
- Nhân viên giao hàng
- Quản trị viên — chức năng mở rộng của nhóm

## 4. Chức năng chính của Khách hàng
- Đăng nhập
- Tạo yêu cầu giao hàng
- Nhập địa chỉ lấy hàng và giao hàng
- Nhập thông tin kiện hàng
- Nhập thông tin người gửi và người nhận
- Tính và hiển thị phí giao hàng
- Xác nhận hoặc hủy yêu cầu trước khi lấy hàng
- Xem danh sách đơn
- Xem trạng thái hiện tại
- Xem lịch sử trạng thái

## 5. Chức năng chính của Nhân viên giao hàng
- Xem đơn chờ từ Room.
- Nhận đơn.
- Xem đơn đang giao.
- Cập nhật trạng thái.
- Xem lịch sử giao hàng.
- Xem hồ sơ và đăng xuất.

## 6. Chức năng mở rộng của Quản trị viên
- Đăng nhập
- Xem toàn bộ yêu cầu giao hàng
- Quản lý người dùng
- Xem lịch sử xử lý đơn
- Phân công lại đơn khi cần

## 7. Luồng trạng thái
CHO_TIEP_NHAN → DA_CHAP_NHAN → DA_DEN_NHA_HANG → DA_LAY_HANG → DA_DEN_KHACH_HANG → DA_GIAO

Luồng hủy:
- CHO_TIEP_NHAN → DA_HUY
- DA_CHAP_NHAN → DA_HUY

## 8. Công nghệ sử dụng
- Kotlin.
- Jetpack Compose Material 3.
- XML Login được nhúng qua AndroidView.
- MVVM.
- Room Database.
- Kotlin Flow/StateFlow.
- KSP.
- Repository pattern.

## 9. Kiến trúc dự kiến
- Presentation Layer
- ViewModel
- Repository
- DAO
- Room Database

Sơ đồ đã đồng bộ với code: [Architecture — Auth & Navigation](./Extra/Diagrams/Architecture/Architecture-Auth-Navigation.md).

## 10. Cấu trúc repository
- **Code**: Chứa toàn bộ source code Android Studio của dự án.
- **DOCX**: Chứa báo cáo tài liệu đặc tả và các văn bản báo cáo.
- **Extra**: Chứa các sơ đồ thiết kế (Use Case, Activity, State, ERD, v.v.), minh chứng kiểm thử, hình ảnh và video liên quan đến dự án.
- **PPTX**: Chứa file trình chiếu thuyết trình (PowerPoint).

## 11. Danh sách thành viên và Phân công công việc
| STT | Họ và tên | MSSV | Công việc phụ trách |
|---|---|---|---|
| 1 | Nguyễn Tấn Hiệp | 087205010642 | Use Case <br> Core, Stitch UI, đăng nhập và điều hướng |
| 2 | Nguyễn Lâm Hữu Hùng | 079205019508 | Activity Diagram <br> Giao diện và chức năng Khách hàng |
| 3 | Huỳnh Nhật Nam | 080206015277 | State Diagram và ERD <br> Room Database và tầng dữ liệu |
| 4 | Nguyễn Quốc Thịnh | 052206007772 | Screen Flow và Architecture <br> Giao diện và chức năng Nhân viên giao hàng |

## 12. Yêu cầu môi trường
- Android Studio: Iguana / Koala (hoặc mới hơn)
- JDK chạy Gradle: JDK 17 từ Android Studio
- Java/Kotlin bytecode target: Java 11
- Android SDK: API 36 (Min SDK 24)
- Giao diện: Jetpack Compose (Material 3)

## 13. Hướng dẫn chạy
1. Mở thư mục `Code` bằng Android Studio.
2. Chọn Gradle JDK là JDK 17 đi kèm Android Studio.
3. Sync Gradle và chạy cấu hình `app` trên máy ảo hoặc thiết bị API 24 trở lên.
4. Đăng nhập bằng một tài khoản mẫu, mật khẩu chung `123456`:

| Role | Số điện thoại |
|---|---|
| CLIENT | `0123456789` hoặc `0987654321` |
| DELIVERY | `0111222333` hoặc `0444555666` |
| ADMIN | `0000000000` |

5. Hoặc chạy lệnh kiểm tra bằng terminal:
```bash
cd Code
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

## 14. Tài liệu tham khảo
- [Design System](./Extra/Stitch/README.md)
- [Use Case tổng quát](./Extra/Diagrams/UseCase/UC-Tong-Quat.md)
- [Screen Flow](./Extra/Diagrams/ScreenFlow/SF-Tong-Quan.md)
- [Architecture](./Extra/Diagrams/Architecture/Architecture-Auth-Navigation.md)
- [ERD](./Extra/Diagrams/ERD/ERD.md)
- [State Diagram](./Extra/Diagrams/State/State-Diagram.md)

## 15. Video demo
Chưa cập nhật.

## 16. Quy tắc commit
- Mỗi thành viên commit bằng tài khoản riêng.
- Commit message phải mô tả rõ thay đổi.
- Không sử dụng các commit message như update, fix, final hoặc done.
