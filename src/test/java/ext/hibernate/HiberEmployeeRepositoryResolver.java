package ext.hibernate;

import db.EmployeeRepository;
import db.EmployeeRepositoryHiber;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.extension.*;

public class HiberEmployeeRepositoryResolver implements ParameterResolver {
    private final String EMF_GLOBAL_KEY = "EntityManagerFactory";  //Название ключа EntityManagerFactory в хранилище
    EntityManager em;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType().equals(EmployeeRepository.class)) return true;
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {

        //Вытаскиваем сохранённый EntityManager из extensionContext
        EntityManagerFactory entityManagerFactory = (EntityManagerFactory) extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).get(EMF_GLOBAL_KEY);

        //Для каждого теста создаём свой EntityManager, т.к. он не потокобезопасный
        // (п. 4.3. https://translated.turbopages.org/proxy_u/en-ru.ru.5b18764a-64d1f0a4-194f148e-74722d776562/https/www.baeldung.com/hibernate-entitymanager)
        em = entityManagerFactory.createEntityManager();
        System.out.println("\n--------------------------EM Created\n---------------------------------");
        return new EmployeeRepositoryHiber(em);
    }
}
