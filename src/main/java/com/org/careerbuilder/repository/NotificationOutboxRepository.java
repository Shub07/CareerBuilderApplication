package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.NotificationOutbox;
import com.org.careerbuilder.models.enums.NotificationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findByStatusOrderByCreatedAtAsc(NotificationStatus status, Pageable pageable);
}
