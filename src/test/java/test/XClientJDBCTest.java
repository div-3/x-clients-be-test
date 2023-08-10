package test;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static ext.CommonHelper.getProperties;

public class XClientJDBCTest {
    private final String propFilePath = "src/main/resources/JDBC_x_client.properties";

    @Test
    public void shouldConnectToDB(){
        Properties properties = getProperties(propFilePath);
        try (Connection connection = DriverManager.getConnection(
                properties.getProperty("connectionString"),
                properties.getProperty("user"),
                properties.getProperty("password")))
        {
            ResultSet resultSet = connection.createStatement().executeQuery("select * from company;");
            resultSet.next();
            System.out.println(resultSet.getArray("name").toString());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
