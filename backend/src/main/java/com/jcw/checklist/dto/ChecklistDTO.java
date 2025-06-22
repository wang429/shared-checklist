package com.jcw.checklist.dto;

import java.util.Map;

public class ChecklistDTO {
  public Long id;
  public String content;
  public Map<String, Boolean> progress;

  public ChecklistDTO(Long id, String content, Map<String, Boolean> progress) {
    this.id = id;
    this.content = content;
    this.progress = progress;
  }
}