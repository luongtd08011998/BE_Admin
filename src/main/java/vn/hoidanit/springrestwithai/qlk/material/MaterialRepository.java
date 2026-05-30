package vn.hoidanit.springrestwithai.qlk.material;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByCode(String code);
    boolean existsByCode(String code);
    
    Page<Material> findByCategoryWarehouseId(Long warehouseId, Pageable pageable);
    Optional<Material> findByIdAndCategoryWarehouseId(Long id, Long warehouseId);
    long countByCategoryWarehouseId(Long warehouseId);
}
