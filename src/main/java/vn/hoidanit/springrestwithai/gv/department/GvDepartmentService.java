package vn.hoidanit.springrestwithai.gv.department;

import java.util.List;
import vn.hoidanit.springrestwithai.gv.department.dto.CreateGvDepartmentRequest;
import vn.hoidanit.springrestwithai.gv.department.dto.GvDepartmentResponse;
import vn.hoidanit.springrestwithai.gv.department.dto.UpdateGvDepartmentRequest;

public interface GvDepartmentService {

    List<GvDepartmentResponse> getAllActive();

    List<GvDepartmentResponse> getAll();

    GvDepartmentResponse getById(Long id);

    GvDepartmentResponse create(CreateGvDepartmentRequest request);

    GvDepartmentResponse update(Long id, UpdateGvDepartmentRequest request);

    void delete(Long id);
}
