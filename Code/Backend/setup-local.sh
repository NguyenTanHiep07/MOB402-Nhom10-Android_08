#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [ -e .env ]; then
  echo "Đã có Code/Backend/.env; giữ nguyên cấu hình."
  exit 0
fi
umask 077
jwt_value=$(openssl rand -hex 32)
demo_value=$(openssl rand -hex 8)
cat > .env <<EOF
MONGODB_URI=mongodb://localhost:27017/delivery_db?replicaSet=rs0
JWT_SECRET=$jwt_value
DEMO_ENABLED=false
DEMO_PASSWORD=$demo_value
CORS_ALLOWED_ORIGINS=http://localhost:3000
EOF
echo "Đã tạo Backend/.env cho MongoDB local; mặc định không seed demo."
echo "Chỉ bật DEMO_ENABLED=true trên cơ sở dữ liệu riêng dành cho demo."
