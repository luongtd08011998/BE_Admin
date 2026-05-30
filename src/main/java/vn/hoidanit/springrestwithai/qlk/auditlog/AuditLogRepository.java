package vn.hoidanit.springrestwithai.qlk.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // ── API 2: lấy log theo user — dùng LEFT JOIN FETCH để load user trong 1 query ──
    @Query(value = """
            SELECT a FROM AuditLog a
            LEFT JOIN FETCH a.user u
            WHERE a.user.id = :userId
            """,
            countQuery = """
            SELECT COUNT(a) FROM AuditLog a
            WHERE a.user.id = :userId
            """)
    Page<AuditLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // ── API 3: lấy log theo entityName ──
    @Query(value = """
            SELECT a FROM AuditLog a
            LEFT JOIN FETCH a.user u
            WHERE a.entityName = :entityName
            """,
            countQuery = """
            SELECT COUNT(a) FROM AuditLog a
            WHERE a.entityName = :entityName
            """)
    Page<AuditLog> findByEntityName(@Param("entityName") String entityName, Pageable pageable);

    // ── API 4: lấy log theo entityName + entityId ──
    @Query(value = """
            SELECT a FROM AuditLog a
            LEFT JOIN FETCH a.user u
            WHERE a.entityName = :entityName
              AND a.entityId = :entityId
            """,
            countQuery = """
            SELECT COUNT(a) FROM AuditLog a
            WHERE a.entityName = :entityName
              AND a.entityId = :entityId
            """)
    Page<AuditLog> findByEntityNameAndEntityId(
            @Param("entityName") String entityName,
            @Param("entityId") Long entityId,
            Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * API 1 — Tìm kiếm tổng hợp với filter tùy chọn.
     * LEFT JOIN FETCH để load user.email ngay trong query, tránh LazyInitializationException.
     * Nếu tham số null thì bỏ qua filter đó.
     */
    @Query(value = """
            SELECT a FROM AuditLog a
            LEFT JOIN FETCH a.user u
            WHERE (:entityName IS NULL OR a.entityName = :entityName)
              AND (:action     IS NULL OR a.action     = :action)
              AND (:userId     IS NULL OR a.user.id    = :userId)
            """,
            countQuery = """
            SELECT COUNT(a) FROM AuditLog a
            WHERE (:entityName IS NULL OR a.entityName = :entityName)
              AND (:action     IS NULL OR a.action     = :action)
              AND (:userId     IS NULL OR a.user.id    = :userId)
            """)
    Page<AuditLog> findWithFilters(
            @Param("entityName") String entityName,
            @Param("action")     String action,
            @Param("userId")     Long userId,
            Pageable pageable);
}
