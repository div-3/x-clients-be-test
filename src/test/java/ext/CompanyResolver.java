package ext;

import db.CompanyRepository;
import db.CompanyRepositoryHiber;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.db.CompanyEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.*;

import java.lang.annotation.Annotation;
import java.sql.SQLException;

public class CompanyResolver implements ParameterResolver, AfterAllCallback {
    private CompanyRepository companyRepository;
    private final String EMF_GLOBAL_KEY = "EntityManagerFactory";  //Название ключа EntityManagerFactory в хранилище
    private final String TEST_NUM_COMPANY_GLOBAL_KEY = "COMPANY";  //Название ключа EntityManagerFactory в хранилище
    private final String TEST_COMPANY_NAME = "TEST COMPANY";
    private int companyId = 0;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType().equals(CompanyEntity.class)) return true;
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        //Вытаскиваем сохранённый EntityManager из extensionContext
        EntityManagerFactory entityManagerFactory = (EntityManagerFactory) extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).get(EMF_GLOBAL_KEY);
        EntityManager em = entityManagerFactory.createEntityManager();

        companyRepository = new CompanyRepositoryHiber(em);
        try {
            companyId = companyRepository.create(TEST_COMPANY_NAME);
            int testNum = parameterContext.findAnnotation(TestNum.class).get().testNum();
            extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).put(TEST_NUM_COMPANY_GLOBAL_KEY + testNum, companyId); //Сохраняем номер Company для создания Employee
            return companyRepository.getById(companyId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (companyId != 0) companyRepository.deleteById(companyId);
    }
}
