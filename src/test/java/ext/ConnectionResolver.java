package ext;

import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionResolver implements ParameterResolver, AfterAllCallback {
    private final String propFilePath = "src/main/resources/JDBC_x_client.properties";  //Путь к настройкам подключения к БД
    Connection connection;
    public static String KEY = "connection";    //KEY для глобального хранилища в extensionContext

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(Connection.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Properties properties = getProperties();
        String connectionString = properties.getProperty("connectionString");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");
        try {
            connection = DriverManager.getConnection(connectionString, user, password);
            if (!connection.isClosed()) System.out.println("Соединение открыто");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).put(KEY, connection);
        return connection;
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        connection.close();
        if (connection.isClosed()) System.out.println("Соединение закрыто!");
    }

    //Получить параметры подключения к DB из файла
    private Properties getProperties() {
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
