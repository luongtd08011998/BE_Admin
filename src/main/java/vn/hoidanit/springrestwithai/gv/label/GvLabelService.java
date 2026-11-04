package vn.hoidanit.springrestwithai.gv.label;

import java.util.List;
import vn.hoidanit.springrestwithai.gv.label.dto.CreateGvLabelRequest;
import vn.hoidanit.springrestwithai.gv.label.dto.GvLabelResponse;
import vn.hoidanit.springrestwithai.gv.label.dto.UpdateGvLabelRequest;

public interface GvLabelService {

    List<GvLabelResponse> getAllActive();

    List<GvLabelResponse> getAll();

    GvLabelResponse getById(Long id);

    GvLabelResponse create(CreateGvLabelRequest request);

    GvLabelResponse update(Long id, UpdateGvLabelRequest request);

    void delete(Long id);
}
