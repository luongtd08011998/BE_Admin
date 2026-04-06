package vn.hoidanit.springrestwithai.feature.article;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.article.dto.ArticleFilterRequest;
import vn.hoidanit.springrestwithai.feature.article.dto.ArticleResponse;
import vn.hoidanit.springrestwithai.feature.article.dto.CreateArticleRequest;
import vn.hoidanit.springrestwithai.feature.article.dto.UpdateArticleRequest;
import vn.hoidanit.springrestwithai.feature.category.Category;
import vn.hoidanit.springrestwithai.feature.category.CategoryRepository;
import vn.hoidanit.springrestwithai.feature.tag.Tag;
import vn.hoidanit.springrestwithai.feature.tag.TagRepository;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ArticleServiceImpl articleService;

    // ========== create ==========

    @Test
    @DisplayName("create - success without tags: saves article and returns response")
    void create_successWithoutTags_returnsArticleResponse() {
        CreateArticleRequest request = new CreateArticleRequest(
                "Spring Boot Tutorial", "spring-boot-tutorial", "Content here",
                null, (byte) 0, null, 1L, null, List.of());

        User author = buildUser(1L, "Admin");
        Article saved = buildArticle(1L, "Spring Boot Tutorial", "spring-boot-tutorial",
                (byte) 0, (byte) 0, author, null, List.of());

        when(articleRepository.existsBySlug("spring-boot-tutorial")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        ArticleResponse response = articleService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Spring Boot Tutorial");
        assertThat(response.slug()).isEqualTo("spring-boot-tutorial");
        assertThat(response.tags()).isEmpty();
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    @Test
    @DisplayName("create - success with tags and category: maps all fields correctly")
    void create_successWithTagsAndCategory_returnsArticleResponse() {
        Tag tag1 = buildTag(1L, "Java");
        Tag tag2 = buildTag(2L, "Spring Boot");
        User author = buildUser(1L, "Admin");
        Category category = buildCategory(1L, "Công nghệ");

        CreateArticleRequest request = new CreateArticleRequest(
                "Java Guide", "java-guide", "Java content",
                "thumb.jpg", (byte) 0, (byte) 1, 1L, 1L, List.of(1L, 2L));

        Article saved = buildArticle(5L, "Java Guide", "java-guide",
                (byte) 0, (byte) 1, author, category, List.of(tag1, tag2));

        when(articleRepository.existsBySlug("java-guide")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(tagRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(tag1, tag2));
        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        ArticleResponse response = articleService.create(request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.author().id()).isEqualTo(1L);
        assertThat(response.category().id()).isEqualTo(1L);
        assertThat(response.tags()).hasSize(2);
        assertThat(response.tags().get(0).name()).isEqualTo("Java");
    }

    @Test
    @DisplayName("create - null active defaults to 0")
    void create_nullActive_defaultsToZero() {
        CreateArticleRequest request = new CreateArticleRequest(
                "Article", "article-slug", null,
                null, (byte) 0, null, 1L, null, List.of());

        User author = buildUser(1L, "Admin");
        Article saved = buildArticle(1L, "Article", "article-slug", (byte) 0, (byte) 0, author, null, List.of());

        when(articleRepository.existsBySlug("article-slug")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        ArticleResponse response = articleService.create(request);

        assertThat(response.active()).isEqualTo((byte) 0);
    }

    @Test
    @DisplayName("create - duplicate slug: throws DuplicateResourceException")
    void create_duplicateSlug_throwsDuplicateResourceException() {
        CreateArticleRequest request = new CreateArticleRequest(
                "Article", "dup-slug", null, null, (byte) 0, null, 1L, null, List.of());

        when(articleRepository.existsBySlug("dup-slug")).thenReturn(true);

        assertThatThrownBy(() -> articleService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - author not found: throws ResourceNotFoundException")
    void create_authorNotFound_throwsResourceNotFoundException() {
        CreateArticleRequest request = new CreateArticleRequest(
                "Article", "article-slug", null, null, (byte) 0, null, 99L, null, List.of());

        when(articleRepository.existsBySlug("article-slug")).thenReturn(false);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - category not found: throws ResourceNotFoundException")
    void create_categoryNotFound_throwsResourceNotFoundException() {
        CreateArticleRequest request = new CreateArticleRequest(
                "Article", "article-slug", null, null, (byte) 0, null, 1L, 99L, List.of());

        when(articleRepository.existsBySlug("article-slug")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L, "Admin")));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - tag not found: throws ResourceNotFoundException")
    void create_tagNotFound_throwsResourceNotFoundException() {
        CreateArticleRequest request = new CreateArticleRequest(
                "Article", "article-slug", null, null, (byte) 0, null, 1L, null, List.of(1L, 99L));

        when(articleRepository.existsBySlug("article-slug")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L, "Admin")));
        when(tagRepository.findAllById(List.of(1L, 99L))).thenReturn(List.of(buildTag(1L, "Java")));

        assertThatThrownBy(() -> articleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(articleRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update - success: updates all fields and returns response")
    void update_success_returnsUpdatedArticleResponse() {
        User author = buildUser(1L, "Admin");
        Article existing = buildArticle(1L, "Old Title", "old-slug", (byte) 0, (byte) 0, author, null, List.of());
        Article updated = buildArticle(1L, "New Title", "new-slug", (byte) 1, (byte) 1, author, null, List.of());

        UpdateArticleRequest request = new UpdateArticleRequest(
                1L, "New Title", "new-slug", "Updated content",
                null, (byte) 1, (byte) 1, 1L, null, List.of());

        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(articleRepository.existsBySlugAndIdNot("new-slug", 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(articleRepository.save(any(Article.class))).thenReturn(updated);

        ArticleResponse response = articleService.update(request);

        assertThat(response.title()).isEqualTo("New Title");
        assertThat(response.slug()).isEqualTo("new-slug");
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    @Test
    @DisplayName("update - not found: throws ResourceNotFoundException")
    void update_notFound_throwsResourceNotFoundException() {
        UpdateArticleRequest request = new UpdateArticleRequest(
                99L, "Title", "slug", null, null, (byte) 0, null, 1L, null, List.of());

        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - duplicate slug on different article: throws DuplicateResourceException")
    void update_duplicateSlug_throwsDuplicateResourceException() {
        User author = buildUser(1L, "Admin");
        Article existing = buildArticle(1L, "Title", "old-slug", (byte) 0, (byte) 0, author, null, List.of());
        UpdateArticleRequest request = new UpdateArticleRequest(
                1L, "Title", "taken-slug", null, null, (byte) 0, null, 1L, null, List.of());

        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(articleRepository.existsBySlugAndIdNot("taken-slug", 1L)).thenReturn(true);

        assertThatThrownBy(() -> articleService.update(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(articleRepository, never()).save(any());
    }

    // ========== getById ==========

    @Test
    @DisplayName("getById - success: returns article response")
    void getById_success_returnsArticleResponse() {
        User author = buildUser(1L, "Admin");
        Article article = buildArticle(1L, "Spring Boot Tutorial", "spring-boot-tutorial",
                (byte) 0, (byte) 1, author, null, List.of());

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        ArticleResponse response = articleService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Spring Boot Tutorial");
    }

    @Test
    @DisplayName("getById - not found: throws ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== filter ==========

    @Test
    @DisplayName("filter - success: returns paginated result")
    void filter_success_returnsPaginatedResult() {
        User author = buildUser(1L, "Admin");
        Article article = buildArticle(1L, "Title", "slug", (byte) 0, (byte) 1, author, null, List.of());
        Page<Article> page = new PageImpl<>(List.of(article), PageRequest.of(0, 10), 1);
        ArticleFilterRequest filter = new ArticleFilterRequest(null);

        when(articleRepository.findBy(
                any(org.springframework.data.jpa.domain.PredicateSpecification.class),
                any(java.util.function.Function.class))).thenReturn(page);

        ResultPaginationDTO result = articleService.filter(filter, PageRequest.of(0, 10));

        assertThat(result.meta().total()).isEqualTo(1);
        assertThat(result.result()).hasSize(1);
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete - success: calls deleteById once")
    void delete_success_callsDeleteById() {
        when(articleRepository.existsById(1L)).thenReturn(true);

        articleService.delete(1L);

        verify(articleRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete - not found: throws ResourceNotFoundException")
    void delete_notFound_throwsResourceNotFoundException() {
        when(articleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> articleService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(articleRepository, never()).deleteById(any());
    }

    // ========== helpers ==========

    private Article buildArticle(Long id, String title, String slug,
            byte type, byte active, User author, Category category, List<Tag> tags) {
        Article article = new Article();
        article.setId(id);
        article.setTitle(title);
        article.setSlug(slug);
        article.setType(type);
        article.setActive(active);
        article.setViews(0);
        article.setAuthor(author);
        article.setCategory(category);
        tags.stream()
                .map(tag -> new TagArticle(article, tag))
                .forEach(article.getTagArticles()::add);
        return article;
    }

    private User buildUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private Category buildCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    private Tag buildTag(Long id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }
}
