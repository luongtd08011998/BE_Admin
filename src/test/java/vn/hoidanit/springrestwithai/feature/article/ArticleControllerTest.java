package vn.hoidanit.springrestwithai.feature.article;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import vn.hoidanit.springrestwithai.common.TestDataFactory;
import vn.hoidanit.springrestwithai.feature.article.dto.CreateArticleRequest;
import vn.hoidanit.springrestwithai.feature.article.dto.UpdateArticleRequest;
import vn.hoidanit.springrestwithai.feature.category.Category;
import vn.hoidanit.springrestwithai.feature.category.CategoryRepository;
import vn.hoidanit.springrestwithai.feature.tag.Tag;
import vn.hoidanit.springrestwithai.feature.tag.TagRepository;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    private User testAuthor;
    private Category testCategory;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testDataFactory.seedPermissions("ARTICLES", "/api/v1/articles", "GET", "POST", "PUT", "DELETE");
        testDataFactory.seedPermissions("ARTICLES", "/api/v1/articles/**", "GET", "DELETE");

        testAuthor = userRepository.save(buildUser("author@test.com", "Test Author"));
        testCategory = categoryRepository.save(buildCategory("Test Category", "test-category-ctrl"));
        testTag = tagRepository.save(buildTag("TestTag", "Test tag description"));
    }

    @AfterEach
    void cleanUp() {
        articleRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        testDataFactory.cleanup();
    }

    // ========== POST /api/v1/articles ==========

    @Test
    @DisplayName("POST /articles - 201: creates article and returns response body")
    void createArticle_success_returns201() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest(
                "My First Article", "my-first-article", "Content here",
                null, (byte) 0, (byte) 1, testAuthor.getId(), null, List.of());

        mockMvc.perform(post("/api/v1/articles")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.title", is("My First Article")))
                .andExpect(jsonPath("$.data.slug", is("my-first-article")))
                .andExpect(jsonPath("$.data.author.name", is("Test Author")));
    }

    @Test
    @DisplayName("POST /articles - 201: creates article with tags and category")
    void createArticle_withTagsAndCategory_returns201() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest(
                "Tagged Article", "tagged-article", "Content",
                "thumb.jpg", (byte) 0, (byte) 1,
                testAuthor.getId(), testCategory.getId(), List.of(testTag.getId()));

        mockMvc.perform(post("/api/v1/articles")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.category.id", is(testCategory.getId().intValue())))
                .andExpect(jsonPath("$.data.tags[0].name", is("TestTag")));
    }

    @Test
    @DisplayName("POST /articles - 400: missing required fields returns validation error")
    void createArticle_missingFields_returns400() throws Exception {
        String emptyJson = "{}";

        mockMvc.perform(post("/api/v1/articles")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("POST /articles - 409: duplicate slug returns conflict")
    void createArticle_duplicateSlug_returns409() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest(
                "Article One", "dup-slug", null, null, (byte) 0, null,
                testAuthor.getId(), null, List.of());

        mockMvc.perform(post("/api/v1/articles")
                .with(testDataFactory.jwtWithPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/articles")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode", is(409)));
    }

    @Test
    @DisplayName("POST /articles - 401: no token returns unauthorized")
    void createArticle_noToken_returns401() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest(
                "Article", "article-slug", null, null, (byte) 0, null,
                testAuthor.getId(), null, List.of());

        mockMvc.perform(post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /articles - 403: no permission returns forbidden")
    void createArticle_noPermission_returns403() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest(
                "Article", "article-slug-403", null, null, (byte) 0, null,
                testAuthor.getId(), null, List.of());

        mockMvc.perform(post("/api/v1/articles")
                        .with(testDataFactory.jwtWithoutPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== GET /api/v1/articles ==========

    @Test
    @DisplayName("GET /articles - 200: returns paginated list")
    void getAllArticles_success_returns200() throws Exception {
        Article article = buildArticle("Paginated Article", "paginated-article", testAuthor, null);
        articleRepository.save(article);

        mockMvc.perform(get("/api/v1/articles")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.meta.total", is(1)))
                .andExpect(jsonPath("$.data.result[0].title", is("Paginated Article")));
    }

    @Test
    @DisplayName("GET /articles - 401: no token returns unauthorized")
    void getAllArticles_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/articles"))
                .andExpect(status().isUnauthorized());
    }

    // ========== GET /api/v1/articles/{id} ==========

    @Test
    @DisplayName("GET /articles/{id} - 200: returns article by id")
    void getArticleById_success_returns200() throws Exception {
        Article saved = articleRepository.save(
                buildArticle("Detail Article", "detail-article", testAuthor, null));

        mockMvc.perform(get("/api/v1/articles/" + saved.getId())
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.title", is("Detail Article")))
                .andExpect(jsonPath("$.data.slug", is("detail-article")));
    }

    @Test
    @DisplayName("GET /articles/{id} - 404: not found returns 404")
    void getArticleById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/articles/99999")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    // ========== PUT /api/v1/articles ==========

    @Test
    @DisplayName("PUT /articles - 200: updates article successfully")
    void updateArticle_success_returns200() throws Exception {
        Article saved = articleRepository.save(
                buildArticle("Original Title", "original-slug", testAuthor, null));

        UpdateArticleRequest request = new UpdateArticleRequest(
                saved.getId(), "Updated Title", "updated-slug", "New content",
                null, (byte) 0, (byte) 1, testAuthor.getId(), null, List.of());

        mockMvc.perform(put("/api/v1/articles")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.title", is("Updated Title")))
                .andExpect(jsonPath("$.data.slug", is("updated-slug")));
    }

    @Test
    @DisplayName("PUT /articles - 404: updating non-existent article returns 404")
    void updateArticle_notFound_returns404() throws Exception {
        UpdateArticleRequest request = new UpdateArticleRequest(
                99999L, "Title", "slug-for-update", null,
                null, (byte) 0, null, testAuthor.getId(), null, List.of());

        mockMvc.perform(put("/api/v1/articles")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("PUT /articles - 400: missing required fields returns validation error")
    void updateArticle_missingFields_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/articles")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("PUT /articles - 403: no permission returns forbidden")
    void updateArticle_noPermission_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/articles")
                        .with(testDataFactory.jwtWithoutPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ========== DELETE /api/v1/articles/{id} ==========

    @Test
    @DisplayName("DELETE /articles/{id} - 200: deletes article successfully")
    void deleteArticle_success_returns200() throws Exception {
        Article saved = articleRepository.save(
                buildArticle("Delete Me", "delete-me", testAuthor, null));

        mockMvc.perform(delete("/api/v1/articles/" + saved.getId())
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)));
    }

    @Test
    @DisplayName("DELETE /articles/{id} - 404: not found returns 404")
    void deleteArticle_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/articles/99999")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("DELETE /articles/{id} - 401: no token returns unauthorized")
    void deleteArticle_noToken_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/articles/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /articles/{id} - 403: no permission returns forbidden")
    void deleteArticle_noPermission_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/articles/1")
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== helpers ==========

    private Article buildArticle(String title, String slug, User author, Category category) {
        Article article = new Article();
        article.setTitle(title);
        article.setSlug(slug);
        article.setType((byte) 0);
        article.setActive((byte) 1);
        article.setViews(0);
        article.setAuthor(author);
        article.setCategory(category);
        return article;
    }

    private User buildUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private Category buildCategory(String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        return category;
    }

    private Tag buildTag(String name, String description) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        return tag;
    }
}
