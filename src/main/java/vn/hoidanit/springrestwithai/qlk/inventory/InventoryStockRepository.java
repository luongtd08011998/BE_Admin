package vn.hoidanit.springrestwithai.qlk.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {
    Optional<InventoryStock> findByWarehouseIdAndMaterialId(Long warehouseId, Long materialId);
    Page<InventoryStock> findByWarehouseId(Long warehouseId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM InventoryStock s WHERE s.warehouse.id = :warehouseId AND (:search IS NULL OR LOWER(s.material.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.material.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<InventoryStock> findByWarehouseIdAndSearch(@org.springframework.data.repository.query.Param("warehouseId") Long warehouseId, @org.springframework.data.repository.query.Param("search") String search, Pageable pageable);
}
