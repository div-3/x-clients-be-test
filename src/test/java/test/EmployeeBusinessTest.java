package test;

import api.CompanyService;
import api.EmployeeService;
import db.CompanyRepository;
import db.EmployeeRepository;
import ext.*;
import ext.hibernate.HiberCompanyRepositoryResolver;
import ext.hibernate.HiberEmployeeRepositoryResolver;
import ext.hibernate.HiberSessionResolver;
import jakarta.persistence.EntityManagerFactory;
import model.api.Company;
import model.api.Employee;
import model.db.CompanyEntity;
import model.db.EmployeeEntity;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static ext.CommonHelper.getProperties;
import static ext.IsCompanyEqual.isEqual;
import static ext.IsEmployeeEqual.isEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*
 * Тесты:
 * 1. Позитивные:
 * 1.1 Добавление нового сотрудника к компании +
 * 1.2 Получение списка сотрудников компании +
 * 1.3 Получение сотрудника по id +
 * 1.4 Изменение информации о сотруднике +
 * 1.5 Добавление 10 новых сотрудников к компании
 * 1.6 Удаление сотрудников при удалении компании
 *
 * 2. Негативные:
 * 2.1 Добавление нового сотрудника без авторизации
 * 2.2 Добавление нового сотрудника к отсутствующей компании
 * 2.3 Добавление уже существующего сотрудника (поля)
 * 2.4 Добавление сотрудника на уже существующий id
 * 2.5 Добавление сотрудника без обязательного поля (id)
 * 2.6 Добавление сотрудника без обязательного поля (firstName)
 * 2.7 Добавление сотрудника без обязательного поля (lastName)
 * 2.8 Добавление сотрудника без обязательного поля (companyId)
 * 2.9 Добавление сотрудника без обязательного поля (isActive)
 * 2.10 Изменение информации о сотруднике без авторизации (lastName)
 * 2.11 Изменение информации о сотруднике без авторизации (email)
 * 2.12 Изменение информации о сотруднике без авторизации (url)
 * 2.13 Изменение информации о сотруднике без авторизации (phone)
 * 2.14 Изменение информации о сотруднике без авторизации (isActive)
 * 2.15 Изменение информации о сотруднике по несуществующему id
 * 2.16 Получение списка сотрудников несуществующей компании
 * 2.17 Получение списка сотрудников компании в которой нет сотрудников
 * 2.18 Получение сотрудника по несуществующему id
 * */


//В тестах используется для работы: с БД - Hibernate, с API - RestAssured.
@DisplayName("Employee business tests:")
@ExtendWith({CompanyResolver.class,
        EmployeeResolver.class,
        CompanyServiceResolver.class,
        EmployeeServiceResolver.class,
        HiberSessionResolver.class,
        HiberEmployeeRepositoryResolver.class,
        HiberCompanyRepositoryResolver.class})
public class EmployeeBusinessTest {
    private final static String PROPERTIES_FILE_PATH = "src/main/resources/API_x_client.properties";
    private static Properties properties = new Properties();
    private static String baseUriString;
    private static String login;
    private static String password;
    private static Faker faker = new Faker(new Locale("ru"));
    private static List<Integer> companyToDelete = new ArrayList<>();
    private static List<Integer> employeeToDelete = new ArrayList<>();

    private static int testNumCount = 0;


    //Инициализация Hibernate (EntityManagerFactory)
    @BeforeAll
    public static void setUp(EntityManagerFactory emf) {
        properties = getProperties(PROPERTIES_FILE_PATH);
        baseUriString = properties.getProperty("baseURI");
        login = properties.getProperty("login");
        password = properties.getProperty("password");
    }

    //Очистка тестовых данных
    @AfterAll
    public static void cleanTD(CompanyRepository companyRepository,
                               EmployeeRepository employeeRepository) {
        for (int i : employeeToDelete) {
            employeeRepository.deleteById(i);
        }
        for (int i : companyToDelete) {
            companyRepository.deleteById(i);
        }
    }

    //----------------------------------------------------------------------------------------------------------
    //1. Позитивные:
    //----------------------------------------------------------------------------------------------------------
    @Test
    @Tag("Positive")
    @DisplayName("1.1 Добавление нового сотрудника к компании")
    public void shouldAddEmployee(EmployeeService employeeApiService,
                                  EmployeeRepository employeeRepository,
                                  CompanyEntity company) throws SQLException, IOException {

        //Создание объекта Employee с тестовыми данными
        int companyId = company.getId();
        int id = employeeRepository.getLast().getId();
        Employee employee = employeeApiService.generateEmployee();
        employee.setId(++id);
        employee.setCompanyId(companyId);

        //Добавление Employee через API
        employeeApiService.logIn(login, password);
        int createdId = employeeApiService.create(employee);
        employeeToDelete.add(createdId);

        EmployeeEntity employeeDb = employeeRepository.getById(createdId);

        //Проверки
        assertEquals(id, createdId);
//        Employee employeeApi = employeeApiService.getById(createdId);

        assertThat(employee, isEqual(employeeDb));
        //TODO: Написать BUG-репорт, что при создании Employee через API удаляется email

//        assertThat(employeeApi, isEqual(employeeDb));
//        TODO: Написать BUG-репорт, что при запросе Employee через API поле "url" меняется на "avatar_url"
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.2 Получение списка сотрудников компании")
    public void shouldGetEmployeeByCompanyId(EmployeeService employeeApiService,
                                             EmployeeRepository employeeRepository,
                                             CompanyEntity company) throws SQLException, IOException {

        int companyId = company.getId();

        List<Employee> listBefore = employeeApiService.getAllByCompanyId(companyId);

        assertEquals(0, listBefore.size());

        //Создание объекта Employee с тестовыми данными для компании
        EmployeeEntity employeeCreated = employeeRepository.create(companyId);
        employeeToDelete.add(employeeCreated.getId());

        List<Employee> listAfter = employeeApiService.getAllByCompanyId(companyId);

        //Проверки:
        assertEquals(1, listAfter.size());
        assertThat(listAfter.get(0), isEqual(employeeCreated));
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.3 Получение сотрудника по id")
    public void shouldGetEmployeeById(EmployeeService employeeApiService,
                                      CompanyRepository companyRepository,
                                      EmployeeRepository employeeRepository,
                                      @TestNum(testNum = 1) CompanyEntity company,
                                      @TestNum(testNum = 1) EmployeeEntity employee) throws SQLException, IOException {

        Employee employeeApi = employeeApiService.getById(employee.getId());

        assertThat(employeeApi, isEqual(employee));
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.4 Изменение информации о сотруднике")
    public void shouldUpdateEmployeeLastName(EmployeeService employeeApiService,
                                      CompanyRepository companyRepository,
                                      EmployeeRepository employeeRepository,
                                      @TestNum(testNum = 2) CompanyEntity company,
                                      @TestNum(testNum = 2) EmployeeEntity employee) throws SQLException, IOException {

        Employee employeeApi = employeeApiService.getById(employee.getId());

        employeeApi.setLastName(faker.name().lastName());
        employeeApi.setEmail(faker.internet().emailAddress("b" + faker.number().digits(6)));
        employeeApi.setUrl(faker.internet().url());
        employeeApi.setPhone(faker.number().digits(10));
        employeeApi.setIsActive(!employeeApi.getIsActive());

        employeeApiService.logIn(login, password);
        int id = employeeApiService.update(employeeApi);
        //TODO: Написать BUG-репорт, что при успешном обновлении информации о сотруднике
        // возвращается SC 201 вместо SC 200, как указано в Swagger

        //TODO: Написать BUG-репорт, что при успешном обновлении информации о сотруднике
        // возвращается тело не с полями, отличающимися от Swagger

        assertThat(employeeApi, isEqual(employeeRepository.getById(id)));
        //TODO: Написать BUG-репорт, что при обновлении employee не обновляется поле phone.
    }


    @Test
    @Tag("Positive")
    @DisplayName("1.5 Добавление 5 новых сотрудников к компании")
    public void shouldAdd5Employee(EmployeeService employeeApiService,
                                             CompanyRepository companyRepository,
                                             EmployeeRepository employeeRepository,
                                             @TestNum(testNum = 3) CompanyEntity company) throws SQLException, IOException {

        //Генерируем и создаём через API Employee для определённой Company
        List<Integer> employeeToCreateId = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Employee empl = employeeApiService.generateEmployee();  //Генерируем Employee без id и companyId
            empl.setCompanyId(company.getId());     //Устанавливаем companyId

            employeeApiService.logIn(login, password);
            int id = employeeApiService.create(empl);
            empl.setId(id);                         //Устанавливаем Id
            employeeToCreateId.add(id);
            employeeToDelete.add(id);
        }

        List<EmployeeEntity> employeeEntityCreated = employeeRepository.getAllByCompanyId(company.getId());
        List<Integer> employeeCreatedId = new ArrayList<>();
        for (EmployeeEntity e : employeeEntityCreated) {
            employeeCreatedId.add(e.getId());
        }

        assertEquals(employeeToCreateId.size(), employeeCreatedId.size());
        assertTrue(employeeToCreateId.containsAll(employeeCreatedId));


    }

}
