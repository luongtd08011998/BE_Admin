package vn.hoidanit.springrestwithai.qlk.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QlkCategoryRepository extends JpaRepository<QlkCategory, Long> {

    Page<QlkCategory> findByWarehouseId(Long warehouseId, Pageable pageable);

    boolean existsByNameAndWarehouseId(String name, Long warehouseId);

    boolean existsByNameAndWarehouseIdAndIdNot(String name, Long warehouseId, Long id);
}
