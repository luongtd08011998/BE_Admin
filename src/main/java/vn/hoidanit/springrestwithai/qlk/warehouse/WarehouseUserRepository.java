package vn.hoidanit.springrestwithai.qlk.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseUserRepository extends JpaRepository<WarehouseUser, Long> {
    boolean existsByWarehouseIdAndUserId(Long warehouseId, Long userId);
    List<WarehouseUser> findByUserId(Long userId);
    List<WarehouseUser> findByWarehouseId(Long warehouseId);
    void deleteByWarehouseId(Long warehouseId);
}
