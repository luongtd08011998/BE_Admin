package vn.hoidanit.springrestwithai.feature.document;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.document.dto.CreateDocumentRequest;
import vn.hoidanit.springrestwithai.feature.document.dto.DocumentResponse;
import vn.hoidanit.springrestwithai.feature.document.dto.UpdateDocumentRequest;

public interface DocumentService {

    DocumentResponse create(CreateDocumentRequest request);

    DocumentResponse update(UpdateDocumentRequest request);

    DocumentResponse getById(Long id);

    ResultPaginationDTO getAll(Pageable pageable);

    void delete(Long id);
}
