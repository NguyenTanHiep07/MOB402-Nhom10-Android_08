# Android_08 — Delivery App

## 1. Thông tin dự án
- Course Code: MOB402
- Group Code: Nhom10
- Project Code: Android_08
- Repository: MOB402-Nhom10-Android_08

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
- Đăng nhập
- Xem danh sách đơn được giao
- Xem chi tiết đơn
- Chấp nhận đơn
- Cập nhật các trạng thái:
  + Đã lấy hàng
  + Đang giao hàng
  + Đã giao hàng
- Xem lịch sử giao hàng

## 6. Chức năng mở rộng của Quản trị viên
- Đăng nhập
- Xem toàn bộ yêu cầu giao hàng
- Quản lý người dùng
- Xem lịch sử xử lý đơn
- Phân công lại đơn khi cần

## 7. Luồng trạng thái
Chờ tiếp nhận → Đã chấp nhận → Đã lấy hàng → Đang giao hàng → Đã giao hàng

Luồng hủy:
- Chờ tiếp nhận → Đã hủy
- Đã chấp nhận → Đã hủy

## 8. Kiến trúc dự kiến
- Presentation Layer
- ViewModel
- Domain/Business Logic
- Repository
- Local hoặc Remote Data Source

## 9. Cấu trúc repository
- **Code**: Chứa toàn bộ source code Android Studio của dự án.
- **DOCX**: Chứa báo cáo tài liệu đặc tả và các văn bản báo cáo.
- **Extra**: Chứa các sơ đồ thiết kế (Use Case, Activity, State, ERD, v.v.), minh chứng kiểm thử, hình ảnh và video liên quan đến dự án.
- **PPTX**: Chứa file trình chiếu thuyết trình (PowerPoint).

## 10. Danh sách thành viên và Phân công công việc
| STT | Họ và tên | MSSV | Công việc phụ trách |
|---|---|---|---|
| 1 | Nguyễn Tấn Hiệp | 087205010642 | Use Case <br> Core, Stitch UI, đăng nhập và điều hướng |
| 2 | Nguyễn Lâm Hữu Hùng | 079205019508 | Activity Diagram <br> Giao diện và chức năng Khách hàng |
| 3 | Huỳnh Nhật Nam | 080206015277 | State Diagram và ERD <br> Room Database và tầng dữ liệu |
| 4 | Nguyễn Quốc Thịnh | 052206007772 | Screen Flow và Architecture <br> Giao diện và chức năng Nhân viên giao hàng |

## 11. Yêu cầu môi trường
- Android Studio: Iguana / Koala (hoặc mới hơn)
- JDK: Java 11 (JavaVersion.VERSION_11)
- Android SDK: API 36 (Min SDK 24)
- Giao diện: Jetpack Compose (Material 3)

## 12. Hướng dẫn chạy
*Lưu ý: Source code Android chưa được thêm vào repository. Hướng dẫn chi tiết sẽ cập nhật sau khi source code được upload.*

## 13. Video demo
Chưa cập nhật.

## 14. Quy tắc commit
- Mỗi thành viên commit bằng tài khoản riêng.
- Commit message phải mô tả rõ thay đổi.
- Không sử dụng các commit message như update, fix, final hoặc done.
