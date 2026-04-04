package vn.hoidanit.springrestwithai.feature.dashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.hoidanit.springrestwithai.feature.company.CompanyRepository;
import vn.hoidanit.springrestwithai.feature.dashboard.dto.DashboardResponse;
import vn.hoidanit.springrestwithai.feature.permission.PermissionRepository;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("getDashboard - returns correct counts from all repositories")
    void getDashboard_returnsCounts() {
        when(userRepository.count()).thenReturn(10L);
        when(companyRepository.count()).thenReturn(5L);
        when(roleRepository.count()).thenReturn(3L);
        when(permissionRepository.count()).thenReturn(20L);

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.totalUsers()).isEqualTo(10L);
        assertThat(response.totalCompanies()).isEqualTo(5L);
        assertThat(response.totalRoles()).isEqualTo(3L);
        assertThat(response.totalPermissions()).isEqualTo(20L);

        verify(userRepository).count();
        verify(companyRepository).count();
        verify(roleRepository).count();
        verify(permissionRepository).count();
    }

    @Test
    @DisplayName("getDashboard - empty database returns all zeros")
    void getDashboard_emptyDatabase_returnsZeros() {
        when(userRepository.count()).thenReturn(0L);
        when(companyRepository.count()).thenReturn(0L);
        when(roleRepository.count()).thenReturn(0L);
        when(permissionRepository.count()).thenReturn(0L);

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.totalUsers()).isZero();
        assertThat(response.totalCompanies()).isZero();
        assertThat(response.totalRoles()).isZero();
        assertThat(response.totalPermissions()).isZero();
    }
}
