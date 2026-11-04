package vn.hoidanit.springrestwithai.gv.department.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GvDepartmentResponse(
    Long id,
    String code,
    String name,
    String description,
    Long managerId,
    String managerName,
    String managerEmail,
    Long parentId,
    String parentName,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt
) {}
