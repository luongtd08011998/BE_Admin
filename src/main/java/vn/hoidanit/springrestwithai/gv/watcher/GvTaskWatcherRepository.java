package vn.hoidanit.springrestwithai.gv.watcher;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvTaskWatcherRepository extends JpaRepository<GvTaskWatcher, Long> {

    List<GvTaskWatcher> findByTaskId(Long taskId);

    List<GvTaskWatcher> findByUserId(Long userId);

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    void deleteByTaskIdAndUserId(Long taskId, Long userId);
}
