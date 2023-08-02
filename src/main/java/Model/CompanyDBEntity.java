package Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.Objects;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyDBEntity {
    private int id;
    @JsonProperty("isActive")
    private boolean isActive;
    private String createDateTime;
    private String lastChangedDateTime;
    private String name;
    private String description;
    private String deletedAt;

    public CompanyDBEntity() {
    }

    public CompanyDBEntity(int id, boolean isActive, String createDateTime, String lastChangedDateTime, String name, String description, String deletedAt) {
        this.id = id;
        this.isActive = isActive;
        this.createDateTime = createDateTime;
        this.lastChangedDateTime = lastChangedDateTime;
        this.name = name;
        this.description = description;
        this.deletedAt = deletedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getLastChangedDateTime() {
        return lastChangedDateTime;
    }

    public void setLastChangedDateTime(String lastChangedDateTime) {
        this.lastChangedDateTime = lastChangedDateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyDBEntity companyDBEntity = (CompanyDBEntity) o;
        return id == companyDBEntity.id && isActive == companyDBEntity.isActive && Objects.equals(createDateTime, companyDBEntity.createDateTime) && Objects.equals(lastChangedDateTime, companyDBEntity.lastChangedDateTime) && Objects.equals(name, companyDBEntity.name) && Objects.equals(description, companyDBEntity.description) && Objects.equals(deletedAt, companyDBEntity.deletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isActive, createDateTime, lastChangedDateTime, name, description, deletedAt);
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", isActive=" + isActive +
                ", createDateTime='" + createDateTime + '\'' +
                ", lastChangedDateTime='" + lastChangedDateTime + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", deletedAt='" + deletedAt + '\'' +
                '}';
    }
}
