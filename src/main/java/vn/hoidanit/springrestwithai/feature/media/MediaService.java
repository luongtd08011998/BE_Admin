package vn.hoidanit.springrestwithai.feature.media;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.media.dto.CreateMediaFolderRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaFilterRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaFolderResponse;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaResponse;
import vn.hoidanit.springrestwithai.feature.media.dto.MoveMediaRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.UpdateMediaFolderRequest;

public interface MediaService {

    MediaResponse upload(MultipartFile file, String title, Long userId);

    MediaResponse upload(MultipartFile file, String title, Long folderId, Long userId);

    ResultPaginationDTO findAll(MediaFilterRequest filter, Pageable pageable);

    MediaResponse findById(Long id);

    void delete(Long id);

    void moveMedia(MoveMediaRequest request);

    List<MediaFolderResponse> getFolders(Long parentId, boolean all);

    MediaFolderResponse createFolder(CreateMediaFolderRequest request, Long userId);

    MediaFolderResponse updateFolder(Long id, UpdateMediaFolderRequest request);

    void deleteFolder(Long id);
}
