package vn.hoidanit.springrestwithai.qlk.dashboard.dto;

public record QlkDashboardResponse(
        long totalWarehouses,
        long totalMaterials,
        long totalSuppliers,
        long totalVouchers) {
}
