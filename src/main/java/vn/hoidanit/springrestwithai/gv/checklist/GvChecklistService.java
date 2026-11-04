package vn.hoidanit.springrestwithai.gv.checklist;

import java.util.List;
import vn.hoidanit.springrestwithai.gv.checklist.dto.CreateGvChecklistRequest;
import vn.hoidanit.springrestwithai.gv.checklist.dto.GvChecklistResponse;
import vn.hoidanit.springrestwithai.gv.checklist.dto.UpdateGvChecklistRequest;

public interface GvChecklistService {

    List<GvChecklistResponse> getByTaskId(Long taskId);

    GvChecklistResponse create(Long taskId, CreateGvChecklistRequest request);

    GvChecklistResponse update(Long checklistId, UpdateGvChecklistRequest request);

    GvChecklistResponse toggle(Long checklistId, Long userId);

    void delete(Long checklistId);
}
