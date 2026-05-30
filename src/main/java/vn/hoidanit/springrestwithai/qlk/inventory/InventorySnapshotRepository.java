package vn.hoidanit.springrestwithai.qlk.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventorySnapshotRepository extends JpaRepository<InventorySnapshot, Long> {
    Page<InventorySnapshot> findByWarehouseId(Long warehouseId, Pageable pageable);
    Optional<InventorySnapshot> findByWarehouseIdAndPeriodAndMaterialId(Long warehouseId, String period, Long materialId);
}
