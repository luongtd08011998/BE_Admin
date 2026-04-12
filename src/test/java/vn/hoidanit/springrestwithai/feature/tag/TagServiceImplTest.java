package vn.hoidanit.springrestwithai.feature.tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import vn.hoidanit.springrestwithai.feature.tag.dto.CreateTagRequest;
import vn.hoidanit.springrestwithai.feature.tag.dto.TagFilterRequest;
import vn.hoidanit.springrestwithai.feature.tag.dto.TagResponse;
import vn.hoidanit.springrestwithai.feature.tag.dto.UpdateTagRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    // ========== create ==========

    @Test
    @DisplayName("create - success: returns TagResponse and saves once")
    void create_success_returnsTagResponse() {
        CreateTagRequest request = new CreateTagRequest("Java", "Ngôn ngữ lập trình Java");
        Tag saved = buildTag(1L, "Java", "Ngôn ngữ lập trình Java");

        when(tagRepository.existsByName("Java")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(saved);

        TagResponse response = tagService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Java");
        assertThat(response.description()).isEqualTo("Ngôn ngữ lập trình Java");
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    @DisplayName("create - duplicate name: throws DuplicateResourceException")
    void create_duplicateName_throwsDuplicateResourceException() {
        CreateTagRequest request = new CreateTagRequest("Java", "desc");

        when(tagRepository.existsByName("Java")).thenReturn(true);

        assertThatThrownBy(() -> tagService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(tagRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update - success: updates fields and returns TagResponse")
    void update_success_returnsUpdatedTagResponse() {
        Tag existing = buildTag(1L, "Java", "old description");
        UpdateTagRequest request = new UpdateTagRequest(1L, "Java Updated", "new description");
        Tag updated = buildTag(1L, "Java Updated", "new description");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tagRepository.existsByNameAndIdNot("Java Updated", 1L)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(updated);

        TagResponse response = tagService.update(request);

        assertThat(response.name()).isEqualTo("Java Updated");
        assertThat(response.description()).isEqualTo("new description");
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    @DisplayName("update - not found: throws ResourceNotFoundException")
    void update_notFound_throwsResourceNotFoundException() {
        UpdateTagRequest request = new UpdateTagRequest(99L, "Java", "desc");

        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.update(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(tagRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - duplicate name: throws DuplicateResourceException")
    void update_duplicateName_throwsDuplicateResourceException() {
        Tag existing = buildTag(1L, "Java", "desc");
        UpdateTagRequest request = new UpdateTagRequest(1L, "Spring Boot", "desc");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tagRepository.existsByNameAndIdNot("Spring Boot", 1L)).thenReturn(true);

        assertThatThrownBy(() -> tagService.update(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(tagRepository, never()).save(any());
    }

    // ========== getById ==========

    @Test
    @DisplayName("getById - found: returns TagResponse")
    void getById_found_returnsTagResponse() {
        Tag tag = buildTag(1L, "Java", "desc");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        TagResponse response = tagService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Java");
    }

    @Test
    @DisplayName("getById - not found: throws ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== filter ==========

    @Test
    @DisplayName("filter - returns paginated ResultPaginationDTO")
    void filter_returnsPaginatedResult() {
        Tag tag1 = buildTag(1L, "Java", "desc1");
        Tag tag2 = buildTag(2L, "Spring Boot", "desc2");
        Page<Tag> page = new PageImpl<>(List.of(tag1, tag2), PageRequest.of(0, 10), 2);

        when(tagRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        ResultPaginationDTO result = tagService.filter(new TagFilterRequest(null), PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.meta().total()).isEqualTo(2);
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete - success: calls deleteById once")
    void delete_success_callsDeleteById() {
        when(tagRepository.existsById(1L)).thenReturn(true);

        tagService.delete(1L);

        verify(tagRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete - not found: throws ResourceNotFoundException")
    void delete_notFound_throwsResourceNotFoundException() {
        when(tagRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> tagService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(tagRepository, never()).deleteById(any());
    }

    // ========== helpers ==========

    private Tag buildTag(Long id, String name, String description) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        tag.setDescription(description);
        return tag;
    }
}
