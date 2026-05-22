package vn.hoidanit.springrestwithai.qlkh.road;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.qlkh.road.dto.RoadDropdownResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/roads")
public class RoadAdminController {

    private final RoadService roadService;

    public RoadAdminController(RoadService roadService) {
        this.roadService = roadService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoadDropdownResponse>>> getRoadsForDropdown() {
        List<RoadDropdownResponse> roads = roadService.getAllRoadsForDropdown();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tuyến đường thành công", roads));
    }
}
