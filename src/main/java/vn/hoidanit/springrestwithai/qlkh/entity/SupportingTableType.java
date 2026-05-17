package vn.hoidanit.springrestwithai.qlkh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "supportingtabletype")
public class SupportingTableType {

    @Id
    @Column(name = "SupportingTableTypeId")
    private Integer supportingTableTypeId;

    @Column(name = "EnumName")
    private String enumName;

    @Column(name = "DisplayName")
    private String displayName;

    public Integer getSupportingTableTypeId() {
        return supportingTableTypeId;
    }

    public void setSupportingTableTypeId(Integer supportingTableTypeId) {
        this.supportingTableTypeId = supportingTableTypeId;
    }

    public String getEnumName() {
        return enumName;
    }

    public void setEnumName(String enumName) {
        this.enumName = enumName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
