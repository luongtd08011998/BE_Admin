package vn.hoidanit.springrestwithai.feature.permission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.domain.Specification;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.hoidanit.springrestwithai.feature.permission.dto.PermissionFilterRequest;
import vn.hoidanit.springrestwithai.feature.permission.dto.PermissionResponse;
import vn.hoidanit.springrestwithai.feature.permission.dto.UpdatePermissionRequest;
import vn.hoidanit.springrestwithai.security.PermissionAuthorizationManager;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionAuthorizationManager permissionAuthorizationManager;

    public PermissionServiceImpl(PermissionRepository permissionRepository,
            PermissionAuthorizationManager permissionAuthorizationManager) {
        this.permissionRepository = permissionRepository;
        this.permissionAuthorizationManager = permissionAuthorizationManager;
    }

    @Override
    @Transactional
    public PermissionResponse create(CreatePermissionRequest request) {
        if (permissionRepository.existsByApiPathAndMethod(request.apiPath(), request.method())) {
            throw new DuplicateResourceException("Quyền hạn", "apiPath + method",
                    request.apiPath() + " [" + request.method() + "]");
        }

        Permission permission = new Permission();
        permission.setName(request.name());
        permission.setApiPath(request.apiPath());
        permission.setMethod(request.method());
        permission.setModule(request.module());

        Permission saved = permissionRepository.save(permission);
        permissionAuthorizationManager.loadCache();
        return PermissionResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public PermissionResponse update(UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Quyền hạn", "id", request.id()));

        if (permissionRepository.existsByApiPathAndMethodAndIdNot(
                request.apiPath(), request.method(), request.id())) {
            throw new DuplicateResourceException("Quyền hạn", "apiPath + method",
                    request.apiPath() + " [" + request.method() + "]");
        }

        permission.setName(request.name());
        permission.setApiPath(request.apiPath());
        permission.setMethod(request.method());
        permission.setModule(request.module());

        Permission saved = permissionRepository.save(permission);
        permissionAuthorizationManager.loadCache();
        return PermissionResponse.fromEntity(saved);
    }

    @Override
    public PermissionResponse getById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quyền hạn", "id", id));
        return PermissionResponse.fromEntity(permission);
    }

    @Override
    public ResultPaginationDTO filter(PermissionFilterRequest filter, Pageable pageable) {
        Specification<Permission> spec = PermissionSpecification.build(filter);
        Page<PermissionResponse> pageResult = permissionRepository.findAll(spec, pageable)
                .map(PermissionResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Quyền hạn", "id", id);
        }
        permissionRepository.deleteById(id);
        permissionAuthorizationManager.loadCache();
    }
}
