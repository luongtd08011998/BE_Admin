package vn.hoidanit.springrestwithai.feature.document;

import org.springframework.data.domain.Pageable;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.document.dto.CreateDocumentRequest;
import vn.hoidanit.springrestwithai.feature.document.dto.DocumentFilterRequest;
import vn.hoidanit.springrestwithai.feature.document.dto.DocumentResponse;
import vn.hoidanit.springrestwithai.feature.document.dto.UpdateDocumentRequest;

import java.util.List;

public interface DocumentService {

    DocumentResponse create(CreateDocumentRequest request);

    DocumentResponse update(UpdateDocumentRequest request);

    DocumentResponse getById(Long id);

    ResultPaginationDTO filter(DocumentFilterRequest filter, Pageable pageable);

    List<DocumentResponse> getByArticleId(Long articleId);

    void delete(Long id);
}
