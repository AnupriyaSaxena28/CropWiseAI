package com.cropwise.backend.repository;

import com.cropwise.backend.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {
    List<ActivityLog> findByUserIdOrderByDateDesc(String userId);
}
