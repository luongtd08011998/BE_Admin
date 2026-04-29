Anh/chị BE vui lòng triển khai API cho tính năng Phản ánh dịch vụ để mobile app (KMP Compose) có thể:

1) Luồng nghiệp vụ cần hỗ trợ
Người dùng chọn Loại vấn đề (1 trong các giá trị): leak, quality, pressure, outage, billing, meter, other
Nhập Địa điểm (text)
Nhập Mô tả chi tiết (text)
Đính kèm tối đa 5 ảnh (tùy chọn)
Bấm Gửi phản ánh → app hiển thị màn “Gửi thành công” và mã số theo dõi dạng PHxxxxx (hoặc format BE cấp, miễn là trả về string).