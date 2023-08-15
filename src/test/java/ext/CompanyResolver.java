package ext;

import db.CompanyRepository;
import db.CompanyRepositoryHiber;
import db.EmployeeRepositoryHiber;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.db.CompanyEntity;
import net.datafaker.Faker;
import org.junit.jupiter.api.extension.*;

import java.sql.SQLException;
import java.util.Locale;

public class CompanyResolver implements ParameterResolver {
    private CompanyRepository companyRepository;
    private final String EMF_GLOBAL_KEY = "EntityManagerFactory";  //Название ключа EntityManagerFactory в хранилище
    private final String TEST_NUM_COMPANY_GLOBAL_KEY = "COMPANY";  //Название ключа EntityManagerFactory в хранилище
    private final String PREFIX = "TS_";
    private EntityManagerFactory entityManagerFactory;
    private EntityManager em;
    private int companyId = 0;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        if (parameterContext.getParameter().getType().equals(CompanyEntity.class)) return true;
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        //Вытаскиваем сохранённый EntityManager из extensionContext
        entityManagerFactory = (EntityManagerFactory) extensionContext
                .getStore(ExtensionContext.Namespace.GLOBAL).get(EMF_GLOBAL_KEY);
        em = entityManagerFactory.createEntityManager();

        companyRepository = new CompanyRepositoryHiber(em);
        try {
            Faker faker = new Faker(new Locale("RU"));
            companyId = companyRepository
                    .create(PREFIX + faker.company().name(), PREFIX + faker.company().industry());
            if (parameterContext.isAnnotated(TestProperties.class)) {
                int testNum = 0;

                //Если есть аннотация, то достаём из неё данные
                testNum = parameterContext.findAnnotation(TestProperties.class).get().testNum();

                //Сохраняем номер Company для создания Employee
                extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
                        .put(TEST_NUM_COMPANY_GLOBAL_KEY + testNum, companyId);
            }
            return companyRepository.getById(companyId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
