package vn.hoidanit.springrestwithai.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import vn.hoidanit.springrestwithai.feature.article.Article;
import vn.hoidanit.springrestwithai.feature.article.ArticleRepository;
import vn.hoidanit.springrestwithai.feature.article.TagArticle;
import vn.hoidanit.springrestwithai.feature.category.Category;
import vn.hoidanit.springrestwithai.feature.category.CategoryRepository;
import vn.hoidanit.springrestwithai.feature.tag.Tag;
import vn.hoidanit.springrestwithai.feature.tag.TagRepository;
import vn.hoidanit.springrestwithai.feature.company.Company;
import vn.hoidanit.springrestwithai.feature.company.CompanyRepository;
import vn.hoidanit.springrestwithai.feature.document.Document;
import vn.hoidanit.springrestwithai.feature.document.DocumentRepository;
import vn.hoidanit.springrestwithai.feature.permission.Permission;
import vn.hoidanit.springrestwithai.feature.permission.PermissionRepository;
import vn.hoidanit.springrestwithai.feature.role.Role;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.util.constant.DocumentStatus;
import vn.hoidanit.springrestwithai.util.constant.GenderEnum;

/**
 * Seeds initial data on application startup.
 * Only runs when app.seed-data=true (dev/test environments).
 * Skips if data already exists (checks user count).
 */
@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private static final String DEFAULT_PASSWORD = "123456";

    private final CategoryRepository categoryRepository;
    private final DocumentRepository documentRepository;
    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final PermissionRepository permissionRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(
            CategoryRepository categoryRepository,
            DocumentRepository documentRepository,
            ArticleRepository articleRepository,
            TagRepository tagRepository,
            PermissionRepository permissionRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.documentRepository = documentRepository;
        this.articleRepository = articleRepository;
        this.tagRepository = tagRepository;
        this.permissionRepository = permissionRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info(">>> Database already seeded — skipping");
            return;
        }

        log.info(">>> Seeding database...");

        List<Permission> permissions = seedPermissions();
        List<Company> companies = seedCompanies();
        List<Role> roles = seedRoles(permissions);
        seedUsers(roles, companies);
        List<Category> categories = seedCategories();
        seedDocuments(categories);
        List<Tag> tags = seedTags();
        seedArticles(categories, tags);

        log.info(">>> Database seeded successfully");
    }

    // ═══════════════════════════════════════
    // Step 1: Permissions (31 records)
    // ═══════════════════════════════════════

    private List<Permission> seedPermissions() {
        List<Permission> permissions = List.of(
                // USER module
                createPermission("CREATE_USER", "/api/v1/users", "POST", "USER"),
                createPermission("UPDATE_USER", "/api/v1/users", "PUT", "USER"),
                createPermission("DELETE_USER", "/api/v1/users/{id}", "DELETE", "USER"),
                createPermission("VIEW_USERS", "/api/v1/users", "GET", "USER"),
                createPermission("VIEW_USER", "/api/v1/users/{id}", "GET", "USER"),

                // COMPANY module
                createPermission("CREATE_COMPANY", "/api/v1/companies", "POST", "COMPANY"),
                createPermission("UPDATE_COMPANY", "/api/v1/companies", "PUT", "COMPANY"),
                createPermission("DELETE_COMPANY", "/api/v1/companies/{id}", "DELETE", "COMPANY"),
                createPermission("VIEW_COMPANIES", "/api/v1/companies", "GET", "COMPANY"),
                createPermission("VIEW_COMPANY", "/api/v1/companies/{id}", "GET", "COMPANY"),

                // ROLE module
                createPermission("CREATE_ROLE", "/api/v1/roles", "POST", "ROLE"),
                createPermission("UPDATE_ROLE", "/api/v1/roles", "PUT", "ROLE"),
                createPermission("DELETE_ROLE", "/api/v1/roles/{id}", "DELETE", "ROLE"),
                createPermission("VIEW_ROLES", "/api/v1/roles", "GET", "ROLE"),
                createPermission("VIEW_ROLE", "/api/v1/roles/{id}", "GET", "ROLE"),

                // PERMISSION module
                createPermission("CREATE_PERMISSION", "/api/v1/permissions", "POST", "PERMISSION"),
                createPermission("UPDATE_PERMISSION", "/api/v1/permissions", "PUT", "PERMISSION"),
                createPermission("DELETE_PERMISSION", "/api/v1/permissions/{id}", "DELETE", "PERMISSION"),
                createPermission("VIEW_PERMISSIONS", "/api/v1/permissions", "GET", "PERMISSION"),
                createPermission("VIEW_PERMISSION", "/api/v1/permissions/{id}", "GET", "PERMISSION"),

                // FILE module
                createPermission("UPLOAD_FILE", "/api/v1/files", "POST", "FILE"),

                // CATEGORY module
                createPermission("CREATE_CATEGORY", "/api/v1/categories", "POST", "CATEGORY"),
                createPermission("UPDATE_CATEGORY", "/api/v1/categories", "PUT", "CATEGORY"),
                createPermission("DELETE_CATEGORY", "/api/v1/categories/{id}", "DELETE", "CATEGORY"),
                createPermission("VIEW_CATEGORIES", "/api/v1/categories", "GET", "CATEGORY"),
                createPermission("VIEW_CATEGORY", "/api/v1/categories/{id}", "GET", "CATEGORY"),

                // DOCUMENT module
                createPermission("CREATE_DOCUMENT", "/api/v1/documents", "POST", "DOCUMENT"),
                createPermission("UPDATE_DOCUMENT", "/api/v1/documents", "PUT", "DOCUMENT"),
                createPermission("DELETE_DOCUMENT", "/api/v1/documents/{id}", "DELETE", "DOCUMENT"),
                createPermission("VIEW_DOCUMENTS", "/api/v1/documents", "GET", "DOCUMENT"),
                createPermission("VIEW_DOCUMENT", "/api/v1/documents/{id}", "GET", "DOCUMENT"),

                // TAG module
                createPermission("CREATE_TAG", "/api/v1/tags", "POST", "TAG"),
                createPermission("UPDATE_TAG", "/api/v1/tags", "PUT", "TAG"),
                createPermission("DELETE_TAG", "/api/v1/tags/{id}", "DELETE", "TAG"),
                createPermission("VIEW_TAGS", "/api/v1/tags", "GET", "TAG"),
                createPermission("VIEW_TAG", "/api/v1/tags/{id}", "GET", "TAG"),

                // ARTICLE module
                createPermission("CREATE_ARTICLE", "/api/v1/articles", "POST", "ARTICLE"),
                createPermission("UPDATE_ARTICLE", "/api/v1/articles", "PUT", "ARTICLE"),
                createPermission("DELETE_ARTICLE", "/api/v1/articles/{id}", "DELETE", "ARTICLE"),
                createPermission("VIEW_ARTICLES", "/api/v1/articles", "GET", "ARTICLE"),
                createPermission("VIEW_ARTICLE", "/api/v1/articles/{id}", "GET", "ARTICLE"));

        List<Permission> saved = permissionRepository.saveAll(permissions);
        log.info("Seeded {} permissions", saved.size());
        return saved;
    }

    private Permission createPermission(String name, String apiPath, String method, String module) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setApiPath(apiPath);
        permission.setMethod(method);
        permission.setModule(module);
        return permission;
    }

    // ═══════════════════════════════════════
    // Step 2: Companies (3 records)
    // ═══════════════════════════════════════

    private List<Company> seedCompanies() {
        List<Company> companies = List.of(
                createCompany("Công ty TNHH Cấp nước Tóc Tiên", "Cấp nước sinh hoạt", "Xã Châu Pha, Thành phố Hồ Chí Minh"));
                

        List<Company> saved = companyRepository.saveAll(companies);
        log.info("Seeded {} companies", saved.size());
        return saved;
    }

    private Company createCompany(String name, String description, String address) {
        Company company = new Company();
        company.setName(name);
        company.setDescription(description);
        company.setAddress(address);
        return company;
    }

    // ═══════════════════════════════════════
    // Step 3: Roles (4 records)
    // ═══════════════════════════════════════

    private List<Role> seedRoles(List<Permission> allPermissions) {
        // SUPER_ADMIN — all permissions
        Role superAdmin = createRole("SUPER_ADMIN", "Full system access", allPermissions);

        // HR — user management + view company + upload file + document management
        Role hr = createRole("HR", "Human resources management",
                filterPermissions(allPermissions,
                        "CREATE_USER", "UPDATE_USER", "VIEW_USERS", "VIEW_USER",
                        "VIEW_COMPANIES", "VIEW_COMPANY",
                        "VIEW_CATEGORIES", "VIEW_CATEGORY",
                        "CREATE_DOCUMENT", "UPDATE_DOCUMENT", "DELETE_DOCUMENT", "VIEW_DOCUMENTS", "VIEW_DOCUMENT",
                        "UPLOAD_FILE"));

        // MANAGER — view only
        Role manager = createRole("MANAGER", "Department manager",
                filterPermissions(allPermissions,
                        "VIEW_USERS", "VIEW_USER",
                        "VIEW_COMPANIES", "VIEW_COMPANY",
                        "VIEW_ROLES", "VIEW_ROLE",
                        "VIEW_CATEGORIES", "VIEW_CATEGORY",
                        "VIEW_DOCUMENTS", "VIEW_DOCUMENT"));

        // USER — basic access
        Role user = createRole("USER", "Regular employee",
                filterPermissions(allPermissions,
                        "VIEW_USER",
                        "VIEW_COMPANIES", "VIEW_COMPANY",
                        "VIEW_CATEGORIES", "VIEW_CATEGORY",
                        "VIEW_DOCUMENTS", "VIEW_DOCUMENT",
                        "VIEW_TAGS", "VIEW_TAG",
                        "UPLOAD_FILE"));

        List<Role> saved = roleRepository.saveAll(List.of(superAdmin, hr, manager, user));
        log.info("Seeded {} roles", saved.size());
        return saved;
    }

    private Role createRole(String name, String description, List<Permission> permissions) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions);
        return role;
    }

    private List<Permission> filterPermissions(List<Permission> all, String... names) {
        List<String> nameList = List.of(names);
        return all.stream()
                .filter(p -> nameList.contains(p.getName()))
                .toList();
    }

    // ═══════════════════════════════════════
    // Step 4: Users (4 records)
    // ═══════════════════════════════════════

    private void seedUsers(List<Role> allRoles, List<Company> allCompanies) {
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        Role superAdminRole = findRole(allRoles, "SUPER_ADMIN");
        Role hrRole = findRole(allRoles, "HR");
        Role managerRole = findRole(allRoles, "MANAGER");
        Role userRole = findRole(allRoles, "USER");

        Company TocTien = findCompany(allCompanies, "Công ty TNHH Cấp nước Tóc Tiên");
        

        List<User> users = List.of(
                createUser("Super Admin", "luongtd@toctienltd.vn", encodedPassword,
                        30, GenderEnum.MALE, "Ho Chi Minh City",
                        TocTien, List.of(superAdminRole)),

                createUser("HR Manager", "hr@toctienltd.vn", encodedPassword,
                        28, GenderEnum.FEMALE, "Ha Noi",
                        TocTien, List.of(hrRole)),

                createUser("Department Manager", "manager@toctienltd.vn", encodedPassword,
                        35, GenderEnum.MALE, "Da Nang",
                        TocTien, List.of(managerRole)),

                createUser("Normal User", "user@toctienltd.vn", encodedPassword,
                        25, GenderEnum.OTHER, "Ho Chi Minh City",
                        TocTien, List.of(userRole)));

        userRepository.saveAll(users);
        log.info("Seeded {} users (password for all: {})", users.size(), DEFAULT_PASSWORD);
    }

    private User createUser(String name, String email, String encodedPassword,
            int age, GenderEnum gender, String address,
            Company company, List<Role> roles) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setAge(age);
        user.setGender(gender);
        user.setAddress(address);
        user.setCompany(company);
        user.setRoles(roles);
        return user;
    }

    // ═══════════════════════════════════════
    // Step 5: Categories (4 records)
    // ═══════════════════════════════════════

    private List<Category> seedCategories() {
        List<Category> categories = List.of(
                createCategory("Thời sự", "thoi-su"),
                createCategory("Kinh tế", "kinh-te"),
                createCategory("Công nghệ", "cong-nghe"),
                createCategory("Thể thao", "the-thao"));

        List<Category> saved = categoryRepository.saveAll(categories);
        log.info("Seeded {} categories", saved.size());
        return saved;
    }

    // ═══════════════════════════════════════
    // Step 6: Documents (5 records)
    // ═══════════════════════════════════════

    private void seedDocuments(List<Category> categories) {
        Category thoiSu = findCategory(categories, "Thời sự");
        Category kinhTe = findCategory(categories, "Kinh tế");
        Category congNghe = findCategory(categories, "Công nghệ");
        Category theThao = findCategory(categories, "Thể thao");

        List<Document> documents = List.of(
                createDocument(
                        "Hướng dẫn nội quy công ty",
                        "huong-dan-noi-quy-cong-ty",
                        "Tài liệu mô tả chi tiết các quy định và nội quy của công ty dành cho toàn thể nhân viên.",
                        "Nội quy công ty bao gồm các quy định về giờ làm việc, trang phục, ứng xử và các chính sách liên quan đến nhân sự.",
                        DocumentStatus.PUBLISHED,
                        thoiSu),
                createDocument(
                        "Quy trình tuyển dụng nhân sự",
                        "quy-trinh-tuyen-dung-nhan-su",
                        "Tài liệu hướng dẫn quy trình tuyển dụng từ đăng tin, sàng lọc hồ sơ đến phỏng vấn và onboarding.",
                        "Quy trình tuyển dụng gồm 5 bước chính: xác định nhu cầu, đăng tuyển, sàng lọc, phỏng vấn và ký hợp đồng.",
                        DocumentStatus.PUBLISHED,
                        thoiSu),
                createDocument(
                        "Chính sách lương thưởng và phúc lợi",
                        "chinh-sach-luong-thuong-phuc-loi",
                        "Mô tả chi tiết cấu trúc lương, các loại thưởng và phúc lợi dành cho nhân viên công ty.",
                        "Chính sách lương thưởng bao gồm lương cơ bản, thưởng KPI, thưởng dự án và các phúc lợi như bảo hiểm, du lịch.",
                        DocumentStatus.PUBLISHED,
                        kinhTe),
                createDocument(
                        "Hướng dẫn sử dụng hệ thống quản lý nhân sự",
                        "huong-dan-su-dung-he-thong-quan-ly-nhan-su",
                        "Tài liệu hướng dẫn sử dụng phần mềm quản lý nhân sự nội bộ cho cán bộ HR.",
                        "Hướng dẫn bao gồm cách đăng nhập, quản lý hồ sơ nhân viên, theo dõi chấm công và tạo báo cáo.",
                        DocumentStatus.PUBLISHED,
                        congNghe),
                createDocument(
                        "Kế hoạch đào tạo và phát triển nhân viên 2025",
                        "ke-hoach-dao-tao-phat-trien-nhan-vien-2025",
                        "Phác thảo kế hoạch đào tạo kỹ năng chuyên môn và kỹ năng mềm cho nhân viên trong năm 2025.",
                        "Kế hoạch đào tạo 2025 tập trung vào nâng cao năng lực chuyên môn, kỹ năng lãnh đạo và phát triển bản thân cho toàn thể nhân viên.",
                        DocumentStatus.DRAFT,
                        theThao));

        documentRepository.saveAll(documents);
        log.info("Seeded {} documents", documents.size());
    }

    private Document createDocument(String title, String slug, String content, String summary,
            DocumentStatus status, Category category) {
        Document document = new Document();
        document.setTitle(title);
        document.setSlug(slug);
        document.setContent(content);
        document.setSummary(summary);
        document.setStatus(status);
        document.setCategory(category);
        return document;
    }

    private Category findCategory(List<Category> categories, String name) {
        return categories.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found: " + name));
    }

    private Category createCategory(String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        return category;
    }

    // ═══════════════════════════════════════
    // Step 7: Tags (5 records)
    // ═══════════════════════════════════════

    private List<Tag> seedTags() {
        List<Tag> tags = List.of(
                createTag("Java", "Ngôn ngữ lập trình Java"),
                createTag("Spring Boot", "Framework Spring Boot cho Java"),
                createTag("REST API", "Thiết kế và phát triển RESTful API"),
                createTag("MySQL", "Hệ quản trị cơ sở dữ liệu quan hệ MySQL"),
                createTag("Docker", "Đóng gói và triển khai ứng dụng với Docker container"));

        List<Tag> saved = tagRepository.saveAll(tags);
        log.info("Seeded {} tags", saved.size());
        return saved;
    }

    private Tag createTag(String name, String description) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        return tag;
    }

    // ═══════════════════════════════════════
    // Step 8: Articles (3 records)
    // ═══════════════════════════════════════

    private void seedArticles(List<Category> categories, List<Tag> tags) {
        User admin = userRepository.findByEmail("luongtd@toctienltd.vn")
                .orElseThrow(() -> new RuntimeException("Seeded user not found"));

        Category congNghe = findCategory(categories, "Công nghệ");
        Category thoiSu = findCategory(categories, "Thời sự");
        Tag javaTag = findTag(tags, "Java");
        Tag springBootTag = findTag(tags, "Spring Boot");
        Tag restApiTag = findTag(tags, "REST API");
        Tag mysqlTag = findTag(tags, "MySQL");
        Tag dockerTag = findTag(tags, "Docker");

        Article article1 = createArticle(
                "Giới thiệu Spring Boot 4",
                "gioi-thieu-spring-boot-4",
                "Spring Boot 4 mang đến nhiều cải tiến vượt bậc so với phiên bản trước.",
                (byte) 0, (byte) 1, admin, congNghe,
                List.of(javaTag, springBootTag));

        Article article2 = createArticle(
                "Xây dựng REST API với Spring Boot",
                "xay-dung-rest-api-voi-spring-boot",
                "Hướng dẫn từng bước xây dựng RESTful API sử dụng Spring Boot và Spring Security.",
                (byte) 0, (byte) 1, admin, congNghe,
                List.of(springBootTag, restApiTag));

        Article article3 = createArticle(
                "Tối ưu cơ sở dữ liệu MySQL cho ứng dụng Java",
                "toi-uu-co-so-du-lieu-mysql-cho-ung-dung-java",
                "Các kỹ thuật indexing, query optimization và connection pooling giúp tăng hiệu suất MySQL.",
                (byte) 0, (byte) 1, admin, congNghe,
                List.of(javaTag, mysqlTag));

        Article article4 = createArticle(
                "Triển khai ứng dụng Spring Boot với Docker",
                "trien-khai-ung-dung-spring-boot-voi-docker",
                "Hướng dẫn đóng gói và triển khai ứng dụng Spring Boot lên môi trường Docker container.",
                (byte) 0, (byte) 1, admin, congNghe,
                List.of(springBootTag, dockerTag));

        Article article5 = createArticle(
                "Tin tức công ty tháng 1/2025",
                "tin-tuc-cong-ty-thang-1-2025",
                "Tổng hợp tin tức nổi bật của công ty trong tháng 1 năm 2025.",
                (byte) 0, (byte) 1, admin, thoiSu,
                List.of());

        articleRepository.saveAll(List.of(article1, article2, article3, article4, article5));
        log.info("Seeded 5 articles");
    }

    private Article createArticle(String title, String slug, String content,
            byte type, byte active, User author, Category category, List<Tag> tags) {
        Article article = new Article();
        article.setTitle(title);
        article.setSlug(slug);
        article.setContent(content);
        article.setType(type);
        article.setActive(active);
        article.setViews(0);
        article.setAuthor(author);
        article.setCategory(category);
        tags.stream()
                .map(tag -> new TagArticle(article, tag))
                .forEach(article.getTagArticles()::add);
        return article;
    }

    private Tag findTag(List<Tag> tags, String name) {
        return tags.stream()
                .filter(t -> t.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tag not found: " + name));
    }

    private Role findRole(List<Role> roles, String name) {
        return roles.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }

    private Company findCompany(List<Company> companies, String name) {
        return companies.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Company not found: " + name));
    }
}