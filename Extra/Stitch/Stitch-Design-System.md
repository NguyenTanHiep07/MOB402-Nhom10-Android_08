# GoDrop Stitch Design System

Phiên bản: 1.0 — Android_08  
Phạm vi: Login, navigation shell và Home khung của CLIENT, DELIVERY, ADMIN.

## 1. Nguyên tắc

- Một màu tím chàm làm nhận diện chính; màu cam chỉ dùng cho điểm nhấn và cảnh báo.
- Các màn hình dùng cùng typography, khoảng cách, bo góc và trạng thái tương tác.
- Nội dung nghiệp vụ của từng role được đặt trong cùng một `DashboardScaffold`.
- Thành phần phải đọc được ở chiều rộng từ 360 dp và hỗ trợ cuộn dọc.

## 2. Màu sắc

| Token | Hex | Mục đích |
|---|---:|---|
| `Primary` | `#4F46E5` | Nút chính, icon đang chọn, liên kết |
| `PrimaryDark` | `#3730A3` | Gradient header, trạng thái nhấn |
| `PrimaryContainer` | `#EEF2FF` | Nền icon, vùng nhấn nhẹ |
| `Secondary` | `#F97316` | Hành động/phân loại phụ |
| `SecondaryContainer` | `#FFF1E8` | Nền điểm nhấn phụ |
| `Background` | `#F7F8FC` | Nền màn hình |
| `Surface` | `#FFFFFF` | Card, input |
| `OnSurface` | `#172033` | Nội dung chính |
| `OnSurfaceVariant` | `#667085` | Mô tả, nhãn phụ |
| `Outline` | `#98A2B3` | Viền input |
| `Success` | `#16A34A` | Hoàn tất, trực tuyến |
| `Warning` | `#F59E0B` | Đơn cần xử lý |
| `Error` | `#BA1A1A` | Lỗi nhập liệu/xác thực |

Nguồn Compose nằm tại `Color.kt`; nguồn XML nằm tại `res/values/colors.xml`.

## 3. Typography

Font chuẩn: **Roboto / Android Sans Serif**. Không trộn thêm font trong màn hình role.

| Style | Size/line height | Weight | Dùng cho |
|---|---:|---:|---|
| Display Small | 32/38 sp | Extra Bold | Tên thương hiệu |
| Headline Small | 24/30 sp | Bold | Tiêu đề màn hình |
| Title Large | 20/26 sp | Bold | Tiêu đề section lớn |
| Title Medium | 16/22 sp | Semi Bold | Card và dialog |
| Body Large | 16/24 sp | Regular | Nội dung chính |
| Body Medium | 14/20 sp | Regular | Nội dung card |
| Label Large | 14/20 sp | Bold | Button |
| Label Medium | 12/16 sp | Medium | Nhãn phụ/navigation |

Khai báo thực thi nằm tại `ui/theme/Type.kt`.

## 4. Khoảng cách và hình dạng

- Grid cơ sở: `4 dp`.
- Lề ngang màn hình: `20 dp`.
- Khoảng cách section: `16 dp`.
- Khoảng cách input: `14 dp`.
- Chiều cao button chính: `53 dp`.
- Bo input/button: `14–15 dp`.
- Bo action card: `17 dp`.
- Bo metric card: `21 dp`.
- Bo login card: `28 dp`.
- Bo đáy role header: `32 dp`.

Shape token được khai báo tại `ui/theme/Shape.kt`.

## 5. Component chuẩn

### Primary button

- Nền `Primary`, chữ trắng, cao 53 dp, bo 15 dp.
- Disabled khi dữ liệu khởi tạo hoặc thao tác đang chạy.
- Không dùng chữ in hoa toàn bộ.

### Input

- Outlined input, nền trắng, bo 14 dp.
- Viền focus `Primary`, viền bình thường `Outline`.
- Luôn có label; số điện thoại dùng keyboard `Phone`, mật khẩu có nút ẩn/hiện.

### Header và navigation

- Role header cao 228 dp, gradient `PrimaryDark → Primary`.
- Luôn hiển thị thương hiệu, role, tên người dùng và nút đăng xuất.
- Bottom navigation có 4 mục; item chọn dùng `PrimaryContainer`.

### Card

- Nền `Surface`, viền `OutlineVariant`, elevation 0–4 dp.
- Mỗi card có một mục tiêu; icon không thay thế cho nhãn chữ.

## 6. Accessibility và nội dung

- Vùng bấm tối thiểu 48 dp.
- Icon có hành động phải có `contentDescription`.
- Không dùng màu sắc làm tín hiệu duy nhất; trạng thái luôn có nhãn chữ.
- Thông báo đăng nhập không tiết lộ số điện thoại hay mật khẩu nào sai.
- Chuỗi hiển thị dùng resource khi dùng lại hoặc cần bản địa hóa.

## 7. Quy tắc đồng bộ

Khi thay đổi token trong Stitch, phải cập nhật cả `Color.kt`, `Type.kt`, `Shape.kt` và
XML resources trong cùng pull request. Không tạo màu/font cục bộ mới nếu đã có token
tương đương trong tài liệu này.
