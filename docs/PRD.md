1. Bài toán (Problem Statement)
Tôi cần một nền tảng blog cá nhân đơn giản để tự viết, quản lý và xuất bản các bài viết, chia sẻ kiến thức của mình lên internet. Thay vì sử dụng các nền tảng có sẵn (như WordPress, Medium) hoặc thiết lập các hệ cơ sở dữ liệu phức tạp, dự án này hướng tới sự tối giản: hệ thống lưu trữ bài viết trực tiếp dưới dạng các tệp tin (files) trên ổ cứng (như Markdown hoặc JSON). Điều này giúp dự án nhẹ nhàng, dễ triển khai, đồng thời là cơ hội tuyệt vời để thực hành xây dựng ứng dụng Server-Side Rendering bằng Spring Boot.

2. Người dùng (User Roles)
Hệ thống chia làm 2 nhóm người dùng chính:

Guest (Độc giả): Bất kỳ ai truy cập vào trang web từ internet. Họ có quyền xem danh sách bài viết và đọc nội dung chi tiết. Không cần tài khoản.

Admin (Tác giả/Chủ blog): Là chính bạn. Người duy nhất có quyền truy cập vào khu vực quản trị (Dashboard) để thêm, sửa, xóa các bài viết. Yêu cầu phải đăng nhập thành công.

3. User Stories & Tiêu chí chấp nhận (Acceptance Criteria)
Dưới đây là 7 User Story cốt lõi bám sát yêu cầu dự án.

Epic 1: Trải nghiệm Độc giả (Guest Section)
User Story 1: Xem danh sách bài viết ở Trang chủ
Là một Độc giả (Guest),
Tôi muốn nhìn thấy danh sách các bài viết đã xuất bản trên Trang chủ (Home Page),
Để tôi có thể biết blog có những nội dung gì và chọn bài để đọc.

Tiêu chí chấp nhận (Acceptance Criteria):

Hệ thống đọc các file từ thư mục lưu trữ và hiển thị danh sách bài viết.

Mỗi mục trong danh sách hiển thị: Tiêu đề bài viết và Ngày xuất bản.

Tiêu đề bài viết có thể click vào và sẽ điều hướng sang trang chi tiết của bài đó.

Nếu không có bài viết nào, hiển thị thông báo: "Chưa có bài viết nào được xuất bản."

User Story 2: Đọc chi tiết bài viết
Là một Độc giả,
Tôi muốn xem nội dung chi tiết của một bài viết cụ thể,
Để tôi có thể đọc toàn bộ thông tin mà tác giả chia sẻ.

Tiêu chí chấp nhận:

Trang bài viết (/article/{id}) hiển thị đầy đủ: Tiêu đề lớn, Ngày xuất bản và Nội dung bài viết.

Nếu nội dung được lưu bằng Markdown, hệ thống phải render đúng ra các thẻ HTML tương ứng (Heading, in đậm, list, code block...).

Nếu đường dẫn bài viết (ID/Tên file) không tồn tại, hiển thị trang lỗi 404 (Not Found).

Epic 2: Xác thực & Bảo mật (Authentication)
User Story 3: Đăng nhập vào trang Quản trị
Là một Admin,
Tôi muốn đăng nhập vào hệ thống bằng tài khoản và mật khẩu,
Để truy cập vào khu vực Dashboard quản lý blog.

Tiêu chí chấp nhận:

Có một trang Login chứa form điền Username và Password.

Khi nhập đúng thông tin (có thể hardcode trong Spring Security), hệ thống chuyển hướng vào trang Dashboard.

Khi nhập sai thông tin, hiển thị dòng thông báo lỗi "Sai tài khoản hoặc mật khẩu".

Bất kỳ ai cố gắng truy cập đường dẫn /admin/** mà chưa đăng nhập đều bị đẩy ngược về trang Login.

User Story 4: Đăng xuất khỏi hệ thống
Là một Admin,
Tôi muốn có nút Đăng xuất (Logout),
Để kết thúc phiên làm việc và bảo vệ khu vực quản trị.

Tiêu chí chấp nhận:

Nút Đăng xuất hiển thị rõ ràng khi Admin đang ở trạng thái đã đăng nhập.

Click vào Đăng xuất, hệ thống hủy Session và điều hướng người dùng về Trang chủ (Home Page) hoặc Trang Login.

Epic 3: Quản lý bài viết (Admin Section)
User Story 5: Xem Dashboard Quản lý
Là một Admin,
Tôi muốn xem một bảng danh sách tất cả các bài viết trong Dashboard,
Để có cái nhìn tổng quan và quản lý nội dung của mình.

Tiêu chí chấp nhận:

Giao diện Dashboard chỉ truy cập được bởi Admin.

Hiển thị dạng bảng (Table) các bài viết gồm: Tiêu đề, Ngày xuất bản và Cột Hành động.

Có một nút "Add New Article" nổi bật ở trên cùng.

Cột Hành động chứa 2 nút: "Edit" và "Delete" cho từng bài viết.

User Story 6: Thêm bài viết mới
Là một Admin,
Tôi muốn tạo một bài viết mới thông qua một Form điền liệu,
Để xuất bản nội dung mới lên trang chủ.

Tiêu chí chấp nhận:

Click "Add New Article" mở ra trang có form gồm các trường: Tiêu đề (Input text), Ngày xuất bản (Date picker hoặc nhập text), Nội dung (Textarea rộng).

Khi submit form thành công, backend sẽ tạo ra một file mới (ví dụ .md hoặc .json) lưu vào thư mục lưu trữ của hệ thống.

Sau khi lưu file thành công, tự động chuyển hướng về trang Dashboard và bài viết mới xuất hiện ở đầu danh sách.

Nếu để trống tiêu đề hoặc nội dung, hệ thống báo lỗi không cho lưu.

User Story 7: Chỉnh sửa bài viết
Là một Admin,
Tôi muốn chỉnh sửa lại tiêu đề hoặc nội dung của một bài viết đã có,
Để cập nhật thông tin mới hoặc sửa lỗi chính tả.

Tiêu chí chấp nhận:

Click nút "Edit" tại Dashboard sẽ mở ra trang form sửa bài viết.

Các trường (Tiêu đề, Ngày, Nội dung) phải được tự động điền sẵn (pre-filled) dữ liệu hiện tại của bài viết đó.

Khi submit, backend sẽ ghi đè nội dung mới lên file cũ lưu trên ổ cứng.

Sau khi cập nhật xong, chuyển hướng về Dashboard.

User Story 8: Xóa bài viết
Là một Admin,
Tôi muốn xóa một bài viết khỏi hệ thống,
Để dọn dẹp các nội dung không còn phù hợp.

Tiêu chí chấp nhận:

Click nút "Delete" tại Dashboard sẽ kích hoạt hành động xóa.

(Tùy chọn) Hiển thị hộp thoại xác nhận (Confirm alert) "Bạn có chắc chắn muốn xóa bài viết này không?" trước khi xóa.

Backend thực hiện xóa vĩnh viễn file tương ứng khỏi ổ cứng.

Dashboard tự động tải lại (reload) và bài viết đó biến mất khỏi danh sách.