package Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Company {
    private int id;
    private boolean isActive;
    private String createDateTime;
    private String lastChangedDateTime;
    private String name;
    private String description;
    private String deletedAt;

    public Company() {
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
        Company company = (Company) o;
        return id == company.id && isActive == company.isActive && Objects.equals(createDateTime, company.createDateTime) && Objects.equals(lastChangedDateTime, company.lastChangedDateTime) && Objects.equals(name, company.name) && Objects.equals(description, company.description) && Objects.equals(deletedAt, company.deletedAt);
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
