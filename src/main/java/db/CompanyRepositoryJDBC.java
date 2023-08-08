package db;

import Model.CompanyDBEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompanyRepositoryJDBC implements CompanyRepository{
    Connection connection;

    public CompanyRepositoryJDBC(Connection connection) throws SQLException {
        this.connection = connection;
    }

    @Override
    public List<CompanyDBEntity> getAll() throws SQLException {
        String getAllQuery = "select * from company;";
        ResultSet resultSet = connection.createStatement().executeQuery(getAllQuery);
//        return getCompanyDBEntitiesFromResultSet(resultSet);
        return null;
    }

    @Override
    public List<CompanyDBEntity> getAll(boolean isActive) throws SQLException {
        String getAllQuery = "select * from company where \"isActive\" = true;";
        ResultSet resultSet = connection.createStatement().executeQuery(getAllQuery);
//        return getCompanyDBEntitiesFromResultSet(resultSet);
        return null;
    }

    @Override
    public CompanyDBEntity getLast() throws SQLException {
        String getAllQuery = "select * from company where \"isActive\" = true order by id desc limit 1;";
        ResultSet resultSet = connection.createStatement().executeQuery(getAllQuery);
//        return getCompanyDBEntitiesFromResultSet(resultSet).get(0);
        return null;
    }

    @Override
    public CompanyDBEntity getById(int id) throws SQLException {
        String getAllQuery = "select * from company where \"id\" = " + id + ";";
        ResultSet resultSet = connection.createStatement().executeQuery(getAllQuery);
//        return getCompanyDBEntitiesFromResultSet(resultSet).get(0);
        return null;
    }

    @Override
    public int create(String name) throws SQLException {
        String insertQuery = "insert into company (\"name\") values (?);";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);    //Включение возврата созданной записи
        preparedStatement.setString(1, name);
        preparedStatement.executeUpdate();
        ResultSet createdId = preparedStatement.getGeneratedKeys();
        createdId.next();
        return createdId.getInt(1);
    }

    @Override
    public int create(String name, String description) throws SQLException {
        String insertQuery = "insert into company (\"name\", \"description\") values (?, ?);";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);    //Включение возврата созданной записи
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, description);
        preparedStatement.executeUpdate();
        ResultSet createdId = preparedStatement.getGeneratedKeys();
        createdId.next();
        return createdId.getInt(1);
    }

    @Override
    public void deleteById(int id) {

    }

//    private static List<CompanyDBEntity> getCompanyDBEntitiesFromResultSet(ResultSet resultSet) throws SQLException {
//        List<CompanyDBEntity> companies = new ArrayList<>();
//        while (resultSet.next()){
//            companies.add( new CompanyDBEntity(
//                    resultSet.getInt("id"),
//                    resultSet.getBoolean("is_active"),
////                    resultSet.getString("createDateTime"),
//                    resultSet.getTimestamp("create_timestamp"),
//                    resultSet.getTimestamp("change_timestamp"),
//                    resultSet.getString("name"),
//                    resultSet.getString("description"),
//                    resultSet.getTimestamp("deleted_at")));
//        }
//        return companies;
//    }
}
