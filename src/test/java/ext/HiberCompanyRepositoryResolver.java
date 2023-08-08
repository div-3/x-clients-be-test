package ext;

import db.CompanyRepository;
import db.CompanyRepositoryHiber;
import db.MyPersistenceUnitInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.Properties;

import static ext.commonHelper.getProperties;

public class HiberCompanyRepositoryResolver implements ParameterResolver {
    private final String propFilePath = "src/main/resources/JDBC_x_client.properties";  //Путь к настройкам подключения к БД
    private final String EM = "EntityManager";
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType().equals(CompanyRepositoryHiber.class)) return true;
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {

        //Проверка, что нет сохранённого EntityManager в extensionContext
        EntityManager em = (EntityManager) extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).get(EM);

        if (em == null) {
            //Если em отсутствует, то создаём

            //Настройка Hibernate
            Properties propertiesFromCFG = getProperties(propFilePath);     //Чтение параметров из файла конфигурации

            Properties properties = new Properties();
//            properties.put("hibernate.connection.driver_class", "org.postgresql.Driver");
//            properties.put("hibernate.connection.url", propertiesFromCFG.getProperty("connectionString"));
//            properties.put("hibernate.connection.username", propertiesFromCFG.getProperty("user"));
//            properties.put("hibernate.connection.password", propertiesFromCFG.getProperty("password"));
//            properties.put("hibernate.c3p0.min_size", "5");
//            properties.put("hibernate.c3p0.max_size", "20");
//            properties.put("hibernate.c3p0.timeout", "1800");
//            properties.put("hibernate.c3p0.max_statements", "50");
//            properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL82Dialect");

            properties.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            properties.put("hibernate.connection.url", "jdbc:postgresql://dpg-cj94hf0eba7s73bdki80-a.oregon-postgres.render.com/x_clients_db_r06g");
            properties.put("hibernate.connection.username", "x_clients_db_r06g_user");
            properties.put("hibernate.connection.password", "0R1RNWXMepS7mrvcKRThRi82GtJ2Ob58");
            properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            properties.put("hibernate.show_sql", "true");
            properties.put("hibernate.format_sql", "true");
            properties.put("hibernate.connection.autocommit", "true");
            properties.put("hibernate.hbm2ddl.auto", "validate");

            //Создание EM
            PersistenceUnitInfo persistenceUnitInfo = new MyPersistenceUnitInfo(properties);
            HibernatePersistenceProvider hibernatePersistenceProvider = new HibernatePersistenceProvider();
            EntityManagerFactory entityManagerFactory = hibernatePersistenceProvider.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
//            EntityManagerFactory entityManagerFactory = hibernatePersistenceProvider.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
            em = entityManagerFactory.createEntityManager();
            extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).put(EM, em);
        }
        return new CompanyRepositoryHiber(em);
    }
}
