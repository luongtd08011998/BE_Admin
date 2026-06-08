# API Specification

> All endpoints return `ApiResponse<T>` wrapper.
> Update this file whenever endpoints change.

---

## Base URL

```
Development: http://localhost:8080/api/v1
Production:  https://api.example.com/api/v1
```

---

## Authentication

All endpoints require JWT in `Authorization: Bearer <accessToken>` header,
except those marked as **Public**.

---

## 1. Auth

### POST /auth/login

Login and receive tokens.

**Request Body:**

```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  },
  "message": "Login successful",
  "timestamp": "20xx-02-28T10:00:00"
}
```

**Also sets cookie:**

```
Set-Cookie: refresh_token=eyJ...;
            HttpOnly; Secure; SameSite=Lax;
            Path=/api/v1/auth; Max-Age=259200
```

**Errors:**
| Status | When |
|--------|------|
| 400 | Missing email or password |
| 401 | Invalid credentials |

---

### POST /auth/register

Register a new user account.

**Request Body:**

```json
{
  "name": "Nguyen Van A",
  "email": "user@example.com",
  "password": "password123",
  "age": 25,
  "gender": "MALE",
  "address": "Ho Chi Minh City"
}
```

**Success Response (201):**

```json
{
  "statusCode": 201,
  "data": {
    "id": 1,
    "name": "Nguyen Van A",
    "email": "user@example.com",
    "age": 25,
    "gender": "MALE",
    "address": "Ho Chi Minh City",
    "createdAt": "20xx-02-28T10:00:00Z"
  },
  "message": "User registered",
  "timestamp": "20xx-02-28T10:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed (blank name, invalid email, password < 8 chars) |
| 409 | Email already exists |

---

### POST /auth/refresh

Get new access token using refresh token.

**Sources (backend checks in order):**

1. Cookie `refresh_token` (SPA — browser sends automatically)
2. Request body `refreshToken` (Mobile — sends explicitly)

**Request Body (mobile only):**

```json
{
  "refreshToken": "eyJ..."
}
```

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "accessToken": "eyJ...(new)",
    "refreshToken": "eyJ...(new)"
  },
  "message": "Token refreshed",
  "timestamp": "20xx-02-28T10:15:00"
}
```

Also sets new `refresh_token` cookie (replaces old one).

**Errors:**
| Status | When |
|--------|------|
| 401 | No refresh token provided |
| 401 | Refresh token expired or revoked |

---

### POST /auth/logout

Invalidate refresh token and clear cookie.

**Request:** No body needed. Token taken from cookie or Authorization header.

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": null,
  "message": "Logged out",
  "timestamp": "20xx-02-28T11:00:00"
}
```

**Also clears cookie:**

```
Set-Cookie: refresh_token=; Max-Age=0; Path=/api/v1/auth
```

---

### GET /auth/me

Get current logged-in user info.

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "id": 1,
    "name": "Nguyen Van A",
    "email": "user@example.com",
    "age": 25,
    "gender": "MALE",
    "address": "Ho Chi Minh City",
    "avatar": null,
    "company": {
      "id": 1,
      "name": "HoiDanIT"
    },
    "roles": [{ "id": 1, "name": "ADMIN" }]
  },
  "message": "Success",
  "timestamp": "20xx-02-28T10:00:00"
}
```

---

## 2. Users

### GET /users 🔒

List all users with pagination.

**Query Parameters:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page | int | 1 | Page number (1-based) |
| size | int | 10 | Items per page |
| sort | string | id,asc | Sort field and direction |

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "meta": {
      "page": 1,
      "pageSize": 10,
      "pages": 5,
      "total": 50
    },
    "result": [
      {
        "id": 1,
        "name": "Nguyen Van A",
        "email": "user@example.com",
        "age": 25,
        "gender": "MALE",
        "address": "Ho Chi Minh City",
        "avatar": null,
        "company": { "id": 1, "name": "HoiDanIT" },
        "roles": [{ "id": 1, "name": "ADMIN" }],
        "createdAt": "20xx-02-28T10:00:00Z",
        "updatedAt": null
      }
    ]
  },
  "message": "Fetch all users",
  "timestamp": "20xx-02-28T10:00:00"
}
```

---

### GET /users/{id} 🔒

Get a single user by ID.

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "id": 1,
    "name": "Nguyen Van A",
    "email": "user@example.com",
    "age": 25,
    "gender": "MALE",
    "address": "Ho Chi Minh City",
    "avatar": null,
    "company": { "id": 1, "name": "HoiDanIT" },
    "roles": [{ "id": 1, "name": "ADMIN" }],
    "createdAt": "20xx-02-28T10:00:00Z",
    "updatedAt": null
  },
  "message": "Fetch user",
  "timestamp": "20xx-02-28T10:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 404 | User not found |

---

### POST /users 🔒

Create a new user (admin operation — assigns company and roles).

**Request Body:**

```json
{
  "name": "Tran Thi B",
  "email": "tran@example.com",
  "password": "password123",
  "age": 30,
  "gender": "FEMALE",
  "address": "Ha Noi",
  "companyId": 1,
  "roleIds": [3, 5]
}
```

**Success Response (201):**

```json
{
  "statusCode": 201,
  "data": {
    "id": 2,
    "name": "Tran Thi B",
    "email": "tran@example.com",
    "age": 30,
    "gender": "FEMALE",
    "address": "Ha Noi",
    "avatar": null,
    "company": { "id": 1, "name": "HoiDanIT" },
    "roles": [
      { "id": 3, "name": "HR" },
      { "id": 5, "name": "USER" }
    ],
    "createdAt": "20xx-02-28T10:00:00Z"
  },
  "message": "User created",
  "timestamp": "20xx-02-28T10:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed |
| 404 | Company or Role not found |
| 409 | Email already exists |

---

### PUT /users 🔒

Update an existing user.

**Request Body:**

```json
{
  "id": 2,
  "name": "Tran Thi B Updated",
  "age": 31,
  "gender": "FEMALE",
  "address": "Da Nang",
  "companyId": 2,
  "roleIds": [3]
}
```

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "id": 2,
    "name": "Tran Thi B Updated",
    "email": "tran@example.com",
    "age": 31,
    "gender": "FEMALE",
    "address": "Da Nang",
    "avatar": null,
    "company": { "id": 2, "name": "FPT Software" },
    "roles": [{ "id": 3, "name": "HR" }],
    "updatedAt": "20xx-02-28T11:00:00Z"
  },
  "message": "User updated",
  "timestamp": "20xx-02-28T11:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed |
| 404 | User, Company, or Role not found |

**Note:** `email` and `password` are NOT updatable through this endpoint.

---

### DELETE /users/{id} 🔒

Delete a user. Also revokes all their refresh tokens.

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": null,
  "message": "User deleted",
  "timestamp": "20xx-02-28T12:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 404 | User not found |

---

## 3. Companies

### GET /companies 🔒

List all companies with pagination.

**Query Parameters:** same as `/users` (page, size, sort)

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "meta": { "page": 1, "pageSize": 10, "pages": 2, "total": 15 },
    "result": [
      {
        "id": 1,
        "name": "HoiDanIT",
        "description": "Education platform",
        "address": "Ho Chi Minh City",
        "logo": "/logos/hoidanit.png",
        "createdAt": "20xx-01-01T00:00:00Z",
        "updatedAt": null
      }
    ]
  },
  "message": "Fetch all companies",
  "timestamp": "20xx-02-28T10:00:00"
}
```

---

### GET /companies/{id} 🔒

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "id": 1,
    "name": "HoiDanIT",
    "description": "Education platform",
    "address": "Ho Chi Minh City",
    "logo": "/logos/hoidanit.png",
    "createdAt": "20xx-01-01T00:00:00Z",
    "updatedAt": null
  },
  "message": "Fetch company",
  "timestamp": "20xx-02-28T10:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 404 | Company not found |

---

### POST /companies 🔒

**Request Body:**

```json
{
  "name": "FPT Software",
  "description": "IT outsourcing",
  "address": "Ha Noi",
  "logo": "/logos/fpt.png"
}
```

**Success Response (201):**

```json
{
  "statusCode": 201,
  "data": {
    "id": 2,
    "name": "FPT Software",
    "description": "IT outsourcing",
    "address": "Ha Noi",
    "logo": "/logos/fpt.png",
    "createdAt": "20xx-02-28T10:00:00Z"
  },
  "message": "Company created",
  "timestamp": "20xx-02-28T10:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed (blank name) |

---

### PUT /companies 🔒

**Request Body:**

```json
{
  "id": 2,
  "name": "FPT Software Updated",
  "description": "Technology services",
  "address": "Da Nang",
  "logo": "/logos/fpt-new.png"
}
```

**Success Response (200):** same structure as POST.

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed |
| 404 | Company not found |

---

### DELETE /companies/{id} 🔒

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": null,
  "message": "Company deleted",
  "timestamp": "20xx-02-28T12:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 404 | Company not found |

**Note:** Deleting a company sets `company_id = null` on all associated users (does NOT delete users).

---

## 4. Roles ✅ implemented

### GET /roles 🔒

List all roles with pagination.

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "meta": { "page": 1, "pageSize": 10, "pages": 1, "total": 5 },
    "result": [
      {
        "id": 1,
        "name": "ADMIN",
        "description": "Full system access",
        "permissions": [
          {
            "id": 1,
            "name": "CREATE_USER",
            "apiPath": "/api/v1/users",
            "method": "POST",
            "module": "USER"
          },
          {
            "id": 4,
            "name": "VIEW_USERS",
            "apiPath": "/api/v1/users",
            "method": "GET",
            "module": "USER"
          }
        ],
        "createdAt": "20xx-01-01T00:00:00Z",
        "updatedAt": null
      }
    ]
  },
  "message": "Fetch all roles",
  "timestamp": "20xx-02-28T10:00:00"
}
```

---

### GET /roles/{id} 🔒

**Success Response (200):** single role with permissions array (same structure as list item above).

**Errors:**
| Status | When |
|--------|------|
| 404 | Role not found |

---

### POST /roles 🔒

**Request Body:**

```json
{
  "name": "HR",
  "description": "Human resources management",
  "permissionIds": [1, 2, 4, 8]
}
```

**Success Response (201):**

```json
{
  "statusCode": 201,
  "data": {
    "id": 3,
    "name": "HR",
    "description": "Human resources management",
    "permissions": [
      {
        "id": 1,
        "name": "CREATE_USER",
        "apiPath": "/api/v1/users",
        "method": "POST",
        "module": "USER"
      },
      {
        "id": 2,
        "name": "UPDATE_USER",
        "apiPath": "/api/v1/users",
        "method": "PUT",
        "module": "USER"
      },
      {
        "id": 4,
        "name": "VIEW_USERS",
        "apiPath": "/api/v1/users",
        "method": "GET",
        "module": "USER"
      },
      {
        "id": 8,
        "name": "VIEW_COMPANIES",
        "apiPath": "/api/v1/companies",
        "method": "GET",
        "module": "COMPANY"
      }
    ],
    "createdAt": "20xx-02-28T10:00:00Z"
  },
  "message": "Role created",
  "timestamp": "20xx-02-28T10:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed (blank name) |
| 404 | One or more Permission IDs not found |

---

### PUT /roles 🔒

**Request Body:**

```json
{
  "id": 3,
  "name": "HR",
  "description": "Updated description",
  "permissionIds": [1, 2, 4, 5, 8]
}
```

**Success Response (200):** same structure as POST.

**Note:** `permissionIds` replaces the entire permission list (not additive).

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed |
| 404 | Role or Permission not found |

---

### DELETE /roles/{id} 🔒

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": null,
  "message": "Role deleted",
  "timestamp": "20xx-02-28T12:00:00"
}
```

**Note:** Also removes this role from all users who had it (clears join table entries).

**Errors:**
| Status | When |
|--------|------|
| 404 | Role not found |

---

## 5. Permissions

### GET /permissions 🔒

List all permissions with pagination.

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "meta": { "page": 1, "pageSize": 10, "pages": 1, "total": 10 },
    "result": [
      {
        "id": 1,
        "name": "CREATE_USER",
        "apiPath": "/api/v1/users",
        "method": "POST",
        "module": "USER",
        "createdAt": "20xx-01-01T00:00:00Z",
        "updatedAt": null
      }
    ]
  },
  "message": "Fetch all permissions",
  "timestamp": "20xx-02-28T10:00:00"
}
```

---

### GET /permissions/{id} 🔒

**Success Response (200):** single permission (same structure as list item above).

**Errors:**
| Status | When |
|--------|------|
| 404 | Permission not found |

---

### POST /permissions 🔒

**Request Body:**

```json
{
  "name": "CREATE_USER",
  "apiPath": "/api/v1/users",
  "method": "POST",
  "module": "USER"
}
```

**Success Response (201):**

```json
{
  "statusCode": 201,
  "data": {
    "id": 1,
    "name": "CREATE_USER",
    "apiPath": "/api/v1/users",
    "method": "POST",
    "module": "USER",
    "createdAt": "20xx-02-28T10:00:00Z"
  },
  "message": "Permission created",
  "timestamp": "20xx-02-28T10:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed (blank name, invalid method) |
| 409 | Duplicate apiPath + method combination |

---

### PUT /permissions 🔒

**Request Body:**

```json
{
  "id": 1,
  "name": "CREATE_USER",
  "apiPath": "/api/v1/users",
  "method": "POST",
  "module": "USER"
}
```

**Success Response (200):** same structure as POST.

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed |
| 404 | Permission not found |
| 409 | Duplicate apiPath + method (if changed to existing combo) |

---

### DELETE /permissions/{id} 🔒

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": null,
  "message": "Permission deleted",
  "timestamp": "20xx-02-28T12:00:00"
}
```

**Note:** Also removes this permission from all roles (clears join table entries).

**Errors:**
| Status | When |
|--------|------|
| 404 | Permission not found |

---

## 6. Tags

### GET /tags 🔒

List all tags with pagination.

**Query Parameters:** same as `/users` (page, size, sort)

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "meta": { "page": 1, "pageSize": 10, "pages": 1, "total": 3 },
    "result": [
      {
        "id": 1,
        "name": "Java",
        "description": "Ngôn ngữ lập trình Java",
        "createdAt": "20xx-01-01T00:00:00Z",
        "updatedAt": null
      }
    ]
  },
  "message": "Lấy danh sách tag thành công",
  "timestamp": "20xx-04-04T10:00:00"
}
```

---

### GET /tags/{id} 🔒

**Success Response (200):** single tag (same structure as list item above).

**Errors:**
| Status | When |
|--------|------|
| 404 | Tag not found |

---

### POST /tags 🔒

**Request Body:**

```json
{
  "name": "Spring Boot",
  "description": "Framework Spring Boot cho Java"
}
```

**Success Response (201):**

```json
{
  "statusCode": 201,
  "data": {
    "id": 2,
    "name": "Spring Boot",
    "description": "Framework Spring Boot cho Java",
    "createdAt": "20xx-04-04T10:00:00Z"
  },
  "message": "Tạo tag thành công",
  "timestamp": "20xx-04-04T10:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed (blank name) |
| 409 | Tag name already exists |

---

### PUT /tags 🔒

**Request Body:**

```json
{
  "id": 2,
  "name": "Spring Boot Updated",
  "description": "Updated description"
}
```

**Success Response (200):** same structure as POST.

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed |
| 404 | Tag not found |
| 409 | Tag name already taken by another tag |

---

### DELETE /tags/{id} 🔒

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": null,
  "message": "Xóa tag thành công",
  "timestamp": "20xx-04-04T12:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 404 | Tag not found |

---

## 7. Articles

### GET /articles 🔒

List all articles with pagination.

**Query Parameters:** same as `/users` (page, size, sort)

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "meta": { "page": 1, "pageSize": 10, "pages": 1, "total": 2 },
    "result": [
      {
        "id": 1,
        "title": "Giới thiệu Spring Boot 4",
        "slug": "gioi-thieu-spring-boot-4",
        "thumbnail": null,
        "type": 0,
        "views": 0,
        "active": 1,
        "author": { "id": 1, "name": "Admin" },
        "category": { "id": 3, "name": "Công nghệ" },
        "tags": [
          { "id": 1, "name": "Java" },
          { "id": 2, "name": "Spring Boot" }
        ],
        "createdAt": "20xx-01-01T00:00:00Z",
        "updatedAt": "20xx-01-01T00:00:00Z"
      }
    ]
  },
  "message": "Lấy danh sách bài viết thành công"
}
```

---

### GET /articles/search 🔒

Search articles with pagination. Search is case-insensitive and accent-insensitive on `title`, `slug`, and `content`.

**Query Parameters:**

- `keyword`: optional
- `page`, `size`, `sort`: same as `/users`

**Success Response (200):** same structure as `GET /articles`.

**Examples:**

- `keyword=huong dan` matches `Hướng dẫn`
- `keyword=SPRING` matches `spring`

---

### GET /articles/{id} 🔒

**Success Response (200):** single article (same structure as list item above).

**Errors:**
| Status | When |
|--------|------|
| 404 | Article not found |

---

### POST /articles 🔒

**Request Body:**

```json
{
  "title": "Bài viết mới",
  "slug": "bai-viet-moi",
  "content": "Nội dung bài viết...",
  "thumbnail": "uploads/thumbnails/image.jpg",
  "type": 0,
  "active": 1,
  "authorId": 1,
  "categoryId": 3,
  "tagIds": [1, 2]
}
```

**Field notes:**

- `type`: `byte` — 0=Tin tức, 1=Văn bản, 2=Video, 3=Gallery, 4=Khác (required)
- `active`: `byte` — 0=Nháp, 1=Published, 2=Ẩn (optional, defaults to 0)
- `authorId`: required — ID of existing user
- `categoryId`: optional
- `tagIds`: required (can be empty array `[]`)

**Success Response (201):** same structure as GET /{id}.

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed (blank title/slug, null type/authorId/tagIds) |
| 404 | authorId, categoryId, or any tagId not found |
| 409 | Slug already exists |

---

### PUT /articles 🔒

**Request Body:** same as POST with added `id` field:

```json
{
  "id": 1,
  "title": "Updated Title",
  "slug": "updated-slug",
  ...
}
```

**Success Response (200):** same structure as GET /{id}.

**Errors:**
| Status | When |
|--------|------|
| 400 | Validation failed |
| 404 | Article, author, category, or tag not found |
| 409 | Slug already taken by another article |

---

### DELETE /articles/{id} 🔒

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": null,
  "message": "Xóa bài viết thành công"
}
```

**Errors:**
| Status | When |
|--------|------|
| 404 | Article not found |

---

## 8. Dashboard

### GET /dashboard 🔒

Get summary counts for the admin dashboard.

**Success Response (200):**

```json
{
  "statusCode": 200,
  "data": {
    "totalUsers": 50,
    "totalCompanies": 15,
    "totalRoles": 5,
    "totalPermissions": 20
  },
  "message": "Lấy thông tin dashboard thành công"
}
```

---

## Error Response Format

All errors follow this structure:

```json
{
  "statusCode": 400,
  "data": null,
  "message": "Detailed error message",
  "timestamp": "20xx-02-28T10:00:00"
}
```

Validation errors include field details:

```json
{
  "statusCode": 400,
  "data": {
    "email": "Invalid email format",
    "password": "Password must be 8-100 characters",
    "name": "Name is required"
  },
  "message": "Validation failed",
  "timestamp": "20xx-02-28T10:00:00"
}
```

---

## 6. Files

### POST /files 🔒

Upload a single file to the server. The returned `fileName` is then used to update
`avatar` (user) or `logo` (company) via their respective PUT endpoints.

**Request:** `multipart/form-data`

| Field  | Type   | Required | Description                             |
| ------ | ------ | -------- | --------------------------------------- |
| file   | file   | yes      | The file to upload                      |
| folder | string | yes      | Target sub-folder: `avatars` or `logos` |

**Validation rules (rejected with 400 if violated):**

| Rule               | Constraint                                                  |
| ------------------ | ----------------------------------------------------------- |
| File name          | Must not be blank, no special characters except `-` `_` `.` |
| Allowed extensions | `jpg`, `jpeg`, `png`, `gif`, `webp`                         |
| Max file size      | 5 MB (5,242,880 bytes)                                      |
| Allowed folders    | `avatars`, `logos`                                          |

**Success Response (201):**

```json
{
  "statusCode": 201,
  "data": {
    "fileName": "1709123456789_avatar.jpg",
    "folder": "avatars",
    "fileUrl": "/uploads/avatars/1709123456789_avatar.jpg",
    "size": 204800,
    "uploadedAt": "20xx-02-28T10:00:00Z"
  },
  "message": "File uploaded",
  "timestamp": "20xx-02-28T10:00:00"
}
```

**Errors:**
| Status | When |
|--------|------|
| 400 | No file provided |
| 400 | File name is blank or contains invalid characters |
| 400 | File extension not allowed (only jpg/jpeg/png/gif/webp) |
| 400 | File size exceeds 5 MB |
| 400 | Folder value is not `avatars` or `logos` |

**Usage flow:**

```
1. POST /api/v1/files  →  { fileName: "1709123456789_avatar.jpg", ... }
2a. PUT /api/v1/users  →  { id: 1, ..., avatar: "1709123456789_avatar.jpg" }
2b. PUT /api/v1/companies  →  { id: 1, ..., logo: "1709123456789_logo.png" }
```

**File storage:**

- Files are saved under `{upload-dir}/{folder}/` on the server file system
- `upload-dir` is configured via `app.upload.base-dir` in `application.yml`
- Stored file name = `{epochMillis}_{sanitizedOriginalName}` to avoid collisions
- Served as static resources at `/uploads/**`

---

## 9. Categories

### GET /categories 🔒
List categories with pagination & filter.
**Query:** `page`, `size`, `sort` + filter fields from `CategoryFilterRequest`.

### GET /categories/roots 🔒
Get root categories (no parent).

### GET /categories/{id} 🔒
Get category by ID. **Errors:** 404

### POST /categories 🔒
**Body:** `{ "name": "...", "slug": "...", "active": 1, "parentId": null }`
**Response (201).** **Errors:** 400 (validation)

### PUT /categories 🔒
**Body:** `{ "id": 1, "name": "...", "slug": "...", "active": 1, "parentId": null }`
**Errors:** 400, 404

### DELETE /categories/{id} 🔒
**Errors:** 404

### GET /categories/search 🔒
**Query:** `keyword` — search by name.

### GET /categories/parent/{parentId} 🔒
Direct children of a parent category.

### GET /categories/{id}/children 🔒
Alias for `/parent/{id}` — same data.

### GET /categories/slug/{slug} 🔒
Get category by slug. **Errors:** 404

### GET /categories/slug/{slug}/articles 🔒
Articles under category slug (includes sub-tree). Paginated.

### GET /categories/{id}/tree 🔒
Category tree starting from ID.

### GET /categories/tree 🔒
Full category tree. **Query:** `keyword` (optional filter).

### GET /categories/{id}/articles 🔒
Articles under category tree by ID. Paginated.

**CategoryResponse:**
```json
{ "id": 1, "name": "Công nghệ", "slug": "cong-nghe", "active": 1,
  "parent": { "id": null, "name": null },
  "createdAt": "...", "updatedAt": "..." }
```

---

## 10. Documents

### GET /documents 🔒
List documents with pagination & filter (`DocumentFilterRequest`).

### GET /documents/article/{articleId} 🔒
All documents attached to an article.

### GET /documents/{id} 🔒
Get document by ID. **Errors:** 404

### POST /documents 🔒
**Body:** `{ "title": "...", "description": "...", "documentUrl": "uploads/...", "articleId": 1 }`
**Response (201).** **Errors:** 400

### PUT /documents 🔒
**Body:** `{ "id": 1, "title": "...", "description": "...", "documentUrl": "...", "articleId": 1 }`
**Errors:** 400, 404

### DELETE /documents/{id} 🔒
**Errors:** 404

**DocumentResponse:**
```json
{ "id": 1, "title": "...", "description": "...", "documentUrl": "...",
  "article": { "id": 1, "title": "..." }, "createdAt": "...", "updatedAt": "..." }
```

---

## 11. QLKH — Customer Auth

> Base path: `/api/v1/qlkh`
> Authentication: JWT QLKH (`Authorization: Bearer <token>`) — separate from admin JWT.

### POST /qlkh/auth/login 🔓
Login by customer code (DigiCode) + phone number.
**Body:** `{ "digiCode": "KH001", "phone": "0901234567" }`
**Response (200):**
```json
{ "data": { "accessToken": "eyJ...", "refreshToken": "uuid-string" } }
```
**Errors:** 404 (customer not found)

### POST /qlkh/auth/refresh 🔓
Refresh access token using UUID refresh token.
**Body:** `{ "refreshToken": "uuid-string" }`
**Response (200):** same as login. **Errors:** 401 (invalid/expired)

### POST /qlkh/auth/logout 🔒QLKH
Revoke all refresh tokens for the customer.
**Header:** `Authorization: Bearer <accessToken>`
**Response (200):** `{ "data": null }`

### GET /qlkh/customers/me 🔒QLKH
Get current customer info.
**Response (200):**
```json
{ "data": { "digiCode": "KH001", "name": "Nguyễn Văn A",
  "address": "...", "phone": "...", "email": "...", "sms": "...",
  "taxCode": "...", "isActive": 1, "isWaterCut": 0 } }
```

---

## 12. QLKH — Invoices

### GET /qlkh/invoices 🔒QLKH
List invoices for logged-in customer. Paginated, sorted by yearMonth DESC.
**Query:** `yearMonth` (optional filter), `page`, `size`
**InvoiceResponse:**
```json
{ "id": 1, "digiCode": "KH001", "customerName": "...", "yearMonth": "202501",
  "createdDate": "...", "numOfHouseHold": 1, "waterMeterSerial": "...",
  "amount": 50000, "envFee": 5000, "taxFee": 5000, "totalAmount": 60000,
  "paymentStatus": 1, "paymentStatusLabel": "Chưa thanh toán",
  "oldVal": 100, "newVal": 120, "rootKey": "...", "fkey": "...", "blankNo": "..." }
```

### GET /qlkh/invoices/{invoiceId} 🔒QLKH
Get invoice detail (must belong to customer). **Errors:** 404

### GET /qlkh/invoices/by-fkey 🔒QLKH
Lookup invoice by `fkey` param. **Errors:** 400, 404

### GET /qlkh/invoices/by-root-key 🔒QLKH
Lookup invoice by `rootKey` param. **Errors:** 400, 404

### GET /qlkh/invoices/{invoiceId}/e-invoice-download 🔒QLKH
Download e-invoice XML/ZIP from VNPT. Returns binary file (attachment).
**Content-Type:** `application/xml` or `application/zip`. **Errors:** 400, 404

### GET /qlkh/invoices/{invoiceId}/e-invoice-view 🔒QLKH
View e-invoice data parsed from VNPT HTML.
**InvoiceViewResponse:**
```json
{ "invoiceNo": "...", "invoiceDate": "...", "invoiceType": "...",
  "sellerName": "...", "buyerName": "...", "totalAmount": "...",
  "status": "PAID|UNPAID", ... }
```

### GET /qlkh/invoices/e-invoice-list 🔒QLKH
List e-invoices from VNPT by customer code. Returns XML.
**Query:** `fromDate` (dd/MM/yyyy), `toDate` (dd/MM/yyyy)

### GET /qlkh/month-invoices/readings 🔓
Meter readings by period. No JWT required.
**Query:** `yearMonth=YYYYMM` OR `fromYearMonth` + `toYearMonth`
**Response:** `[{ "digiCode": "KH001", "oldVal": 100, "newVal": 120 }]`

### GET /qlkh/month-invoices/consumption-history 🔓
Water consumption history for a single customer over a time range. No JWT required.
Designed for mobile app chart — replaces 6 separate calls to `/readings`.
**Query Parameters:**
| Param | Required | Format | Description |
|-------|----------|--------|-------------|
| `customerCode` | yes | string | Customer DigiCode |
| `fromYearMonth` | yes | yyyyMM | Start month |
| `toYearMonth` | yes | yyyyMM | End month |

**Success Response (200):**
```json
{
  "statusCode": 200,
  "message": "Lấy lịch sử tiêu thụ thành công",
  "data": [
    { "yearMonth": "202601", "oldVal": 150, "newVal": 165, "consumptionM3": 15 },
    { "yearMonth": "202602", "oldVal": 165, "newVal": 180, "consumptionM3": 15 },
    { "yearMonth": "202603", "oldVal": 180, "newVal": 195, "consumptionM3": 15 },
    { "yearMonth": "202604", "oldVal": 195, "newVal": null, "consumptionM3": null }
  ]
}
```

**Notes:**
- `consumptionM3` = `newVal - oldVal`, calculated server-side. `null` if either value is missing.
- Months with no data are omitted from the array.
- `fromYearMonth` must be ≤ `toYearMonth`.

**Errors:**
| Status | When |
|--------|------|
| 400 | Missing or empty `customerCode` |
| 400 | `fromYearMonth` or `toYearMonth` not in yyyyMM format |
| 400 | `fromYearMonth` > `toYearMonth` |

### GET /qlkh/sales-invoices 🔒QLKH
List sales invoices. **Query:** `templateCode` (optional), paginated.
**SalesInvoiceResponse:**
```json
{ "salesInvoiceId": 1, "invoiceNum": "...", "invoiceDate": "...",
  "templateCode": "...", "digiCode": "...", "customerName": "...",
  "address": "...", "invoiceTotal": 100000, "status": 1 }
```

### GET /qlkh/sales-invoices/{salesInvoiceId} 🔒QLKH
Get sales invoice detail. **Errors:** 404

### GET /qlkh/vnpt/health 🔒QLKH
Debug VNPT SOAP connection. **Query:** `fkey` (required)

### GET /qlkh/customer/articles/maintenance 🔓
Articles tagged "BaoTri-CupNuoc". Paginated.

### GET /qlkh/customer/articles/featured 🔓
Featured articles. Paginated.

---

## 13. QLKH — Notifications

> Base path: `/api/v1/qlkh/customer/notifications`

### GET /qlkh/customer/notifications 🔒QLKH
List notifications (newest first).
**Query:** `type` (optional), `excludeType` (optional)
**NotificationResponse:**
```json
{ "id": 1, "customerId": 100, "title": "...", "content": "...",
  "type": "INVOICE", "isRead": false, "createdAt": "...",
  "referenceId": 42, "isSystem": false, "url": null }
```

### GET /qlkh/customer/notifications/unread-count 🔒QLKH
**Query:** `type`, `excludeType` (optional). **Response:** `{ "data": 5 }`

### POST /qlkh/customer/notifications/read 🔒QLKH
Mark as read. **Body:** `{ "ids": [1,2,3], "isSystem": false }` — ids null/empty = mark all.

---

## 14. QLKH — Customer Device

> Base path: `/api/v1/qlkh/customer`

### POST /qlkh/customer/device/register 🔒QLKH
Register FCM device token.
**Body:** `{ "deviceToken": "fcm-token-string", "platform": "ANDROID|IOS" }`

### POST /qlkh/customer/device/unregister 🔒QLKH
Unregister FCM device token (on logout).
**Body:** `{ "deviceToken": "fcm-token-string" }`

---

## 15. QLKH — Customer Feedback

> Base path: `/api/v1/qlkh/customer/feedbacks`

### POST /qlkh/customer/feedbacks 🔒QLKH
Submit feedback. `multipart/form-data`.
**Fields:** `issueType`, `location`, `description`, `images`/`image`/`upload_image` (files)
**Response:** `{ "data": { "trackingCode": "FB-20260517-XXXX" } }`

### GET /qlkh/customer/feedbacks 🔒QLKH
List customer's own feedbacks.
```json
[{ "id": 1, "trackingCode": "...", "issueType": "WATER_LEAK",
   "location": "...", "description": "...", "status": "PENDING",
   "images": ["url1"], "createdAt": "..." }]
```

### GET /qlkh/customer/feedbacks/{id} 🔒QLKH
Feedback detail with staff replies. **Errors:** 404

---

## 16. Admin — Invoices

> Base path: `/api/v1/admin/invoices`

### GET /admin/invoices 🔒
List all invoices (admin dashboard). Paginated with filters.
**Query filters:** `yearMonth`, `paymentStatus`, `customerName`, `digiCode`, `remindStatus`, `roadId`
**AdminInvoiceResponse:**
```json
{ "id": 1, "digiCode": "KH001", "customerName": "...",
  "totalAmount": 60000, "yearMonth": "202501", "invoiceNo": "...",
  "fkey": "...", "qrUrl": "https://...", "blankNo": "...", "roadId": 5,
  "paymentStatus": 1, "isReminded": false, "isOverdue": false,
  "isWaterCutoff": false, "hasReplacement": false }
```

### POST /admin/invoices/send-debt-reminder 🔒
Send debt reminder notifications.
**Body:** `{ "yearMonth": "202501", "monthInvoiceId": null }`
**Response:** `{ "data": { "sentCount": 10, "skipCount": 2 } }`

### POST /admin/invoices/send-overdue-reminder 🔒
Send overdue reminder notifications. Same body/response as debt-reminder.

### POST /admin/invoices/send-water-cutoff 🔒
Send water cutoff notification for a specific invoice.
**Body:** `{ "monthInvoiceId": "123", "employeeName": "...", "employeePhone": "..." }`
**Response:** `{ "data": true }`

### POST /admin/invoices/send-payment-notification 🔒
Admin manually sends payment-success notifications for paid invoices.
**Body:** `{ "monthInvoiceIds": [101, 102] }`
- Only sends for invoices with `paymentStatus == 2` (paid). Others are skipped.
- Deduplication: invoices already notified (in `NotifiedPayment` table) are skipped.
- Sends in parallel (up to 10 threads), timeout 120 s.

**Response:** `{ "data": { "sentCount": 2, "skipCount": 0 } }`

**Errors:**
| Status | When |
|--------|------|
| 400 | `monthInvoiceIds` is null or empty |

---

## 17. Admin — Feedbacks

> Base path: `/api/v1/admin/feedbacks`

### GET /admin/feedbacks/statistics 🔒
Feedback statistics (counts by status).

### GET /admin/feedbacks 🔒
List all feedbacks with filters. Paginated.
**Query:** `keyword`, `status`, `issueType`, `customerSearch`, `createdFrom`, `createdTo`

### GET /admin/feedbacks/{id} 🔒
Feedback detail with customer info and replies.

### PUT /admin/feedbacks/{id}/status 🔒
Update feedback status. **Body:** `{ "status": "IN_PROGRESS" }`

### POST /admin/feedbacks/{id}/replies 🔒
Add staff reply. **Body:** `{ "content": "..." }`

### GET /admin/feedbacks/{id}/replies 🔒
List replies for a feedback.

---

## 18. Admin — Roads

### GET /admin/roads 🔒
Get roads for dropdown filter.
**Response:**
```json
{ "data": [{ "id": 1, "name": "Đường ABC", "type": 1 }] }
```

---

## 19. Admin — Notifications

> Base path: `/api/v1/admin/notifications`

### GET /admin/notifications 🔒
List all notifications (admin tracker). Paginated with filters.
**Query filters:** `type`, `deliveryStatus`, `customerId`, `customerDigiCode`, `createdFrom`, `createdTo`, `roadId`, `page`, `size`
**NotificationAdminResponse:**
```json
{
  "id": 1,
  "customerId": 101,
  "customerName": "Nguyễn Văn A",
  "customerDigiCode": "KH001",
  "title": "Thông báo hóa đơn",
  "type": "INVOICE",
  "referenceId": 123,
  "isRead": false,
  "deliveryStatus": "DELIVERED",
  "deliveredAt": "2026-06-08T10:00:00",
  "failureReason": null,
  "createdAt": "2026-06-08T09:30:00"
}
```

### GET /admin/notifications/statistics 🔒
Get statistics counts of sent notifications by status/type.

### GET /admin/notifications/{id} 🔒
Get details of a single notification.

### POST /admin/notifications/{id}/resend 🔒
Resend a failed notification.

---

## 20. External — QR Payment

> Base path: `/api/v1/external/qr-payment`
> Auth: API Key (not JWT)

### POST /external/qr-payment/generate 🔑
Generate VietQR payment URL.
**Body:**
```json
{ "customerCode": "KH001", "yearMonth": "202501",
  "amount": 50000, "envFee": 5000, "taxFee": 5000, "fkey": "..." }
```
**Response:**
```json
{ "data": { "fkey": "...", "customerCode": "KH001", "yearMonth": "202501",
  "totalAmount": 60000, "qrUrl": "https://img.vietqr.io/...", "addInfo": "..." } }
```
**Errors:** 400 (total = 0)

---

## Endpoint Summary

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| **Auth** | | | |
| POST | /auth/login | 🔓 | Admin login |
| POST | /auth/register | 🔓 | Register |
| POST | /auth/refresh | 🔓 | Refresh token |
| POST | /auth/logout | 🔒 | Logout |
| GET | /auth/me | 🔒 | Current admin info |
| **Users** | | | |
| GET | /users | 🔒 | List users |
| GET | /users/{id} | 🔒 | Get user |
| POST | /users | 🔒 | Create user |
| PUT | /users | 🔒 | Update user |
| DELETE | /users/{id} | 🔒 | Delete user |
| **Companies** | | | |
| GET | /companies | 🔒 | List companies |
| GET | /companies/{id} | 🔒 | Get company |
| POST | /companies | 🔒 | Create company |
| PUT | /companies | 🔒 | Update company |
| DELETE | /companies/{id} | 🔒 | Delete company |
| **Roles** | | | |
| GET | /roles | 🔒 | List roles |
| GET | /roles/{id} | 🔒 | Get role |
| POST | /roles | 🔒 | Create role |
| PUT | /roles | 🔒 | Update role |
| DELETE | /roles/{id} | 🔒 | Delete role |
| **Permissions** | | | |
| GET | /permissions | 🔒 | List permissions |
| GET | /permissions/{id} | 🔒 | Get permission |
| POST | /permissions | 🔒 | Create permission |
| PUT | /permissions | 🔒 | Update permission |
| DELETE | /permissions/{id} | 🔒 | Delete permission |
| **Tags** | | | |
| GET | /tags | 🔒 | List tags |
| GET | /tags/{id} | 🔒 | Get tag |
| POST | /tags | 🔒 | Create tag |
| PUT | /tags | 🔒 | Update tag |
| DELETE | /tags/{id} | 🔒 | Delete tag |
| **Articles** | | | |
| GET | /articles | 🔒 | List articles |
| GET | /articles/search | 🔒 | Search articles |
| GET | /articles/{id} | 🔒 | Get article |
| POST | /articles | 🔒 | Create article |
| PUT | /articles | 🔒 | Update article |
| DELETE | /articles/{id} | 🔒 | Delete article |
| **Categories** | | | |
| GET | /categories | 🔒 | List categories |
| GET | /categories/roots | 🔒 | Root categories |
| GET | /categories/{id} | 🔒 | Get category |
| POST | /categories | 🔒 | Create category |
| PUT | /categories | 🔒 | Update category |
| DELETE | /categories/{id} | 🔒 | Delete category |
| GET | /categories/search | 🔒 | Search by name |
| GET | /categories/parent/{parentId} | 🔒 | Direct children |
| GET | /categories/{id}/children | 🔒 | Direct children (alias) |
| GET | /categories/slug/{slug} | 🔒 | Get by slug |
| GET | /categories/slug/{slug}/articles | 🔒 | Articles by slug |
| GET | /categories/{id}/tree | 🔒 | Category tree |
| GET | /categories/tree | 🔒 | Full tree |
| GET | /categories/{id}/articles | 🔒 | Articles by category tree |
| **Documents** | | | |
| GET | /documents | 🔒 | List documents |
| GET | /documents/article/{articleId} | 🔒 | Docs by article |
| GET | /documents/{id} | 🔒 | Get document |
| POST | /documents | 🔒 | Create document |
| PUT | /documents | 🔒 | Update document |
| DELETE | /documents/{id} | 🔒 | Delete document |
| **Files & Media** | | | |
| POST | /files | 🔒 | Upload file |
| POST | /media | 🔒 | Upload media |
| GET | /media | 🔒 | List media |
| GET | /media/{id} | 🔒 | Get media |
| DELETE | /media/{id} | 🔒 | Delete media |
| **Dashboard** | | | |
| GET | /dashboard | 🔒 | Dashboard summary |
| **QLKH Auth** | | | |
| POST | /qlkh/auth/login | 🔓 | Customer login |
| POST | /qlkh/auth/refresh | 🔓 | Customer refresh token |
| POST | /qlkh/auth/logout | 🔒Q | Customer logout |
| GET | /qlkh/customers/me | 🔒Q | Customer info |
| **QLKH Invoices** | | | |
| GET | /qlkh/invoices | 🔒Q | List invoices |
| GET | /qlkh/invoices/{id} | 🔒Q | Get invoice |
| GET | /qlkh/invoices/by-fkey | 🔒Q | Lookup by fkey |
| GET | /qlkh/invoices/by-root-key | 🔒Q | Lookup by rootKey |
| GET | /qlkh/invoices/{id}/e-invoice-download | 🔒Q | Download e-invoice |
| GET | /qlkh/invoices/{id}/e-invoice-view | 🔒Q | View e-invoice |
| GET | /qlkh/invoices/e-invoice-list | 🔒Q | List e-invoices (XML) |
| GET | /qlkh/month-invoices/readings | 🔓 | Meter readings |
| GET | /qlkh/month-invoices/consumption-history | 🔓 | Consumption history for 1 customer |
| GET | /qlkh/sales-invoices | 🔒Q | List sales invoices |
| GET | /qlkh/sales-invoices/{id} | 🔒Q | Get sales invoice |
| GET | /qlkh/vnpt/health | 🔒Q | VNPT health check |
| GET | /qlkh/customer/articles/maintenance | 🔓 | Maintenance articles |
| GET | /qlkh/customer/articles/featured | 🔓 | Featured articles |
| **QLKH Notifications** | | | |
| GET | /qlkh/customer/notifications | 🔒Q | List notifications |
| GET | /qlkh/customer/notifications/unread-count | 🔒Q | Unread count |
| POST | /qlkh/customer/notifications/read | 🔒Q | Mark as read |
| **QLKH Device** | | | |
| POST | /qlkh/customer/device/register | 🔒Q | Register FCM token |
| POST | /qlkh/customer/device/unregister | 🔒Q | Unregister FCM token |
| **QLKH Feedback** | | | |
| POST | /qlkh/customer/feedbacks | 🔒Q | Submit feedback |
| GET | /qlkh/customer/feedbacks | 🔒Q | List my feedbacks |
| GET | /qlkh/customer/feedbacks/{id} | 🔒Q | Feedback detail |
| **Admin Invoices** | | | |
| GET | /admin/invoices | 🔒 | List invoices (admin) |
| POST | /admin/invoices/send-debt-reminder | 🔒 | Send debt reminder |
| POST | /admin/invoices/send-overdue-reminder | 🔒 | Send overdue reminder |
| POST | /admin/invoices/send-water-cutoff | 🔒 | Send water cutoff |
| POST | /admin/invoices/send-payment-notification | 🔒 | Send payment success notification |
| **Admin Feedbacks** | | | |
| GET | /admin/feedbacks/statistics | 🔒 | Feedback stats |
| GET | /admin/feedbacks | 🔒 | List feedbacks |
| GET | /admin/feedbacks/{id} | 🔒 | Feedback detail |
| PUT | /admin/feedbacks/{id}/status | 🔒 | Update status |
| POST | /admin/feedbacks/{id}/replies | 🔒 | Add reply |
| GET | /admin/feedbacks/{id}/replies | 🔒 | List replies |
| **Admin Roads** | | | |
| GET | /admin/roads | 🔒 | Roads dropdown |
| **Admin Notifications** | | | |
| GET | /admin/notifications | 🔒 | List notifications (admin) |
| GET | /admin/notifications/statistics | 🔒 | Notifications statistics |
| GET | /admin/notifications/{id} | 🔒 | Get notification detail |
| POST | /admin/notifications/{id}/resend | 🔒 | Resend notification |
| **External** | | | |
| POST | /external/qr-payment/generate | 🔑 | Generate VietQR |

> **Legend:** 🔓 Public · 🔒 Admin JWT · 🔒Q QLKH JWT · 🔑 API Key
