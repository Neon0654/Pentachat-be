# 📖 Hướng dẫn sử dụng Swagger UI - PentaChat

Swagger UI là công cụ mạnh mẽ hỗ trợ việc trực quan hóa và tương tác với các API của hệ thống mà không cần cài đặt các công cụ bên ngoài như Postman.

## 1. Cách truy cập
Sau khi khởi chạy ứng dụng Spring Boot, bạn có thể truy cập Swagger UI qua đường dẫn:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

---

## 2. Các chức năng chính

### 🔍 Xem danh sách API
- Các API được nhóm theo **Controller** (như `UserController`, `FriendController`, ...).
- Nhấp vào từng Controller để xem danh sách các API (GET, POST, PUT, DELETE).

### 📄 Xem chi tiết Request/Response
- Mỗi API sẽ hiển thị chi tiết:
  - **Parameters**: Các tham số cần truyền (Query, Path).
  - **Request Body**: Cấu trúc JSON mẫu và mô tả các trường.
  - **Responses**: Ý nghĩa của các mã trả về (200, 201, 400, 404, 500).

---

## 3. Cách Test API trực tiếp

Để thực hiện test một API, hãy làm theo các bước sau:

1. **Chọn API**: Tìm đến API bạn muốn kiểm tra.
2. **Bắt đầu**: Nhấn nút **"Try it out"** ở góc phải của API đó.
3. **Nhập dữ liệu**:
   - Điền các tham số vào ô Parameter (nếu có).
   - Chỉnh sửa nội dung JSON trong phần **Request body** (nếu là POST/PUT).
4. **Thực thi**: Nhấn nút màu xanh **"Execute"**.
5. **Xem kết quả**:
   - **Curl**: Lệnh curl tương ứng để bạn có thể copy chạy ở terminal.
   - **Request URL**: URL thực tế đã được gọi.
   - **Server response**: Bao gồm **Code** (Mã trạng thái) và **Response body** (Dữ liệu trả về).

---

## 4. Xử lý Token/Session (Nếu có)

Hiện tại hệ thống đang được cấu hình `permitAll()`. Tuy nhiên, nếu sau này cần xác thực:
1. Nhấn nút **"Authorize"** ở trên cùng trang Swagger.
2. Nhập Token hoặc Session ID (nếu được yêu cầu).
3. Sau đó, tất cả các request test sẽ tự động đính kèm thông tin xác thực này.

---

## 🛠️ Lưu ý kỹ thuật
- Tài liệu API (JSON format) có thể lấy tại: `/v3/api-docs`
- Thư viện sử dụng: `springdoc-openapi-starter-webmvc-ui`

---
*Chúc bạn có trải nghiệm test API tuyệt vời với PentaChat!* 🚀
