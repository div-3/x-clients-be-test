package model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
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
@Entity
//@Entity(name = "company")
@Table(name = "company", schema = "public", catalog = "x_clients_db_r06g")
public class CompanyEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @Column(name = "create_timestamp", nullable = false)
    private Timestamp createDateTime;
    @Column(name = "change_timestamp", nullable = false)
    private Timestamp lastChangedDateTime;
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Column(name = "description", nullable = true, length = 300)
    private String description;
    @Column(name = "deleted_at", nullable = true)
    private Timestamp deletedAt;

    //Связь с внешней таблицей
    @JsonIgnore //чтобы не попасть на зацикливание при mapping в Jackson. Hibernate нормально переваривает
    @OneToMany(mappedBy = "company")
    private List<EmployeeEntity> employees;


    public CompanyEntity() {
    }

    public CompanyEntity(int id, boolean isActive, Timestamp createDateTime, Timestamp lastChangedDateTime, String name, String description, Timestamp deletedAt) {
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

    public Timestamp getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(Timestamp createDateTime) {
        this.createDateTime = createDateTime;
    }

    public Timestamp getLastChangedDateTime() {
        return lastChangedDateTime;
    }

    public void setLastChangedDateTime(Timestamp lastChangedDateTime) {
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

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyEntity companyEntity = (CompanyEntity) o;
        return id == companyEntity.id && isActive == companyEntity.isActive && Objects.equals(createDateTime, companyEntity.createDateTime) && Objects.equals(lastChangedDateTime, companyEntity.lastChangedDateTime) && Objects.equals(name, companyEntity.name) && Objects.equals(description, companyEntity.description) && Objects.equals(deletedAt, companyEntity.deletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isActive, createDateTime, lastChangedDateTime, name, description, deletedAt);
    }

    @Override
    public String toString() {
        return "CompanyEntity{" +
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
