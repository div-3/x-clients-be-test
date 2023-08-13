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


/*
 * Тесты:
 * 1. Позитивные:
 * 1.1 Добавление нового сотрудника к компании +
 * 1.2 Получение списка сотрудников компании +
 * 1.3 Получение сотрудника по id
 * 1.4 Изменение информации о сотруднике (lastName)
 * 1.5 Изменение информации о сотруднике (email)
 * 1.6 Изменение информации о сотруднике (url)
 * 1.7 Изменение информации о сотруднике (phone)
 * 1.8 Изменение информации о сотруднике (isActive)
 * 1.9 Добавление 10 новых сотрудников к компании
 * 1.10 Удаление сотрудников при удалении компании
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
@ExtendWith({CompanyResolver.class, EmployeeResolver.class, CompanyServiceResolver.class, EmployeeServiceResolver.class,
        HiberSessionResolver.class, HiberEmployeeRepositoryResolver.class, HiberCompanyRepositoryResolver.class})
public class EmployeeBusinessTest {
    private final static String PROPERTIES_FILE_PATH = "src/main/resources/API_x_client.properties";
    private static Properties properties = new Properties();
    private static String baseUriString;
    private static String login;
    private static String password;
    private static Faker faker = new Faker(new Locale("ru"));
    private static List<Integer> companyToDelete = new ArrayList<>();
    private static List<Integer> employeeToDelete = new ArrayList<>();


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
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println(company);
        System.out.println(employee);

    }


    @Test
    public void createTest(EmployeeRepository employeeRepository, CompanyRepository companyRepository) throws SQLException {
        System.out.println(employeeRepository.getLast());
        String name = faker.company().name();
        int compId = companyRepository.create(name);
        System.out.println("CompId = " + compId);
        CompanyEntity company = companyRepository.getById(compId);
        EmployeeEntity employeeEntity = new EmployeeEntity
                (1, true, Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()),
                        "Иван", "Д", "В", "89246010000",
                        "div@mail.ru", null, null, company);
        int empId = employeeRepository.create(employeeEntity);
        System.out.println("Создан сотрудник: empId = " + empId + "Данные: " + employeeEntity.toString());
        employeeRepository.deleteById(empId);
        companyRepository.deleteById(compId);
    }


    @Test
    @Tag("Positive")
    @DisplayName("1.2 Получение списка компаний GET")
    public void shouldGetCompanyList(CompanyService apiService, CompanyRepository repository) throws SQLException, IOException {
        List<CompanyEntity> companiesDb = repository.getAll();
        List<Company> companiesApi = apiService.getAll();

        //Сравнение листов компаний
        assertEquals(companiesDb.size(), companiesApi.size());

        //Перекладываем в Map для быстрого поиска
        Map<Integer, CompanyEntity> mapDb = new HashMap<>();
        for (CompanyEntity c : companiesDb) {
            mapDb.put(c.getId(), c);
        }

//        companiesApi.get(0).setActive(false);     //Проверка работы теста
        for (Company c : companiesApi) {
            assertThat(c, isEqual(mapDb.get(c.getId())));
        }
    }
}
