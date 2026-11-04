package vn.hoidanit.springrestwithai.gv.label;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvLabelRepository extends JpaRepository<GvLabel, Long> {

    Optional<GvLabel> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<GvLabel> findByIsActiveTrue();
}
