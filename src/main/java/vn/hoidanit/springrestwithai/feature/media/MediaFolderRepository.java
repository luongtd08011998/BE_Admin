package vn.hoidanit.springrestwithai.feature.media;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFolderRepository extends JpaRepository<MediaFolder, Long> {

    List<MediaFolder> findByParentIdOrderByNameAsc(Long parentId);

    List<MediaFolder> findByParentIdIsNullOrderByNameAsc();

    List<MediaFolder> findAllByOrderByNameAsc();

    Optional<MediaFolder> findByNameIgnoreCaseAndParentId(String name, Long parentId);

    Optional<MediaFolder> findByNameIgnoreCaseAndParentIdIsNull(String name);

    List<MediaFolder> findByParentId(Long parentId);
}

