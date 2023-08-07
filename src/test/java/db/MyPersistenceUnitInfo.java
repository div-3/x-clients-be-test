package db;

import Model.CompanyDBEntity;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class MyPersistenceUnitInfo implements PersistenceUnitInfo {
    private final Properties properties;

    public MyPersistenceUnitInfo(Properties properties) {
        this.properties = properties;
    }

    //Возвращает список обрабатываемых классов
    @Override
    public List<String> getManagedClassNames() {
        return List.of(
                CompanyDBEntity.class.getName()
        );
    }

    //Возвращает настройки
    @Override
    public Properties getProperties() {
        return this.properties;
    }

    //Возвращает название PersistentUnit. Актуально, если их несколько.
    @Override
    public String getPersistenceUnitName() {
        return "testEntityManager";
    }

    //Просто надо так
    @Override
    public String getPersistenceProviderClassName() {
        return HibernatePersistenceProvider.class.getName();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return null;
    }

    @Override
    public DataSource getJtaDataSource() {
        return null;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return null;
    }

    @Override
    public List<String> getMappingFileNames() {
        return null;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return null;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return null;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return false;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return null;
    }

    @Override
    public ValidationMode getValidationMode() {
        return null;
    }


    @Override
    public String getPersistenceXMLSchemaVersion() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void addTransformer(ClassTransformer classTransformer) {

    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return null;
    }
}
