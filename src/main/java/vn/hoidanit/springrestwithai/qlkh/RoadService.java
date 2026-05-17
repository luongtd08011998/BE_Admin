package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.stereotype.Service;
import vn.hoidanit.springrestwithai.qlkh.dto.RoadDropdownResponse;

import java.util.List;

@Service
public class RoadService {
    
    private final SupportingTableRepository supportingTableRepository;

    public RoadService(SupportingTableRepository supportingTableRepository) {
        this.supportingTableRepository = supportingTableRepository;
    }

    public List<RoadDropdownResponse> getAllRoadsForDropdown() {
        return supportingTableRepository.findActiveRoads();
    }
}
