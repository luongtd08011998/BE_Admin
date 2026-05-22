package vn.hoidanit.springrestwithai.qlkh.road;

import org.springframework.stereotype.Service;
import vn.hoidanit.springrestwithai.qlkh.road.dto.RoadDropdownResponse;
import vn.hoidanit.springrestwithai.qlkh.road.SupportingTableRepository;

import java.util.List;

@Service
public class RoadServiceImpl implements RoadService {
    
    private final SupportingTableRepository supportingTableRepository;

    public RoadServiceImpl(SupportingTableRepository supportingTableRepository) {
        this.supportingTableRepository = supportingTableRepository;
    }

    public List<RoadDropdownResponse> getAllRoadsForDropdown() {
        return supportingTableRepository.findActiveRoads();
    }
}
