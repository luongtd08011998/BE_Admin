package vn.hoidanit.springrestwithai.qlkh.notification.admin;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.NotificationAdminFilterRequest;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.NotificationAdminResponse;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.NotificationStatisticsResponse;

public interface NotificationAdminService {

    ResultPaginationDTO getNotifications(NotificationAdminFilterRequest filter, Pageable pageable);

    NotificationAdminResponse getNotification(Long id);

    NotificationStatisticsResponse getStatistics();

    NotificationAdminResponse resendNotification(Long id);
}
