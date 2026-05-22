package vn.hoidanit.springrestwithai.qlkh.road;
import vn.hoidanit.springrestwithai.qlkh.road.dto.RoadDropdownResponse;
import java.util.List;
public interface RoadService {
    List<RoadDropdownResponse> getAllRoadsForDropdown();
}
