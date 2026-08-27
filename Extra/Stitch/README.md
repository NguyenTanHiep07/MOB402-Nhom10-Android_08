# GoDrop Stitch handoff

Thư mục này là nguồn chuẩn cho phần thiết kế thuộc `feature/auth-navigation`.
Nó giúp đồng bộ giao diện Android với project Stitch và cung cấp đầu vào để tái tạo
prototype khi project Stitch được chuyển giữa các thành viên.

## Thành phần

- [Stitch Design System](./Stitch-Design-System.md): màu sắc, typography, khoảng cách,
  bo góc và quy tắc component.
- [Prototype tổng](./Prototype-Overview.md): danh sách màn hình, luồng điều hướng và
  tiêu chí nghiệm thu.
- [Prompt dùng cho Stitch](./Stitch-Prompt.md): prompt chuẩn để tạo hoặc cập nhật
  project GoDrop trong Google Stitch.
- Bản xuất sơ đồ sẵn để chèn báo cáo:
  - `Extra/Diagrams/Architecture/Architecture-Auth-Navigation.png`
  - `Extra/Diagrams/ScreenFlow/SF-00-Auth-Role-Navigation.png`
  - `Extra/Diagrams/UseCase/UC-AUTH-01-Dang-Nhap.png`

## Nguồn code tương ứng

- Compose theme: `Code/app/src/main/java/com/mob10/deliveryapp/ui/theme/`
- Component dùng chung: `Code/app/src/main/java/com/mob10/deliveryapp/ui/components/GoDropComponents.kt`
- Login XML chạy chính: `Code/app/src/main/res/layout/screen_login.xml`
- Cầu nối Compose/XML: `Code/app/src/main/java/com/mob10/deliveryapp/ui/auth/XmlLoginScreen.kt`
- Login Compose tham chiếu/preview: `Code/app/src/main/java/com/mob10/deliveryapp/ui/auth/LoginScreen.kt`
- Điều hướng role: `Code/app/src/main/java/com/mob10/deliveryapp/ui/navigation/AppDestination.kt`

## Liên kết project Stitch

Repository chưa chứa URL của project Stitch bên ngoài. Chủ project cần dán URL chia sẻ
vào mục này trước khi nộp để giảng viên có thể mở prototype trực tiếp. Không lưu tài
khoản, cookie hoặc thông tin đăng nhập trong repository.
