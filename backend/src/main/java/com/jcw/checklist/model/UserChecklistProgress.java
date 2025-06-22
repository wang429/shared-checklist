package com.jcw.checklist.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@IdClass(UserChecklistProgress.UserChecklistProgressId.class)
public class UserChecklistProgress {

  @Id
  private UUID userId;

  @Id
  private Long checklistItemId;

  private boolean checked;

  // Getters and setters
  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public Long getChecklistItemId() {
    return checklistItemId;
  }

  public void setChecklistItemId(Long checklistItemId) {
    this.checklistItemId = checklistItemId;
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
  }

  // Composite key class
  public static class UserChecklistProgressId implements Serializable {
    private UUID userId;
    private Long checklistItemId;

    public UserChecklistProgressId() {
    }

    public UserChecklistProgressId(UUID userId, Long checklistItemId) {
      this.userId = userId;
      this.checklistItemId = checklistItemId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      UserChecklistProgressId that = (UserChecklistProgressId) o;
      return Objects.equals(userId, that.userId) && Objects.equals(checklistItemId, that.checklistItemId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userId, checklistItemId);
    }
  }
}
