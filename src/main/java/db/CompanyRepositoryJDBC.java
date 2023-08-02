package db;

import Model.CompanyDBEntity;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CompanyRepositoryJDBC implements CompanyRepository{
    Connection connection;

    public CompanyRepositoryJDBC(Connection connection) throws SQLException {
        this.connection = connection;
    }

    @Override
    public List<CompanyDBEntity> getAll() throws SQLException {
        String getAllQuery = "select * from company;";
        ResultSet resultSet = connection.createStatement().executeQuery(getAllQuery);
        return getCompanyDBEntitiesFromResultSet(resultSet);
    }

    @Override
    public List<CompanyDBEntity> getAll(boolean isActive) throws SQLException {
        String getAllQuery = "select * from company where \"isActive\" = true;";
        ResultSet resultSet = connection.createStatement().executeQuery(getAllQuery);
        return getCompanyDBEntitiesFromResultSet(resultSet);
    }

    @Override
    public CompanyDBEntity getLast() throws SQLException {
        String getAllQuery = "select * from company where \"isActive\" = true order by id desc limit 1;";
        ResultSet resultSet = connection.createStatement().executeQuery(getAllQuery);
        return getCompanyDBEntitiesFromResultSet(resultSet).get(0);
    }

    @Override
    public CompanyDBEntity getById(int id) throws SQLException {
        String getAllQuery = "select * from company where \"id\" = " + id + ";";
        ResultSet resultSet = connection.createStatement().executeQuery(getAllQuery);
        return getCompanyDBEntitiesFromResultSet(resultSet).get(0);
    }

    @Override
    public int create(String name) throws SQLException {
        return 0;
    }

    @Override
    public int create(String name, String description) throws SQLException {
        return 0;
    }

    @Override
    public void deleteById(int id) {

    }

    private static List<CompanyDBEntity> getCompanyDBEntitiesFromResultSet(ResultSet resultSet) throws SQLException {
        List<CompanyDBEntity> companies = new ArrayList<>();
        while (resultSet.next()){
            companies.add( new CompanyDBEntity(
                    resultSet.getInt("id"),
                    resultSet.getBoolean("isActive"),
                    resultSet.getString("createDateTime"),
                    resultSet.getString("lastChangedDateTime"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("deletedAt")));
        }
        return companies;
    }
}
