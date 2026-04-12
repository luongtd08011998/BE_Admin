package vn.hoidanit.springrestwithai.feature.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    
}
