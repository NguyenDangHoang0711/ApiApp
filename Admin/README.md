# Admin Web Interface

Giao diện quản trị hệ thống cho Admin.

## Cài đặt và Sử dụng

### 1. Tạo tài khoản Admin mặc định

Trước khi sử dụng, bạn cần tạo tài khoản Admin mặc định:

```bash
cd ApiApp
npm run create-admin
```

Hoặc chạy trực tiếp:
```bash
cd ApiApp
node scripts/createDefaultAdmin.js
```

### 2. Thông tin đăng nhập mặc định

Sau khi chạy script, bạn sẽ có tài khoản Admin với thông tin:

- **Email**: `admin@example.com`
- **Password**: `admin123`

### 3. Khởi động API Server

```bash
cd ApiApp
npm start
# hoặc
npm run dev  # (với nodemon để auto-reload)
```

### 4. Mở Admin Interface

Mở file `index.html` trong trình duyệt hoặc sử dụng local server:

```bash
# Sử dụng Python
python -m http.server 8000

# Hoặc sử dụng Node.js http-server
npx http-server -p 8000
```

Sau đó truy cập: `http://localhost:8000`

### 5. Cấu hình API URL

Nếu API server chạy trên port khác hoặc domain khác, sửa file `admin.js`:

```javascript
const API_BASE_URL = 'http://localhost:3000'; // Thay đổi theo server của bạn
```

## Chức năng

- **Quản lý khách hàng**: Xem, thêm, sửa, xóa khách hàng
- **Quản lý sản phẩm**: Xem, thêm, sửa, xóa sản phẩm
- **Theo dõi hoạt động**: Xem và xử lý các báo cáo

## Lưu ý

- Đảm bảo API server đang chạy trước khi sử dụng Admin interface
- Token được lưu trong localStorage, sẽ tự động logout nếu token hết hạn
- Tất cả các thao tác đều được ghi vào MongoDB thông qua API

