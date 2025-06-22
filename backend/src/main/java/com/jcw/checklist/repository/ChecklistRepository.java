package com.jcw.checklist.repository;

import com.jcw.checklist.model.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
  
  @Query("SELECT new com.jcw.checklist.dto.ChecklistSummaryDTO(c.id, c.name) FROM Checklist c")
  List<com.jcw.checklist.dto.ChecklistSummaryDTO> findAllSummaries();
}

