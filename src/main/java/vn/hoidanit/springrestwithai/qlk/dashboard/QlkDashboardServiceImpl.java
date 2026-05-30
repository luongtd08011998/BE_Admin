package vn.hoidanit.springrestwithai.qlk.dashboard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.qlk.dashboard.dto.QlkDashboardResponse;
import vn.hoidanit.springrestwithai.qlk.warehouse.WarehouseRepository;
import vn.hoidanit.springrestwithai.qlk.material.MaterialRepository;
import vn.hoidanit.springrestwithai.qlk.supplier.SupplierRepository;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.StockVoucherRepository;

import vn.hoidanit.springrestwithai.security.JwtUtil;

@Service
public class QlkDashboardServiceImpl implements QlkDashboardService {

    private final WarehouseRepository warehouseRepository;
    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final StockVoucherRepository stockVoucherRepository;

    public QlkDashboardServiceImpl(
            WarehouseRepository warehouseRepository,
            MaterialRepository materialRepository,
            SupplierRepository supplierRepository,
            StockVoucherRepository stockVoucherRepository) {
        this.warehouseRepository = warehouseRepository;
        this.materialRepository = materialRepository;
        this.supplierRepository = supplierRepository;
        this.stockVoucherRepository = stockVoucherRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public QlkDashboardResponse getDashboard() {
        Long warehouseId = JwtUtil.extractWarehouseIdOrNull();

        long totalWarehouses = warehouseId != null ? 1 : warehouseRepository.count();
        long totalMaterials = warehouseId != null ? materialRepository.countByCategoryWarehouseId(warehouseId) : materialRepository.count();
        long totalSuppliers = supplierRepository.count(); // Suppliers are global
        long totalVouchers = warehouseId != null ? stockVoucherRepository.countByWarehouseId(warehouseId) : stockVoucherRepository.count();

        return new QlkDashboardResponse(totalWarehouses, totalMaterials, totalSuppliers, totalVouchers);
    }
}
