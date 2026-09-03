package vn.hoidanit.springrestwithai.feature.appconfig;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "app_configs")
public class AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String platform; // "IOS" or "ANDROID"

    private String latestVersion; // SemVer for iOS
    
    private Integer latestVersionCode; // Integer for Android

    private String minRequiredVersion; // SemVer for iOS
    
    private Integer minRequiredVersionCode; // Integer for Android

    private String storeUrl;

    @Column(columnDefinition = "TEXT")
    private String releaseNotes;

    @Column(nullable = false)
    private Integer active = 1;

    private Instant createdAt;

    private Instant updatedAt;

    public AppConfig() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public Integer getLatestVersionCode() {
        return latestVersionCode;
    }

    public void setLatestVersionCode(Integer latestVersionCode) {
        this.latestVersionCode = latestVersionCode;
    }

    public String getMinRequiredVersion() {
        return minRequiredVersion;
    }

    public void setMinRequiredVersion(String minRequiredVersion) {
        this.minRequiredVersion = minRequiredVersion;
    }

    public Integer getMinRequiredVersionCode() {
        return minRequiredVersionCode;
    }

    public void setMinRequiredVersionCode(Integer minRequiredVersionCode) {
        this.minRequiredVersionCode = minRequiredVersionCode;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
