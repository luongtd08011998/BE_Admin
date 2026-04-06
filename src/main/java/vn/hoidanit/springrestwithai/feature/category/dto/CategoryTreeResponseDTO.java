package vn.hoidanit.springrestwithai.feature.category.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CategoryTreeResponseDTO {
    private Long id;
    private String name;
    private String slug;
    private Instant createdAt;
    private Instant updatedAt;

    private List<CategoryTreeResponseDTO> children = new ArrayList<>();

    private CategoryTreeResponseDTO parent; // 👈 Duy nhất 1 cha

    public CategoryTreeResponseDTO(Long id, String name, String slug, Instant createAt, Instant updateAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.createdAt =  createAt;
        this.updatedAt = updateAt;
    }

    public CategoryTreeResponseDTO(Long id, String name, String slug, Instant createdAt, Instant updatedAt, List<CategoryTreeResponseDTO> children, CategoryTreeResponseDTO parent) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.children = children;
        this.parent = parent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<CategoryTreeResponseDTO> getChildren() { return children; }
    public void setChildren(List<CategoryTreeResponseDTO> children) { this.children = children; }

    public CategoryTreeResponseDTO getParent() { return parent; }
    public void setParent(CategoryTreeResponseDTO parent) { this.parent = parent; }
}
