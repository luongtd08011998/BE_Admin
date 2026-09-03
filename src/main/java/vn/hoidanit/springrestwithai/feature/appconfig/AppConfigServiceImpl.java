package vn.hoidanit.springrestwithai.feature.appconfig;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.AppConfigFilterRequest;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.AppConfigResponse;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.CreateAppConfigRequest;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.UpdateAppConfigRequest;

@Service
public class AppConfigServiceImpl implements AppConfigService {

    private final AppConfigRepository appConfigRepository;

    public AppConfigServiceImpl(AppConfigRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }

    @Override
    public AppConfigResponse getLatestConfig(String platform) {
        if (platform == null || (!platform.equalsIgnoreCase("ANDROID") && !platform.equalsIgnoreCase("IOS"))) {
            throw new IllegalArgumentException("Platform không hợp lệ. Chỉ chấp nhận ANDROID hoặc IOS.");
        }
        AppConfig config = appConfigRepository.findByPlatformIgnoreCaseAndActive(platform, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Cấu hình phiên bản", "platform", platform));
        return AppConfigResponse.fromEntity(config);
    }

    @Override
    public ResultPaginationDTO filter(AppConfigFilterRequest filter, Pageable pageable) {
        Specification<AppConfig> spec = AppConfigSpecification.build(filter);
        Page<AppConfigResponse> pageResult = appConfigRepository.findAll(spec, pageable)
                .map(AppConfigResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    public AppConfigResponse getById(Long id) {
        AppConfig config = appConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cấu hình phiên bản", "id", id));
        return AppConfigResponse.fromEntity(config);
    }

    @Override
    @Transactional
    public AppConfigResponse create(CreateAppConfigRequest request) {
        AppConfig config = new AppConfig();
        config.setPlatform(request.platform().toUpperCase());
        config.setLatestVersion(request.latestVersion());
        config.setLatestVersionCode(request.latestVersionCode());
        config.setMinRequiredVersion(request.minRequiredVersion());
        config.setMinRequiredVersionCode(request.minRequiredVersionCode());
        config.setStoreUrl(request.storeUrl());
        config.setReleaseNotes(request.releaseNotes());
        config.setActive(request.active() != null ? request.active().intValue() : 1);

        return AppConfigResponse.fromEntity(appConfigRepository.save(config));
    }

    @Override
    @Transactional
    public AppConfigResponse update(UpdateAppConfigRequest request) {
        AppConfig config = appConfigRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Cấu hình phiên bản", "id", request.id()));

        config.setPlatform(request.platform().toUpperCase());
        config.setLatestVersion(request.latestVersion());
        config.setLatestVersionCode(request.latestVersionCode());
        config.setMinRequiredVersion(request.minRequiredVersion());
        config.setMinRequiredVersionCode(request.minRequiredVersionCode());
        config.setStoreUrl(request.storeUrl());
        config.setReleaseNotes(request.releaseNotes());
        if (request.active() != null) {
            config.setActive(request.active() != null ? request.active().intValue() : null);
        }

        return AppConfigResponse.fromEntity(appConfigRepository.save(config));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!appConfigRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cấu hình phiên bản", "id", id);
        }
        appConfigRepository.deleteById(id);
    }
}
