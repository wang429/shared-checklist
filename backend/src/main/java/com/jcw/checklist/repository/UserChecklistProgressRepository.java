package com.jcw.checklist.repository;

import com.jcw.checklist.model.UserChecklistProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserChecklistProgressRepository extends JpaRepository<UserChecklistProgress, UserChecklistProgress.UserChecklistProgressId> {
  
  @Query("SELECT p FROM UserChecklistProgress p WHERE p.checklistItemId IN :itemIds")
  List<UserChecklistProgress> findByChecklistItemIds(@Param("itemIds") List<Long> itemIds);
  
  @Query("SELECT p FROM UserChecklistProgress p JOIN ChecklistItem ci ON p.checklistItemId = ci.id WHERE ci.checklist.id = :checklistId")
  List<UserChecklistProgress> findByChecklistId(@Param("checklistId") Long checklistId);
}
