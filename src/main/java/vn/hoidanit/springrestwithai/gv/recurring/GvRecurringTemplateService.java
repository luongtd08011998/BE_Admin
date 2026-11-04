package vn.hoidanit.springrestwithai.gv.recurring;

import java.util.List;
import vn.hoidanit.springrestwithai.gv.recurring.dto.CreateGvRecurringRequest;
import vn.hoidanit.springrestwithai.gv.recurring.dto.GvRecurringResponse;
import vn.hoidanit.springrestwithai.gv.recurring.dto.UpdateGvRecurringRequest;

public interface GvRecurringTemplateService {

    List<GvRecurringResponse> getAllActive();

    List<GvRecurringResponse> getAll();

    GvRecurringResponse getById(Long id);

    GvRecurringResponse create(Long creatorId, CreateGvRecurringRequest request);

    GvRecurringResponse update(Long id, UpdateGvRecurringRequest request);

    void toggleActive(Long id);

    void delete(Long id);
}
