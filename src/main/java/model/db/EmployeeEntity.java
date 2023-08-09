package model.db;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "employee")
public class EmployeeEntity {
    @Id
    private int id;
    private boolean is_active;
    private Timestamp create_timestamp;
    private Timestamp change_timestamp;
    private String first_name;
    private String last_name;
    private String middle_name;
    private String phone;
    private String email;
    private String avatar_url;
    private int company_id;

    public EmployeeEntity(int id, boolean is_active, Timestamp create_timestamp, Timestamp change_timestamp, String first_name, String last_name, String middle_name, String phone, String email, String avatar_url, int company_id) {
        this.id = id;
        this.is_active = is_active;
        this.create_timestamp = create_timestamp;
        this.change_timestamp = change_timestamp;
        this.first_name = first_name;
        this.last_name = last_name;
        this.middle_name = middle_name;
        this.phone = phone;
        this.email = email;
        this.avatar_url = avatar_url;
        this.company_id = company_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public Timestamp getCreate_timestamp() {
        return create_timestamp;
    }

    public void setCreate_timestamp(Timestamp create_timestamp) {
        this.create_timestamp = create_timestamp;
    }

    public Timestamp getChange_timestamp() {
        return change_timestamp;
    }

    public void setChange_timestamp(Timestamp change_timestamp) {
        this.change_timestamp = change_timestamp;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public int getCompany_id() {
        return company_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeEntity that = (EmployeeEntity) o;
        return id == that.id && is_active == that.is_active && company_id == that.company_id && Objects.equals(create_timestamp, that.create_timestamp) && Objects.equals(change_timestamp, that.change_timestamp) && Objects.equals(first_name, that.first_name) && Objects.equals(last_name, that.last_name) && Objects.equals(middle_name, that.middle_name) && Objects.equals(phone, that.phone) && Objects.equals(email, that.email) && Objects.equals(avatar_url, that.avatar_url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, is_active, create_timestamp, change_timestamp, first_name, last_name, middle_name, phone, email, avatar_url, company_id);
    }

    @Override
    public String toString() {
        return "EmployeeEntity{" +
                "id=" + id +
                ", is_active=" + is_active +
                ", create_timestamp=" + create_timestamp +
                ", change_timestamp=" + change_timestamp +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", middle_name='" + middle_name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                ", company_id=" + company_id +
                '}';
    }
}
