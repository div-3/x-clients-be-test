package db;

import model.db.EmployeeEntity;

import java.sql.SQLException;
import java.util.List;

public interface EmployeeRepository {

        List<EmployeeEntity> getAllByCompanyId(int companyId) throws SQLException;

        EmployeeEntity getById(int id) throws SQLException;

        int create(EmployeeEntity e) throws SQLException;
        EmployeeEntity create(int companyId) throws SQLException;

        int update(EmployeeEntity e) throws SQLException;

        void deleteById(int id);
        EmployeeEntity getLast();

        List<EmployeeEntity> getAll();

}
