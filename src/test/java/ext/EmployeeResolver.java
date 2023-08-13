package ext;

import db.EmployeeRepository;
import db.EmployeeRepositoryHiber;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.db.EmployeeEntity;
import org.junit.jupiter.api.extension.*;

import java.sql.SQLException;

public class EmployeeResolver implements ParameterResolver, AfterAllCallback {
    private EmployeeRepository employeeRepository;
    private final String EMF_GLOBAL_KEY = "EntityManagerFactory";  //Название ключа EntityManagerFactory в хранилище
    private final String TEST_NUM_COMPANY_GLOBAL_KEY = "COMPANY";  //Название ключа EntityManagerFactory в хранилище
    private final String TEST_COMPANY_NAME = "TEST COMPANY";
    private int employeeId = 0;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType().equals(EmployeeEntity.class)) return true;
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        //Вытаскиваем сохранённый EntityManager из extensionContext
        EntityManagerFactory entityManagerFactory = (EntityManagerFactory) extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).get(EMF_GLOBAL_KEY);
        EntityManager em = entityManagerFactory.createEntityManager();

        employeeRepository = new EmployeeRepositoryHiber(em);
        try {
            int companyId = 0;
            if (parameterContext.isAnnotated(TestNum.class)){
                int testNum = parameterContext.findAnnotation(TestNum.class).get().testNum();
                companyId = (int) extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).get(TEST_NUM_COMPANY_GLOBAL_KEY + testNum);
            }
            EmployeeEntity employee= employeeRepository.create(companyId);
            employeeId = employee.getId();
            return employee;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (employeeId != 0) employeeRepository.deleteById(employeeId);
    }
}
