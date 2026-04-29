GET /api/v1/qlkh/customer/notifications
Response JSON hiện tại

{
"statusCode": 200,
"message": "success",
"data": [
{
"id": 1,
"customerId": 123,
"title": "Thông báo nâng cấp đồng hồ nước",
"content": "Nội dung bài viết...",
"type": "GENERAL",
"isRead": false,
"createdAt": "2026-04-28T10:00:00",
"referenceId": 456,
"isSystem": false
}
]
}
BE chỉ cần thêm 1 trường url vào mỗi notification object:

{
"statusCode": 200,
"message": "success",
"data": [
{
"id": 1,
"customerId": 123,
"title": "Thông báo nâng cấp đồng hồ nước",
"content": "Nội dung bài viết...",
"type": "GENERAL",
"isRead": false,
"createdAt": "2026-04-28T10:00:00",
"referenceId": 456,
"isSystem": false,
"url": "https://capnuoctoctien.vn/bai-viet/thong-bao-nang-cap-dong-ho-nuoc"
}
]
}
