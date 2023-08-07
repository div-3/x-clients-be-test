package Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/*Для Hibernate надо:
* 1. Подключить в maven
* 2. Создать Data-класс (POJO)
* 3. Желательно имплементить к нему интерфейс Serializable
* 4. Добавить аннотации:
* 4.1. @Entity
* 4.2. @Table(name = "company")   - не обязательно, но можно указать название таблицы в БД
* 4.3. @Column(name = "description", nullable = true, length = 300) - не обязательно, но можно для каждого поля указать ограничения из БД и название столбца
 * 4.4. @Id - для первичного ключа
* 4.5. @GeneratedValue(strategy = GenerationType.IDENTITY) - определяет способ создания ключа при заполнении таблицы
* */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "company")
public class CompanyDBEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @JsonProperty("isActive")
    @Column(name = "isActive", nullable = false)
    private boolean isActive;
    @Column(name = "createDateTime", nullable = false)
    private String createDateTime;
    @Column(name = "lastChangedDateTime", nullable = false)
    private String lastChangedDateTime;
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Column(name = "description", nullable = true, length = 300)
    private String description;
    @Column(name = "deletedAt", nullable = true)
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
