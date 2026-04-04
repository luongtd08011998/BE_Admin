package vn.hoidanit.springrestwithai.feature.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.category.Category;
import vn.hoidanit.springrestwithai.feature.category.CategoryRepository;
import vn.hoidanit.springrestwithai.feature.document.dto.CreateDocumentRequest;
import vn.hoidanit.springrestwithai.feature.document.dto.DocumentResponse;
import vn.hoidanit.springrestwithai.feature.document.dto.UpdateDocumentRequest;
import vn.hoidanit.springrestwithai.util.constant.DocumentStatus;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final CategoryRepository categoryRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository, CategoryRepository categoryRepository) {
        this.documentRepository = documentRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public DocumentResponse create(CreateDocumentRequest request) {
        if (documentRepository.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("Bài viết", "slug", request.slug());
        }

        Document document = new Document();
        document.setTitle(request.title());
        document.setSlug(request.slug());
        document.setContent(request.content());
        document.setSummary(request.summary());
        document.setThumbnail(request.thumbnail());
        document.setStatus(request.status() != null ? request.status() : DocumentStatus.DRAFT);

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", request.categoryId()));
            document.setCategory(category);
        }

        return DocumentResponse.fromEntity(documentRepository.save(document));
    }

    @Override
    @Transactional
    public DocumentResponse update(UpdateDocumentRequest request) {
        Document document = documentRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", "id", request.id()));

        if (documentRepository.existsBySlugAndIdNot(request.slug(), request.id())) {
            throw new DuplicateResourceException("Bài viết", "slug", request.slug());
        }

        document.setTitle(request.title());
        document.setSlug(request.slug());
        document.setContent(request.content());
        document.setSummary(request.summary());
        document.setThumbnail(request.thumbnail());
        if (request.status() != null) {
            document.setStatus(request.status());
        }

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", request.categoryId()));
            document.setCategory(category);
        } else {
            document.setCategory(null);
        }

        return DocumentResponse.fromEntity(documentRepository.save(document));
    }

    @Override
    public DocumentResponse getById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", "id", id));
        return DocumentResponse.fromEntity(document);
    }

    @Override
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<DocumentResponse> pageResult = documentRepository.findAll(pageable)
                .map(DocumentResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bài viết", "id", id);
        }
        documentRepository.deleteById(id);
    }
}
