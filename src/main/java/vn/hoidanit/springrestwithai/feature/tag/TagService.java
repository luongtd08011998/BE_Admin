package vn.hoidanit.springrestwithai.feature.tag;

import org.springframework.data.domain.Pageable;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.tag.dto.CreateTagRequest;
import vn.hoidanit.springrestwithai.feature.tag.dto.TagFilterRequest;
import vn.hoidanit.springrestwithai.feature.tag.dto.TagResponse;
import vn.hoidanit.springrestwithai.feature.tag.dto.UpdateTagRequest;

public interface TagService {

    TagResponse create(CreateTagRequest request);

    TagResponse update(UpdateTagRequest request);

    TagResponse getById(Long id);

    ResultPaginationDTO filter(TagFilterRequest filter, Pageable pageable);

    void delete(Long id);
}
