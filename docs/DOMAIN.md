1. Domain Model (Các thực thể & Quy tắc nghiệp vụ)
Do tính chất tối giản của dự án và việc không sử dụng cơ sở dữ liệu quan hệ (RDBMS), hệ thống chỉ có một thực thể (Entity) cốt lõi duy nhất.

Thực thể: Article (Bài viết)
Đại diện cho một bài blog được lưu trữ dưới dạng một tệp tin (file) trên ổ cứng.

Các thuộc tính (Attributes):

id (String): Định danh duy nhất của bài viết. Thường được sử dụng để làm tên file lưu trên ổ cứng (ví dụ: uuid-v4, hoặc slug tạo từ tiêu đề).

title (String): Tiêu đề hiển thị của bài viết.

content (String): Nội dung chi tiết của bài viết, được lưu trữ dưới định dạng Markdown (hoặc plain text).

publishedDate (LocalDate / String): Ngày xuất bản bài viết. Có thể kèm giờ nếu cần.

Quy tắc nghiệp vụ bất biến (Invariants):

id phải là duy nhất, không được chứa các ký tự đặc biệt gây lỗi khi tạo tên file trên hệ điều hành (chỉ nên dùng chữ, số, dấu gạch ngang -).

title và content không được phép để trống (null hoặc empty) khi lưu mới hoặc cập nhật.

Một Article tương ứng với đúng một tệp tin duy nhất trên hệ thống lưu trữ (ổ cứng server).

(Lưu ý: Không có thực thể User lưu trong hệ thống file, thông tin Admin dùng để đăng nhập sẽ được cấu hình trực tiếp trong code/properties).

2. Ràng buộc Kỹ thuật (Technical Constraints)
Dự án phải tuân thủ nghiêm ngặt các công nghệ và cấu hình sau:

Ngôn ngữ lập trình: Java 21.

Web Framework: Spring Boot 3.3.x (hoặc bản 3.x mới nhất).

Kiến trúc: Monolith, Server-Side Rendering (SSR). Không phân tách Frontend/Backend API.

Giao diện (Frontend):

Template Engine: Thymeleaf (Render HTML từ server).

Styling: CSS thuần hoặc thư viện CSS nhẹ (như Bootstrap, Tailwind qua CDN). Không dùng Framework JS (React, Vue, Angular).

Lưu trữ dữ liệu (Database): TUYỆT ĐỐI KHÔNG DÙNG DATABASE. Toàn bộ Article phải được lưu dưới dạng file .md hoặc .json trong một thư mục cục bộ của server (ví dụ: thư mục ./data/articles/). Không dùng SQL, JPA, Hibernate.

Xác thực (Authentication): Sử dụng spring-boot-starter-security. Triển khai Form Login mặc định (dùng Session & Cookie). Thông tin Admin (Username/Password) được cấu hình cứng (hardcoded) trong file application.properties hoặc InMemoryUserDetailsManager.

Môi trường Deploy: Đóng gói thành tệp thực thi JAR (java -jar blog.jar). Có thể chạy trực tiếp trên máy chủ ảo (VPS) hoặc local.

3. Yêu cầu Phi chức năng (Non-Functional Requirements)
Bảo mật (Security):

Toàn bộ các endpoint bắt đầu bằng /admin/** bắt buộc phải có phiên đăng nhập hợp lệ.

Path Traversal Prevention: Khi đọc, sửa hoặc xóa file dựa trên id từ client gửi lên, code backend phải chặn các ký tự như ../ hoặc ..\ để ngăn hacker đọc các file hệ thống ngoài thư mục quy định.

Hiệu năng (Performance):

Thời gian phản hồi (TTFB) để render một trang chứa bài viết (khi không chịu tải) phải dưới 200ms.

Do đọc file trực tiếp từ ổ cứng có thể chậm nếu file quá nhiều, hệ thống nên có cơ chế Cache in-memory đơn giản cho danh sách bài viết trên Trang chủ nếu cần thiết.

Quy mô (Scalability & Load): Phục vụ 1 Admin thao tác. Phục vụ số lượng người đọc nhỏ (dưới 100 người dùng đồng thời).

Đồng thời (Concurrency): Khi Admin đang ghi (sửa) file, cần đảm bảo không gây lỗi nếu có người đọc truy cập đúng lúc đó (đảm bảo file I/O thread-safe cơ bản).

4. Những gì Không Làm (Out of Scope)
Đây là ranh giới của dự án. Không phát triển thêm các tính năng dưới đây để tránh "Feature Creep" (phình to dự án):

Không Setup Database: Không cài đặt, cấu hình hay kết nối tới MySQL, PostgreSQL, MongoDB, SQLite hay bất kỳ hệ quản trị CSDL nào.

Không có API (REST/GraphQL): Không xây dựng các endpoint trả về dữ liệu JSON cho các ứng dụng bên thứ 3 hoặc Frontend SPA (Single Page Application). Mọi thứ trả về trực tiếp là HTML.

Không Đăng ký/Quản lý Người dùng: Không có tính năng cho phép độc giả tạo tài khoản. Chỉ có duy nhất 1 tài khoản Admin tĩnh.

Không Bình luận (Comments): Không cho phép người dùng để lại bình luận dưới bài viết.

Không Phân loại (Categories/Tags): Bài viết chỉ có duy nhất các thuộc tính cơ bản. Không tạo hệ thống quản lý danh mục hay thẻ (tags).

Không Tìm kiếm (Search): Không làm ô tìm kiếm bài viết trên trang chủ.

Không Xử lý Hình ảnh (Image Upload): Trình soạn thảo (Admin) không hỗ trợ tính năng upload ảnh lên server. Nếu muốn chèn ảnh vào bài viết, Admin phải sử dụng link ảnh từ một host bên ngoài (như Imgur) chèn vào cú pháp Markdown.

Không có Trình soạn thảo WYSIWYG: Tại trang Thêm/Sửa bài viết, không tích hợp các bộ gõ phức tạp (như CKEditor, TinyMCE). Chỉ sử dụng thẻ <textarea> HTML thông thường để Admin tự gõ text (Markdown).