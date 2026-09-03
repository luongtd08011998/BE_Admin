package vn.hoidanit.springrestwithai.feature.appconfig;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.AppConfigFilterRequest;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.AppConfigResponse;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.CreateAppConfigRequest;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.UpdateAppConfigRequest;

public interface AppConfigService {
    AppConfigResponse getLatestConfig(String platform);
    
    ResultPaginationDTO filter(AppConfigFilterRequest filter, Pageable pageable);
    
    AppConfigResponse getById(Long id);
    
    AppConfigResponse create(CreateAppConfigRequest request);
    
    AppConfigResponse update(UpdateAppConfigRequest request);
    
    void delete(Long id);
}
