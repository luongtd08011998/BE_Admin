package vn.hoidanit.springrestwithai.feature.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.article.dto.ArticleResponse;
import vn.hoidanit.springrestwithai.feature.article.dto.CreateArticleRequest;
import vn.hoidanit.springrestwithai.feature.article.dto.UpdateArticleRequest;
import vn.hoidanit.springrestwithai.feature.category.Category;
import vn.hoidanit.springrestwithai.feature.category.CategoryRepository;
import vn.hoidanit.springrestwithai.feature.tag.Tag;
import vn.hoidanit.springrestwithai.feature.tag.TagRepository;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public ArticleResponse create(CreateArticleRequest request) {
        if (articleRepository.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("Bài viết", "slug", request.slug());
        }

        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", request.authorId()));

        Category category = resolveCategory(request.categoryId());
        List<Tag> tags = resolveTags(request.tagIds());

        Article article = new Article();
        article.setTitle(request.title());
        article.setSlug(request.slug());
        article.setContent(request.content());
        article.setThumbnail(request.thumbnail());
        article.setType(request.type());
        article.setActive(request.active() != null ? request.active() : (byte) 0);
        article.setViews(0);
        article.setAuthor(author);
        article.setCategory(category);

        List<TagArticle> tagArticles = tags.stream()
                .map(tag -> new TagArticle(article, tag))
                .collect(Collectors.toCollection(ArrayList::new));
        article.setTagArticles(tagArticles);

        Article saved = articleRepository.save(article);
        return ArticleResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ArticleResponse update(UpdateArticleRequest request) {
        Article article = articleRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", "id", request.id()));

        if (articleRepository.existsBySlugAndIdNot(request.slug(), request.id())) {
            throw new DuplicateResourceException("Bài viết", "slug", request.slug());
        }

        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", request.authorId()));

        Category category = resolveCategory(request.categoryId());
        List<Tag> tags = resolveTags(request.tagIds());

        article.setTitle(request.title());
        article.setSlug(request.slug());
        article.setContent(request.content());
        article.setThumbnail(request.thumbnail());
        article.setType(request.type());
        if (request.active() != null) {
            article.setActive(request.active());
        }
        article.setAuthor(author);
        article.setCategory(category);

        // Replace tags via orphanRemoval
        article.getTagArticles().clear();
        tags.stream()
                .map(tag -> new TagArticle(article, tag))
                .forEach(article.getTagArticles()::add);

        Article saved = articleRepository.save(article);
        return ArticleResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ArticleResponse getById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", "id", id));
        return ArticleResponse.fromEntity(article);
    }

    @Override
    @Transactional
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<ArticleResponse> pageResult = articleRepository.findAll(pageable)
                .map(ArticleResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bài viết", "id", id);
        }
        articleRepository.deleteById(id);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", categoryId));
    }

    private List<Tag> resolveTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> uniqueIds = tagIds.stream().distinct().toList();
        List<Tag> found = tagRepository.findAllById(uniqueIds);

        if (found.size() != uniqueIds.size()) {
            Set<Long> foundIds = found.stream()
                    .map(Tag::getId)
                    .collect(Collectors.toSet());
            Long missingId = uniqueIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElseThrow();
            throw new ResourceNotFoundException("Tag", "id", missingId);
        }

        return found;
    }
}
