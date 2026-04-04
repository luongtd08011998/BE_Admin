package vn.hoidanit.springrestwithai.feature.tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.tag.dto.CreateTagRequest;
import vn.hoidanit.springrestwithai.feature.tag.dto.TagResponse;
import vn.hoidanit.springrestwithai.feature.tag.dto.UpdateTagRequest;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public TagResponse create(CreateTagRequest request) {
        if (tagRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Tag", "name", request.name());
        }

        Tag tag = new Tag();
        tag.setName(request.name());
        tag.setDescription(request.description());

        Tag saved = tagRepository.save(tag);
        return TagResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public TagResponse update(UpdateTagRequest request) {
        Tag tag = tagRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", request.id()));

        if (tagRepository.existsByNameAndIdNot(request.name(), request.id())) {
            throw new DuplicateResourceException("Tag", "name", request.name());
        }

        tag.setName(request.name());
        tag.setDescription(request.description());

        Tag saved = tagRepository.save(tag);
        return TagResponse.fromEntity(saved);
    }

    @Override
    public TagResponse getById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
        return TagResponse.fromEntity(tag);
    }

    @Override
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<TagResponse> pageResult = tagRepository.findAll(pageable)
                .map(TagResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag", "id", id);
        }
        tagRepository.deleteById(id);
    }
}
