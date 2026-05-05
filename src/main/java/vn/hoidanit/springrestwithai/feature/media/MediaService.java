package vn.hoidanit.springrestwithai.feature.media;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaFilterRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaResponse;

public interface MediaService {

    MediaResponse upload(MultipartFile file, String title, Long userId);

    ResultPaginationDTO findAll(MediaFilterRequest filter, Pageable pageable);

    MediaResponse findById(Long id);

    void delete(Long id);
}
