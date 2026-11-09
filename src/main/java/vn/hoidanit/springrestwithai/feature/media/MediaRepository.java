package vn.hoidanit.springrestwithai.feature.media;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface MediaRepository extends JpaRepository<Media, Long>, JpaSpecificationExecutor<Media> {

    Optional<Media> findByFileUrl(String fileUrl);

    List<Media> findByFolderId(Long folderId);

    long countByFolderId(Long folderId);

    @Query("SELECT m.folderId, COUNT(m) FROM Media m WHERE m.folderId IS NOT NULL GROUP BY m.folderId")
    List<Object[]> countGroupByFolderId();
}
