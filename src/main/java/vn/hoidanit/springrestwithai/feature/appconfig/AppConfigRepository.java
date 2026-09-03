package vn.hoidanit.springrestwithai.feature.appconfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long>, JpaSpecificationExecutor<AppConfig> {
    
    Optional<AppConfig> findByPlatformIgnoreCaseAndActive(String platform, Integer active);
}
