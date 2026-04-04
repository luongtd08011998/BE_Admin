package vn.hoidanit.springrestwithai.feature.tag;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import vn.hoidanit.springrestwithai.common.TestDataFactory;
import vn.hoidanit.springrestwithai.feature.tag.dto.CreateTagRequest;
import vn.hoidanit.springrestwithai.feature.tag.dto.UpdateTagRequest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        testDataFactory.seedPermissions("TAGS", "/api/v1/tags", "GET", "POST", "PUT", "DELETE");
        testDataFactory.seedPermissions("TAGS", "/api/v1/tags/**", "GET", "DELETE");
    }

    @AfterEach
    void cleanUp() {
        testDataFactory.cleanup();
        tagRepository.deleteAll();
    }

    // ========== POST /api/v1/tags ==========

    @Test
    @DisplayName("POST /tags - 201: creates tag and returns response body")
    void createTag_success_returns201() throws Exception {
        CreateTagRequest request = new CreateTagRequest("Java", "Ngôn ngữ lập trình Java");

        mockMvc.perform(post("/api/v1/tags")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.name", is("Java")))
                .andExpect(jsonPath("$.data.description", is("Ngôn ngữ lập trình Java")))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @DisplayName("POST /tags - 400: blank name returns validation error")
    void createTag_blankName_returns400() throws Exception {
        String emptyJson = "{}";

        mockMvc.perform(post("/api/v1/tags")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("POST /tags - 409: duplicate name returns conflict")
    void createTag_duplicateName_returns409() throws Exception {
        CreateTagRequest request = new CreateTagRequest("Java", "desc");

        mockMvc.perform(post("/api/v1/tags")
                .with(testDataFactory.jwtWithPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/tags")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode", is(409)));
    }

    @Test
    @DisplayName("POST /tags - 401: no token returns unauthorized")
    void createTag_noToken_returns401() throws Exception {
        CreateTagRequest request = new CreateTagRequest("Java", "desc");

        mockMvc.perform(post("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /tags - 403: no permission returns forbidden")
    void createTag_noPermission_returns403() throws Exception {
        CreateTagRequest request = new CreateTagRequest("Java", "desc");

        mockMvc.perform(post("/api/v1/tags")
                        .with(testDataFactory.jwtWithoutPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== GET /api/v1/tags/{id} ==========

    @Test
    @DisplayName("GET /tags/{id} - 200: returns tag")
    void getById_found_returns200() throws Exception {
        Tag saved = tagRepository.save(buildTag("Spring Boot", "Framework Spring Boot"));

        mockMvc.perform(get("/api/v1/tags/{id}", saved.getId())
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is("Spring Boot")));
    }

    @Test
    @DisplayName("GET /tags/{id} - 404: not found returns 404")
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/tags/{id}", 99999L)
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("GET /tags/{id} - 401: no token returns unauthorized")
    void getById_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/tags/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /tags/{id} - 403: no permission returns forbidden")
    void getById_noPermission_returns403() throws Exception {
        Tag saved = tagRepository.save(buildTag("Spring Boot", "desc"));

        mockMvc.perform(get("/api/v1/tags/{id}", saved.getId())
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== GET /api/v1/tags ==========

    @Test
    @DisplayName("GET /tags - 200: returns paginated list")
    void getAll_returns200WithPaginationMetadata() throws Exception {
        tagRepository.save(buildTag("Java", "desc1"));
        tagRepository.save(buildTag("Spring Boot", "desc2"));

        mockMvc.perform(get("/api/v1/tags")
                        .with(testDataFactory.jwtWithPermission())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.meta.total").isNumber())
                .andExpect(jsonPath("$.data.result", notNullValue()));
    }

    @Test
    @DisplayName("GET /tags - 403: no permission returns forbidden")
    void getAll_noPermission_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/tags")
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== PUT /api/v1/tags ==========

    @Test
    @DisplayName("PUT /tags - 200: updates and returns updated tag")
    void updateTag_success_returns200() throws Exception {
        Tag saved = tagRepository.save(buildTag("Old Name", "old desc"));

        UpdateTagRequest request = new UpdateTagRequest(saved.getId(), "New Name", "new desc");

        mockMvc.perform(put("/api/v1/tags")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.name", is("New Name")))
                .andExpect(jsonPath("$.data.description", is("new desc")));
    }

    @Test
    @DisplayName("PUT /tags - 400: null id returns validation error")
    void updateTag_nullId_returns400() throws Exception {
        UpdateTagRequest request = new UpdateTagRequest(null, "Name", "desc");

        mockMvc.perform(put("/api/v1/tags")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("PUT /tags - 404: non-existent id returns 404")
    void updateTag_notFound_returns404() throws Exception {
        UpdateTagRequest request = new UpdateTagRequest(99999L, "Name", "desc");

        mockMvc.perform(put("/api/v1/tags")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("PUT /tags - 403: no permission returns forbidden")
    void updateTag_noPermission_returns403() throws Exception {
        UpdateTagRequest request = new UpdateTagRequest(99999L, "Name", "desc");

        mockMvc.perform(put("/api/v1/tags")
                        .with(testDataFactory.jwtWithoutPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== DELETE /api/v1/tags/{id} ==========

    @Test
    @DisplayName("DELETE /tags/{id} - 200: deletes tag successfully")
    void deleteTag_success_returns200() throws Exception {
        Tag saved = tagRepository.save(buildTag("REST API", "RESTful API design"));

        mockMvc.perform(delete("/api/v1/tags/{id}", saved.getId())
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)));
    }

    @Test
    @DisplayName("DELETE /tags/{id} - 404: not found returns 404")
    void deleteTag_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/tags/{id}", 99999L)
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("DELETE /tags/{id} - 403: no permission returns forbidden")
    void deleteTag_noPermission_returns403() throws Exception {
        Tag saved = tagRepository.save(buildTag("REST API", "desc"));

        mockMvc.perform(delete("/api/v1/tags/{id}", saved.getId())
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== helpers ==========

    private Tag buildTag(String name, String description) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        return tag;
    }
}
