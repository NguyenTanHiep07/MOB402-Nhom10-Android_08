#!/bin/bash
set -e

echo "Bắt đầu quy hoạch lại cấu trúc Mobile App..."

# 1. Tạo thư mục Frontend và di chuyển Android app
cd "Code"
mkdir -p Frontend

# Di chuyển các file/thư mục của Android (trừ Backend)
for item in app gradle build.gradle.kts settings.gradle.kts gradle.properties local.properties gradlew gradlew.bat .gitignore; do
    if [ -e "$item" ]; then
        mv "$item" "Frontend/"
    fi
done

echo "=> Đã di chuyển source code Android vào Code/Frontend/."

# 2. Quy hoạch chức năng client
cd Frontend/app/src/main/java/com/mob10/deliveryapp

mkdir -p ui/customer

FILES_TO_MOVE=(
    "ClientHomeScreen.kt"
    "CreateRequestScreen.kt"
    "CreateRequestViewModel.kt"
    "OrderConfirmationScreen.kt"
    "OrderTrackingScreen.kt"
    "OrderViewModel.kt"
    "FeeCalculatorEngine.kt"
)

for file in "${FILES_TO_MOVE[@]}"; do
    if [ -f "$file" ]; then
        mv "$file" "ui/customer/"
        # Cập nhật package name
        sed -i 's/^package com\.mob10\.deliveryapp$/package com.mob10.deliveryapp.ui.customer/' "ui/customer/$file"
    fi
done

echo "=> Đã gom các file chức năng client vào package ui.customer."

# 3. Cập nhật import trong toàn bộ source code
# Quay lại thư mục gốc của java files
cd ../../../../../../../.. 

find Code/Frontend/app/src/main/java -name "*.kt" -type f -exec sed -i \
  -e 's/import com\.mob10\.deliveryapp\.ClientHomeScreen/import com.mob10.deliveryapp.ui.customer.ClientHomeScreen/g' \
  -e 's/import com\.mob10\.deliveryapp\.CreateRequestScreen/import com.mob10.deliveryapp.ui.customer.CreateRequestScreen/g' \
  -e 's/import com\.mob10\.deliveryapp\.CreateRequestViewModel/import com.mob10.deliveryapp.ui.customer.CreateRequestViewModel/g' \
  -e 's/import com\.mob10\.deliveryapp\.CreateRequestViewModelFactory/import com.mob10.deliveryapp.ui.customer.CreateRequestViewModelFactory/g' \
  -e 's/import com\.mob10\.deliveryapp\.OrderConfirmationScreen/import com.mob10.deliveryapp.ui.customer.OrderConfirmationScreen/g' \
  -e 's/import com\.mob10\.deliveryapp\.OrderTrackingScreen/import com.mob10.deliveryapp.ui.customer.OrderTrackingScreen/g' \
  -e 's/import com\.mob10\.deliveryapp\.OrderViewModel/import com.mob10.deliveryapp.ui.customer.OrderViewModel/g' \
  -e 's/import com\.mob10\.deliveryapp\.OrderViewModelFactory/import com.mob10.deliveryapp.ui.customer.OrderViewModelFactory/g' \
  -e 's/import com\.mob10\.deliveryapp\.FeeCalculatorEngine/import com.mob10.deliveryapp.ui.customer.FeeCalculatorEngine/g' {} +

echo "=> Đã cập nhật xong tất cả các import liên quan."
echo "✅ HOÀN TẤT! Bạn có thể mở thư mục Code/Frontend trong Android Studio để tiếp tục làm việc."
