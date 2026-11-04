package vn.hoidanit.springrestwithai.gv.department;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvDepartmentRepository extends JpaRepository<GvDepartment, Long> {

    Optional<GvDepartment> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<GvDepartment> findByIsActiveTrue();

    List<GvDepartment> findByParentId(Long parentId);

    List<GvDepartment> findByParentIsNull();
}
