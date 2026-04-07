package vn.hoidanit.springrestwithai.feature.category;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.hoidanit.springrestwithai.common.TestDataFactory;
import vn.hoidanit.springrestwithai.feature.article.Article;
import vn.hoidanit.springrestwithai.feature.article.ArticleRepository;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    private Category parentCategory;
    private Category childCategory;
    private User testAuthor;

    @BeforeEach
    void setUp() {
        testDataFactory.seedPermissions("CATEGORIES", "/api/v1/categories", "GET", "POST", "PUT", "DELETE");
        testDataFactory.seedPermissions("CATEGORIES", "/api/v1/categories/**", "GET", "DELETE");

        testAuthor = userRepository.save(buildUser("category-author@test.com", "Category Author"));

        parentCategory = new Category();
        parentCategory.setName("Tin tuc");
        parentCategory.setSlug("tin-tuc");
        parentCategory = categoryRepository.save(parentCategory);

        childCategory = new Category();
        childCategory.setName("Cong nghe");
        childCategory.setSlug("cong-nghe");
        childCategory.setParent(parentCategory);
        childCategory = categoryRepository.save(childCategory);
    }

    @AfterEach
    void cleanUp() {
        articleRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        testDataFactory.cleanup();
    }

    @Test
    @DisplayName("GET /categories/slug/{slug}/articles - 200: returns paginated articles for category tree")
    void getArticlesByCategorySlug_success_returns200() throws Exception {
        articleRepository.save(buildArticle("Root Article", "root-article", testAuthor, parentCategory));
        articleRepository.save(buildArticle("Child Article", "child-article", testAuthor, childCategory));

        mockMvc.perform(get("/api/v1/categories/slug/{slug}/articles", parentCategory.getSlug())
                .param("sort", "id,asc")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.meta.total", is(2)))
            .andExpect(jsonPath("$.data.result[0].title", is("Root Article")))
            .andExpect(jsonPath("$.data.result[1].title", is("Child Article")));
    }

    @Test
    @DisplayName("GET /categories/slug/{slug}/articles - 404: category slug not found returns 404")
    void getArticlesByCategorySlug_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/categories/slug/{slug}/articles", "khong-ton-tai")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("GET /categories/slug/{slug}/articles - 401: no token returns unauthorized")
    void getArticlesByCategorySlug_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/categories/slug/{slug}/articles", parentCategory.getSlug()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /categories/slug/{slug}/articles - 403: no permission returns forbidden")
    void getArticlesByCategorySlug_noPermission_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/categories/slug/{slug}/articles", parentCategory.getSlug())
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    private User buildUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

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
}