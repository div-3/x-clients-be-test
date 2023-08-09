package model.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.util.Objects;

public class EmployeeToCreate {
    private String first_name;
    private String last_name;
    private String middle_name;
    private String phone;
    private String avatar_url;
    private int company_id;

    public EmployeeToCreate(String first_name, String last_name, String middle_name, String phone, String avatar_url, int company_id) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.middle_name = middle_name;
        this.phone = phone;
        this.avatar_url = avatar_url;
        this.company_id = company_id;
    }

    public EmployeeToCreate() {
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
        EmployeeToCreate that = (EmployeeToCreate) o;
        return company_id == that.company_id && Objects.equals(first_name, that.first_name) && Objects.equals(last_name, that.last_name) && Objects.equals(middle_name, that.middle_name) && Objects.equals(phone, that.phone) && Objects.equals(avatar_url, that.avatar_url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first_name, last_name, middle_name, phone, avatar_url, company_id);
    }

    @Override
    public String toString() {
        return "EmployeeToCreate{" +
                "first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", middle_name='" + middle_name + '\'' +
                ", phone='" + phone + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                ", company_id=" + company_id +
                '}';
    }
}
