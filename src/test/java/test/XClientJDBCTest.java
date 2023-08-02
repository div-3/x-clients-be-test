package test;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class XClientJDBCTest {
    private final String propFilePath = "src/main/resources/JDBC_x_client.properties";

    @Test
    public void shouldConnectToDB(){
        Properties properties = getProperties();
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

    //Получить параметры подключения к DB из файла
    private Properties getProperties(){
        File propFile = new File(propFilePath);
        Properties JDBCProperties = new Properties();
        try {
            JDBCProperties.load(new FileReader(propFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return JDBCProperties;
    }
}
