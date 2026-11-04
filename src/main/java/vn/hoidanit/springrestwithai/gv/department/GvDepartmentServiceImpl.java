package vn.hoidanit.springrestwithai.gv.department;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.gv.department.dto.CreateGvDepartmentRequest;
import vn.hoidanit.springrestwithai.gv.department.dto.GvDepartmentResponse;
import vn.hoidanit.springrestwithai.gv.department.dto.UpdateGvDepartmentRequest;

@Service
@Transactional
public class GvDepartmentServiceImpl implements GvDepartmentService {

    private final GvDepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public GvDepartmentServiceImpl(GvDepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GvDepartmentResponse> getAllActive() {
        return departmentRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GvDepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GvDepartmentResponse getById(Long id) {
        GvDepartment department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", id));
        return toResponse(department);
    }

    @Override
    public GvDepartmentResponse create(CreateGvDepartmentRequest request) {
        if (departmentRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Phòng ban", "code", request.code());
        }

        User manager = null;
        if (request.managerId() != null) {
            manager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", request.managerId()));
        }

        GvDepartment parent = null;
        if (request.parentId() != null) {
            parent = departmentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng ban cha", "id", request.parentId()));
        }

        GvDepartment department = GvDepartment.builder()
                .code(request.code().trim().toUpperCase())
                .name(request.name().trim())
                .description(request.description())
                .manager(manager)
                .parent(parent)
                .isActive(true)
                .build();

        return toResponse(departmentRepository.save(department));
    }

    @Override
    public GvDepartmentResponse update(Long id, UpdateGvDepartmentRequest request) {
        GvDepartment department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", id));

        if (request.managerId() != null) {
            User manager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", request.managerId()));
            department.setManager(manager);
        } else {
            department.setManager(null);
        }

        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new IllegalArgumentException("Phòng ban cha không thể là chính nó");
            }
            GvDepartment parent = departmentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng ban cha", "id", request.parentId()));
            department.setParent(parent);
        } else {
            department.setParent(null);
        }

        department.setName(request.name().trim());
        department.setDescription(request.description());
        if (request.isActive() != null) {
            department.setIsActive(request.isActive());
        }

        return toResponse(departmentRepository.save(department));
    }

    @Override
    public void delete(Long id) {
        GvDepartment department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", id));
        department.setIsActive(false);
        departmentRepository.save(department);
    }

    private GvDepartmentResponse toResponse(GvDepartment dept) {
        return GvDepartmentResponse.builder()
                .id(dept.getId())
                .code(dept.getCode())
                .name(dept.getName())
                .description(dept.getDescription())
                .managerId(dept.getManager() != null ? dept.getManager().getId() : null)
                .managerName(dept.getManager() != null ? dept.getManager().getName() : null)
                .managerEmail(dept.getManager() != null ? dept.getManager().getEmail() : null)
                .parentId(dept.getParent() != null ? dept.getParent().getId() : null)
                .parentName(dept.getParent() != null ? dept.getParent().getName() : null)
                .isActive(dept.getIsActive())
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }
}
