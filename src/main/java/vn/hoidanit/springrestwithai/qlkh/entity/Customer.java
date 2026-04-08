package vn.hoidanit.springrestwithai.qlkh.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "Code")
    private String code;

    @Column(name = "DigiCode")
    private String digiCode;

    @Column(name = "Name")
    private String name;

    @Column(name = "ShortName")
    private String shortName;

    @Column(name = "Phone")
    private String phone;

    @Column(name = "Address")
    private String address;

    @Column(name = "Email")
    private String email;

    @Column(name = "IsActive")
    private Short isActive;

    @Column(name = "Balance")
    private Double balance;

    @Column(name = "TaxCode")
    private String taxCode;

    @Column(name = "ContactName")
    private String contactName;

    @Column(name = "ContactPhone")
    private String contactPhone;

    @Column(name = "Status")
    private Integer status;

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDigiCode() { return digiCode; }
    public void setDigiCode(String digiCode) { this.digiCode = digiCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Short getIsActive() { return isActive; }
    public void setIsActive(Short isActive) { this.isActive = isActive; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
