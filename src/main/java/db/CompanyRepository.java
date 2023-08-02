package db;

import Model.CompanyDBEntity;

import java.sql.SQLException;
import java.util.List;

public interface CompanyRepository {
    List<CompanyDBEntity> getAll() throws SQLException;

    List<CompanyDBEntity> getAll(boolean isActive) throws SQLException;

    CompanyDBEntity getLast() throws SQLException;
    CompanyDBEntity getById(int id) throws SQLException;

    int create(String name) throws SQLException;

    int create(String name, String description) throws SQLException;

    void deleteById(int id);

}
