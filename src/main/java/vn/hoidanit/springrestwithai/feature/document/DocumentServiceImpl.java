package vn.hoidanit.springrestwithai.feature.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.article.Article;
import vn.hoidanit.springrestwithai.feature.article.ArticleRepository;
import vn.hoidanit.springrestwithai.feature.document.dto.CreateDocumentRequest;
import vn.hoidanit.springrestwithai.feature.document.dto.DocumentResponse;
import vn.hoidanit.springrestwithai.feature.document.dto.UpdateDocumentRequest;

import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final ArticleRepository articleRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository, ArticleRepository articleRepository) {
        this.documentRepository = documentRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional
    public DocumentResponse create(CreateDocumentRequest request) {
        Article article = articleRepository.findById(request.articleId())
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", "id", request.articleId()));

        Document document = new Document();
        document.setTitle(request.title());
        document.setDescription(request.description());
        document.setDocumentUrl(request.documentUrl());
        document.setArticle(article);

        return DocumentResponse.fromEntity(documentRepository.save(document));
    }

    @Override
    @Transactional
    public DocumentResponse update(UpdateDocumentRequest request) {
        Document document = documentRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu", "id", request.id()));

        Article article = articleRepository.findById(request.articleId())
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", "id", request.articleId()));

        document.setTitle(request.title());
        document.setDescription(request.description());
        document.setDocumentUrl(request.documentUrl());
        document.setArticle(article);

        return DocumentResponse.fromEntity(documentRepository.save(document));
    }

    @Override
    @Transactional
    public DocumentResponse getById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu", "id", id));
        return DocumentResponse.fromEntity(document);
    }

    @Override
    @Transactional
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<DocumentResponse> pageResult = documentRepository.findAll(pageable)
                .map(DocumentResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    @Transactional
    public List<DocumentResponse> getByArticleId(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new ResourceNotFoundException("Bài viết", "id", articleId);
        }
        return documentRepository.findAllByArticleId(articleId).stream()
                .map(DocumentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tài liệu", "id", id);
        }
        documentRepository.deleteById(id);
    }
}
