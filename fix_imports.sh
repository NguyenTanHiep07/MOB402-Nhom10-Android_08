#!/bin/bash
set -e

echo "Tiến hành sửa lỗi import trong các file..."

cd "Code/Frontend/app/src/main/java"

find . -name "*.kt" -type f -exec sed -i \
  -e 's/import com\.mob10\.deliveryapp\.ClientHomeScreen/import com.mob10.deliveryapp.ui.customer.ClientHomeScreen/g' \
  -e 's/import com\.mob10\.deliveryapp\.CreateRequestScreen/import com.mob10.deliveryapp.ui.customer.CreateRequestScreen/g' \
  -e 's/import com\.mob10\.deliveryapp\.CreateRequestViewModel/import com.mob10.deliveryapp.ui.customer.CreateRequestViewModel/g' \
  -e 's/import com\.mob10\.deliveryapp\.CreateRequestViewModelFactory/import com.mob10.deliveryapp.ui.customer.CreateRequestViewModelFactory/g' \
  -e 's/import com\.mob10\.deliveryapp\.OrderConfirmationScreen/import com.mob10.deliveryapp.ui.customer.OrderConfirmationScreen/g' \
  -e 's/import com\.mob10\.deliveryapp\.OrderTrackingScreen/import com.mob10.deliveryapp.ui.customer.OrderTrackingScreen/g' \
  -e 's/import com\.mob10\.deliveryapp\.OrderViewModel/import com.mob10.deliveryapp.ui.customer.OrderViewModel/g' \
  -e 's/import com\.mob10\.deliveryapp\.OrderViewModelFactory/import com.mob10.deliveryapp.ui.customer.OrderViewModelFactory/g' \
  -e 's/import com\.mob10\.deliveryapp\.FeeCalculatorEngine/import com.mob10.deliveryapp.ui.customer.FeeCalculatorEngine/g' {} +

echo "✅ Đã sửa import thành công!"
