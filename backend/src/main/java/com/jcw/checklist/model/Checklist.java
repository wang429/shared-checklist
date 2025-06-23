package com.jcw.checklist.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "checklists")
public class Checklist {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  // Bi-directional or uni-directional if preferred
  @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL)
  @OrderBy("displayOrder ASC")
  private List<ChecklistItem> items;

  // getters/setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ChecklistItem> getItems() {
    return items;
  }

  public void setItems(List<ChecklistItem> items) {
    this.items = items;
  }
}
