package com.jcw.checklist.repository;

import com.jcw.checklist.model.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
  List<ChecklistItem> findByChecklistIdOrderByDisplayOrderAsc(Long checklistId);
}
