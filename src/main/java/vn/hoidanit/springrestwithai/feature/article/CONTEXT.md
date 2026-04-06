# Article Feature Context

## Overview

CRUD management for articles (`articles` table) with tag associations via a join entity.

## Entity: Article

- `byte type` — 0=Tin tức, 1=Văn bản, 2=Video, 3=Gallery, 4=Khác
- `byte active` — 0=Nháp, 1=Published, 2=Ẩn
- `int views` — read-only, set to 0 on create, never updated via API
- `@ManyToOne User author` — required (NOT NULL), passed as `authorId` in request body
- `@ManyToOne Category category` — optional (nullable)
- `@OneToMany(cascade=ALL, orphanRemoval=true) List<TagArticle> tagArticles` — tag associations

## Entity: TagArticle

Join entity for the `tag_articles` table. Has its own `id` PK and `created_at` column (hence not a simple `@ManyToMany`). Created/deleted automatically via Article cascade.

## Endpoints (base: `/api/v1/articles`)

| Method | Path         | Permission     | Description             |
| ------ | ------------ | -------------- | ----------------------- |
| GET    | /            | VIEW_ARTICLES  | Paginated list + filter |
| GET    | /{id}        | VIEW_ARTICLE   | Get by ID               |
| GET    | /slug/{slug} | -              | Get by slug             |
| POST   | /            | CREATE_ARTICLE | Create article (201)    |
| PUT    | /            | UPDATE_ARTICLE | Update article          |
| DELETE | /{id}        | DELETE_ARTICLE | Delete article          |

### Filter params (`GET /`)

- `categorySlug` — lọc theo slug của danh mục (INNER JOIN, bỏ bài không có category nếu truyền giá trị)

## Key Design Decisions

- `type` and `active` are `byte` fields (NOT enums) — matches DATABASE.md spec exactly
- Tags are managed via `TagArticle` entity rather than `@ManyToMany` because the join table has extra columns (`id`, `created_at`)
- On update: `article.getTagArticles().clear()` then add new ones — `orphanRemoval=true` handles
  DELETE of old TagArticle records within the same transaction
- `active` in `CreateArticleRequest` is nullable, defaults to `(byte) 0` (Draft) if not provided
- Documents relationship NOT implemented on Article side (Document feature uses Category FK, not Article FK)

## Service: ArticleServiceImpl

- `resolveTags(List<Long> tagIds)` — deduplicates IDs, bulk fetches, throws 404 for first missing
- `resolveCategory(Long categoryId)` — returns null if `categoryId` is null, else fetches or 404

## Tests

- `ArticleServiceImplTest` — 14 Mockito unit tests covering all service methods
- `ArticleControllerTest` — 16 MockMvc integration tests using `@ActiveProfiles("test")`
  - BeforeEach: creates test User, Category, Tag + seeds permissions
  - AfterEach: `articleRepository.deleteAll()` → `tagRepository.deleteAll()` → `categoryRepository.deleteAll()` → `userRepository.deleteAll()` → `testDataFactory.cleanup()`
