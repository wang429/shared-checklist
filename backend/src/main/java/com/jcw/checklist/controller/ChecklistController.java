package com.jcw.checklist.controller;

import com.jcw.checklist.dto.ChecklistDTO;
import com.jcw.checklist.dto.ChecklistSummaryDTO;
import com.jcw.checklist.model.Checklist;
import com.jcw.checklist.model.ChecklistItem;
import com.jcw.checklist.model.User;
import com.jcw.checklist.model.UserChecklistProgress;
import com.jcw.checklist.repository.ChecklistItemRepository;
import com.jcw.checklist.repository.ChecklistRepository;
import com.jcw.checklist.repository.UserChecklistProgressRepository;
import com.jcw.checklist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

  @Autowired
  private ChecklistItemRepository checklistItemRepo;
  @Autowired
  private UserRepository userRepo;
  @Autowired
  private UserChecklistProgressRepository progressRepo;
  @Autowired
  private ChecklistRepository checklistRepo;

  @GetMapping
  public List<ChecklistSummaryDTO> getAllChecklists() {
    return checklistRepo.findAllSummaries();
  }

  @GetMapping("/{checklistId}")
  public List<ChecklistDTO> getChecklistWithProgress(@PathVariable Long checklistId) {
    Checklist checklist = checklistRepo.findById(checklistId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));

    List<ChecklistItem> items = checklist.getItems();
    if (items.isEmpty()) {
      return new ArrayList<>();
    }

    // Load only relevant data in single queries
    List<User> users = userRepo.findAll();
    List<UserChecklistProgress> progresses = progressRepo.findByChecklistId(checklistId);

    // Build lookup maps for O(1) access instead of O(n) stream operations  
    Map<UUID, String> userIdToUsername = new HashMap<>();
    for (User user : users) {
      userIdToUsername.put(user.getId(), user.getUsername());
    }

    Map<String, Boolean> progressLookup = new HashMap<>();
    for (UserChecklistProgress progress : progresses) {
      String username = userIdToUsername.get(progress.getUserId());
      if (username != null && progress.isChecked()) {
        String key = progress.getChecklistItemId() + ":" + username;
        progressLookup.put(key, true);
      }
    }

    // Build result efficiently
    List<ChecklistDTO> result = new ArrayList<>(items.size());
    for (ChecklistItem item : items) {
      Map<String, Boolean> progressMap = new HashMap<>();
      for (String username : userIdToUsername.values()) {
        String key = item.getId() + ":" + username;
        progressMap.put(username, progressLookup.getOrDefault(key, false));
      }
      result.add(new ChecklistDTO(item.getId(), item.getContent(), progressMap));
    }

    return result;
  }

  @PostMapping("/{checklistId}/item/{itemId}/user/{userId}/toggle")
  public void toggleItem(@PathVariable Long checklistId,
                         @PathVariable Long itemId,
                         @PathVariable UUID userId) {
    // Optional: verify item belongs to checklistId if desired

    UserChecklistProgress.UserChecklistProgressId id = new UserChecklistProgress.UserChecklistProgressId(userId, itemId);
    UserChecklistProgress progress = progressRepo.findById(id)
        .orElse(new UserChecklistProgress());

    progress.setUserId(userId);
    progress.setChecklistItemId(itemId);
    progress.setChecked(!progress.isChecked());
    progressRepo.save(progress);
  }

  @PostMapping
  public ChecklistSummaryDTO createChecklist(@RequestBody CreateChecklistRequest request) {
    // Validate request
    if (request.getName() == null || request.getName().trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Checklist name is required");
    }
    
    // Create the checklist - let JPA handle ID generation
    Checklist checklist = new Checklist();
    checklist.setName(request.getName().trim());
    checklist = checklistRepo.save(checklist);

    // Create checklist items
    if (request.getItems() != null && !request.getItems().isEmpty()) {
      for (String itemContent : request.getItems()) {
        if (itemContent != null && !itemContent.trim().isEmpty()) {
          ChecklistItem item = new ChecklistItem();
          item.setContent(itemContent.trim());
          item.setChecklist(checklist);
          checklistItemRepo.save(item);
        }
      }
    }

    // Return the created checklist as summary
    return new ChecklistSummaryDTO(checklist.getId(), checklist.getName());
  }

  // DTO for creating checklists
  public static class CreateChecklistRequest {
    private String name;
    private List<String> items;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
  }

}