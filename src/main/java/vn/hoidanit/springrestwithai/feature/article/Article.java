package vn.hoidanit.springrestwithai.feature.article;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

import vn.hoidanit.springrestwithai.feature.category.Category;
import vn.hoidanit.springrestwithai.feature.document.Document;
import vn.hoidanit.springrestwithai.feature.user.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "slug" })
})
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private String thumbnail;

    @Column(nullable = false)
    private byte type;   // 0=bài viết thường, 1=bài viết nổi bật, 2=tin tức

    private int views;

    @Column(nullable = false)
    private byte active; // 0=Nháp, 1=Published, 2=Ẩn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TagArticle> tagArticles = new ArrayList<>();

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Document> documents = new ArrayList<>();

    private Instant createdAt;

    private Instant updatedAt;

    public Article() {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public byte getActive() {
        return active;
    }

    public void setActive(byte active) {
        this.active = active;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<TagArticle> getTagArticles() {
        return tagArticles;
    }

    public void setTagArticles(List<TagArticle> tagArticles) {
        this.tagArticles = tagArticles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
