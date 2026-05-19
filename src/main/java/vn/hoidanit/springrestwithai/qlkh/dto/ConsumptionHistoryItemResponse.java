package vn.hoidanit.springrestwithai.qlkh.dto;

public record ConsumptionHistoryItemResponse(
        String yearMonth,
        Integer oldVal,
        Integer newVal,
        Integer consumptionM3) {
}
