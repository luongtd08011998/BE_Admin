package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.qlkh.dto.RoadDropdownResponse;
import vn.hoidanit.springrestwithai.qlkh.entity.SupportingTable;

import java.util.List;

@Repository
public interface SupportingTableRepository extends JpaRepository<SupportingTable, Integer> {

    @Query("SELECT new vn.hoidanit.springrestwithai.qlkh.dto.RoadDropdownResponse(s.supportingTableId, s.name, s.type) " +
           "FROM SupportingTable s WHERE s.supportingTableTypeId = 2 AND (s.isActive = 1 OR s.isActive IS NULL) ORDER BY s.name ASC")
    List<RoadDropdownResponse> findActiveRoads();
}
