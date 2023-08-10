package ext.hibernate;


import db.MyPersistenceUnitInfo;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.Properties;

import static ext.CommonHelper.getProperties;

public class HiberSessionResolver implements ParameterResolver {
    private final String AuthPropFilePath = "src/main/resources/JDBC_x_client.properties";  //Путь к настройкам подключения к БД
    private final String HiberPropFilePath = "src/main/resources/hibernate.properties";  //Путь к настройкам Hibernate
    private final String EMF_GLOBAL_KEY = "EntityManagerFactory";  //Название ключа EntityManagerFactory в хранилище

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType().equals(EntityManagerFactory.class)) return true;
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        //Настройка Hibernate
        EntityManagerFactory entityManagerFactory = (EntityManagerFactory) extensionContext
                .getStore(ExtensionContext.Namespace.GLOBAL).get(EMF_GLOBAL_KEY);

        if (entityManagerFactory == null) {
            //Если Factory отсутствует, то создаём

            //Настройка Hibernate
            Properties hiberProperties = getProperties(HiberPropFilePath);     //Чтение параметров Hibernate из файла конфигурации
            Properties authProperties = getProperties(AuthPropFilePath);     //Чтение параметров из файла конфигурации

            hiberProperties.put("hibernate.connection.url", authProperties.getProperty("connectionString"));
            hiberProperties.put("hibernate.connection.username", authProperties.getProperty("user"));
            hiberProperties.put("hibernate.connection.password", authProperties.getProperty("password"));

            //Создание Factory
            PersistenceUnitInfo persistenceUnitInfo = new MyPersistenceUnitInfo(hiberProperties);
            HibernatePersistenceProvider hibernatePersistenceProvider = new HibernatePersistenceProvider();
            entityManagerFactory = hibernatePersistenceProvider
                    .createContainerEntityManagerFactory(persistenceUnitInfo, hiberProperties);

            //Сохраняем entityManagerFactory в глобальное хранилище
            extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).put(EMF_GLOBAL_KEY, entityManagerFactory);
        }


        return entityManagerFactory;
    }
}
