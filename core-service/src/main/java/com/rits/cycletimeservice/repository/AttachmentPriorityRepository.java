package com.rits.cycletimeservice.repository;

import com.rits.cycletimeservice.model.AttachmentPriorityPostgres;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentPriorityRepository extends JpaRepository<AttachmentPriorityPostgres,Long> {
Boolean existsByTag(String tag);
}
