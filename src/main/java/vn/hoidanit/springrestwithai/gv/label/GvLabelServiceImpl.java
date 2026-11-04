package vn.hoidanit.springrestwithai.gv.label;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.gv.label.dto.CreateGvLabelRequest;
import vn.hoidanit.springrestwithai.gv.label.dto.GvLabelResponse;
import vn.hoidanit.springrestwithai.gv.label.dto.UpdateGvLabelRequest;

@Service
@Transactional
public class GvLabelServiceImpl implements GvLabelService {

    private final GvLabelRepository labelRepository;

    public GvLabelServiceImpl(GvLabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GvLabelResponse> getAllActive() {
        return labelRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GvLabelResponse> getAll() {
        return labelRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GvLabelResponse getById(Long id) {
        GvLabel label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhãn", "id", id));
        return toResponse(label);
    }

    @Override
    public GvLabelResponse create(CreateGvLabelRequest request) {
        if (labelRepository.existsByName(request.name().trim())) {
            throw new DuplicateResourceException("Nhãn", "name", request.name().trim());
        }

        GvLabel label = GvLabel.builder()
                .name(request.name().trim())
                .color(request.color() != null ? request.color().trim() : null)
                .description(request.description())
                .isActive(true)
                .build();

        return toResponse(labelRepository.save(label));
    }

    @Override
    public GvLabelResponse update(Long id, UpdateGvLabelRequest request) {
        GvLabel label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhãn", "id", id));

        if (labelRepository.existsByNameAndIdNot(request.name().trim(), id)) {
            throw new DuplicateResourceException("Nhãn", "name", request.name().trim());
        }

        label.setName(request.name().trim());
        label.setColor(request.color() != null ? request.color().trim() : null);
        label.setDescription(request.description());
        if (request.isActive() != null) {
            label.setIsActive(request.isActive());
        }

        return toResponse(labelRepository.save(label));
    }

    @Override
    public void delete(Long id) {
        GvLabel label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhãn", "id", id));
        label.setIsActive(false);
        labelRepository.save(label);
    }

    private GvLabelResponse toResponse(GvLabel label) {
        return GvLabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .description(label.getDescription())
                .isActive(label.getIsActive())
                .createdAt(label.getCreatedAt())
                .updatedAt(label.getUpdatedAt())
                .build();
    }
}
