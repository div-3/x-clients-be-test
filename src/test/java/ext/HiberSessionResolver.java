package ext;


import db.MyPersistenceUnitInfo;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.jupiter.api.extension.*;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;
import java.util.Properties;

import static ext.commonHelper.getProperties;

public class HiberSessionResolver implements ParameterResolver,  AfterAllCallback {

    private final String propFilePath = "src/main/resources/JDBC_x_client.properties";  //Путь к настройкам подключения к БД

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        //Настройка Hibernate
        Properties propertiesFromCFG = getProperties(propFilePath);     //Чтение параметров из файла конфигурации

        Properties properties = new Properties();
        properties.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        properties.put("hibernate.connection.url", propertiesFromCFG.getProperty("connectionString"));
        properties.put("hibernate.connection.username", propertiesFromCFG.getProperty("user"));
        properties.put("hibernate.connection.password", propertiesFromCFG.getProperty("password"));
        properties.put("hibernate.c3p0.min_size", "5");
        properties.put("hibernate.c3p0.max_size", "20");
        properties.put("hibernate.c3p0.timeout", "1800");
        properties.put("hibernate.c3p0.max_statements", "50");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL82Dialect");


        PersistenceUnitInfo persistenceUnitInfo = new MyPersistenceUnitInfo(properties);
        HibernatePersistenceProvider hibernatePersistenceProvider = new HibernatePersistenceProvider();
        EntityManagerFactory entityManagerFactory = hibernatePersistenceProvider.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
        EntityManager em = entityManagerFactory.createEntityManager();


        return em;
    }


    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {

    }
}
