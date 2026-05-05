# Media Feature Context

## Purpose
Thư viện Media dùng chung cho toàn hệ thống — upload, quản lý hình ảnh và tài liệu (pdf, doc, docx).

## Architecture
- Tái dùng `FileService.upload(file, "media")` cho lưu file vật lý (validate extension, size, folder)
- Metadata lưu vào bảng `media` qua `MediaRepository`
- File vật lý lưu tại `./uploads/media/` (serve qua `/uploads/**` static resource)

## Endpoints
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/v1/media | JWT + UPLOAD_MEDIA | Upload media (multipart) |
| GET | /api/v1/media | Public | List media (paginated, filter by title/fileType) |
| GET | /api/v1/media/{id} | Public | Get media by ID |
| DELETE | /api/v1/media/{id} | JWT + DELETE_MEDIA | Xóa file vật lý + DB record |

## Security
- GET endpoints: public (`permitAll` trong SecurityConfig)
- POST/DELETE: qua `PermissionAuthorizationManager` — cần permission trong DB
- 4 permissions: UPLOAD_MEDIA, VIEW_MEDIA, VIEW_MEDIA_ITEM, DELETE_MEDIA

## File Upload Config
- Max size: 10MB (`spring.servlet.multipart.max-file-size` + `app.upload.max-file-size`)
- Allowed extensions: jpg, jpeg, png, gif, webp, pdf, doc, docx
- Folder: `media` (đã thêm vào `app.upload.allowed-folders`)

## Key Decisions
- `uploadedBy` lưu Long userId (lấy từ JWT claim) — không phải entity reference
- `title` nullable: nếu không cung cấp thì dùng tên file gốc
- Delete xóa cả file vật lý (best-effort, log warning nếu fail) và DB record
