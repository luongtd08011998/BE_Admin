package vn.hoidanit.springrestwithai.qlkh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "supportingtable")
public class SupportingTable {

    @Id
    @Column(name = "SupportingTableId")
    private Integer supportingTableId;

    @Column(name = "Name")
    private String name;

    @Column(name = "Code")
    private String code;

    @Column(name = "Type")
    private Integer type;

    @Column(name = "SupportingTableTypeId")
    private Integer supportingTableTypeId;

    @Column(name = "IsActive")
    private Short isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupportingTableTypeId", referencedColumnName = "SupportingTableTypeId", insertable = false, updatable = false)
    private SupportingTableType supportingTableType;

    public Integer getSupportingTableId() {
        return supportingTableId;
    }

    public void setSupportingTableId(Integer supportingTableId) {
        this.supportingTableId = supportingTableId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSupportingTableTypeId() {
        return supportingTableTypeId;
    }

    public void setSupportingTableTypeId(Integer supportingTableTypeId) {
        this.supportingTableTypeId = supportingTableTypeId;
    }

    public Short getIsActive() {
        return isActive;
    }

    public void setIsActive(Short isActive) {
        this.isActive = isActive;
    }

    public SupportingTableType getSupportingTableType() {
        return supportingTableType;
    }

    public void setSupportingTableType(SupportingTableType supportingTableType) {
        this.supportingTableType = supportingTableType;
    }
}
