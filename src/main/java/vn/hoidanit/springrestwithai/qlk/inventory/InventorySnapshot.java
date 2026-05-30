package vn.hoidanit.springrestwithai.qlk.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.springrestwithai.qlk.material.Material;
import vn.hoidanit.springrestwithai.qlk.warehouse.Warehouse;

@Entity
@Table(name = "qlk_inventory_snapshots", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"warehouse_id", "period", "material_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventorySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, length = 10)
    private String period; // e.g. "2026-05"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "opening_quantity")
    private Integer openingQuantity;

    @Column(name = "inbound_sum")
    private Integer inboundSum;

    @Column(name = "outbound_sum")
    private Integer outboundSum;

    @Column(name = "closing_quantity")
    private Integer closingQuantity;
}
