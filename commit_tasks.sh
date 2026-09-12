#!/bin/bash

echo "Tiến hành tạo commit riêng lẻ cho từng công việc..."

# 1. Add toàn bộ thay đổi ở thư mục Code
git add -A Code/

# 2. Gỡ (Unstage) các thư mục thuộc về task Gom nhóm và Fix lỗi ra khỏi commit đầu tiên
git reset HEAD Code/Frontend/app/src/main/java/com/mob10/deliveryapp/ui/customer/
git reset HEAD Code/Frontend/app/src/main/java/com/mob10/deliveryapp/data/remote/dto/
git reset HEAD Code/Frontend/app/src/main/java/com/mob10/deliveryapp/ui/admin/
git reset HEAD Code/Frontend/app/src/main/java/com/mob10/deliveryapp/ui/driver/

# 3. Commit 1: Task di chuyển Frontend
git commit -m "Thịnh: Gộp toàn bộ source code Android vào thư mục Code/Frontend"

# 4. Commit 2: Task Gom chức năng Client
git add Code/Frontend/app/src/main/java/com/mob10/deliveryapp/ui/customer/
git commit -m "Thịnh: Gom nhóm các màn hình và logic của Client vào package ui.customer"

# 5. Commit 3: Task Fix lỗi Compile
git add Code/Frontend/app/src/main/java/com/mob10/deliveryapp/data/remote/dto/
git add Code/Frontend/app/src/main/java/com/mob10/deliveryapp/ui/admin/
git add Code/Frontend/app/src/main/java/com/mob10/deliveryapp/ui/driver/
git commit -m "Thịnh: Sửa lỗi compile (trùng RegisterRequest) và cập nhật đường dẫn import"

# 6. Commit các file còn sót lại (nếu có)
git add .
git commit -m "Thịnh: Cập nhật các thay đổi liên quan khác trong project" || true

echo ""
echo "✅ Đã chia và commit thành công từng công việc. Bạn có thể dùng lệnh 'git log -n 4' để kiểm tra lại!"
