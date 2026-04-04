# Tag Feature — Context

## What this module does
CRUD for the `tags` entity. Tags are labels that can be attached to articles (via the `tag_articles` join table defined in the database schema). This module only covers tag management — the article-tag relationship is deferred until the articles feature is built.

## Decisions

### Name uniqueness constraint
`name` has a `@UniqueConstraint` at the database level and a `DuplicateResourceException` guard in the service layer. Tag names must be globally unique. Attempted duplicates return HTTP 409.

### `description` is nullable
No `@NotBlank` on `description`. Tags are often created with just a name; descriptions can be added later via PUT.

### Article relationship is out of scope here
The `tag_articles` join table exists in the schema, but `Tag` does not map a `@ManyToMany` to `Article` in this phase. This link will be added when the articles feature is implemented.

## Key files
- `Tag.java` — entity, table name `tags`
- `TagRepository.java` — `existsByName`, `existsByNameAndIdNot` for uniqueness checks
- `TagServiceImpl.java` — business logic: 409 on duplicate name, 404 on not found
- `TagController.java` — `POST /api/v1/tags` returns 201 with `Location` header

## Test coverage
- `TagServiceImplTest` — 10 unit tests (Mockito, no DB)
- `TagControllerTest` — 13 integration tests (MockMvc + live MySQL, `@ActiveProfiles("test")`)
