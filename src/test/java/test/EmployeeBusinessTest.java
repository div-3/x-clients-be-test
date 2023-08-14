package test;

import api.EmployeeService;
import db.CompanyRepository;
import db.EmployeeRepository;
import ext.*;
import ext.hibernate.HiberCompanyRepositoryResolver;
import ext.hibernate.HiberEmployeeRepositoryResolver;
import ext.hibernate.HiberSessionResolver;
import jakarta.persistence.EntityManagerFactory;
import model.api.Employee;
import model.db.CompanyEntity;
import model.db.EmployeeEntity;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static ext.CommonHelper.getProperties;
import static ext.IsEmployeeEqual.isEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/*
 * Тесты:
 * 1. Позитивные:
 * 1.1 Добавление нового сотрудника к компании +
 * 1.2 Получение списка сотрудников компании +
 * 1.3 Получение сотрудника по id +
 * 1.4 Изменение информации о сотруднике +
 * 1.5 Добавление 5 новых сотрудников к компании +
 *
 * 2. Негативные:
 * 2.1 Добавление нового сотрудника без авторизации (добавить в контрактные) +
 * 2.2 Добавление нового сотрудника к отсутствующей компании (добавить в контрактные) +
 * 2.3 Добавление уже существующего сотрудника (все поля) +
 * 2.4 Добавление сотрудника на уже существующий id +
 * 2.5 Изменение информации о сотруднике без авторизации +
 * 2.6 Изменение информации о сотруднике по несуществующему id +
 * 2.7 Получение списка сотрудников несуществующей компании +
 * 2.8 Получение списка сотрудников компании в которой нет сотрудников +
 * 2.9 Получение сотрудника по несуществующему id +
 * 2.10 Добавление сотрудника без обязательного поля (id) +
 * 2.11 Добавление сотрудника без обязательного поля (firstName) +
 * 2.12 Добавление сотрудника без обязательного поля (lastName) +
 * 2.13 Добавление сотрудника без обязательного поля (companyId) +
 * 2.14 Добавление сотрудника без необязательного поля (middleName) +
 * 2.15 Добавление сотрудника без необязательного поля (email) +
 * 2.16 Добавление сотрудника без необязательного поля (url) +
 * 2.17 Добавление сотрудника без необязательного поля (phone) +
 * 2.18 Добавление сотрудника без необязательного поля (birthdate) +
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
        final int id = employeeRepository.getLast().getId() + 1;
        Employee employee = employeeApiService.generateEmployee();
        employee.setId(id);
        employee.setCompanyId(companyId);

        //Добавление Employee через API
        employeeApiService.logIn(login, password);
        int createdId = employeeApiService.create(employee);
        employeeToDelete.add(createdId);

        EmployeeEntity employeeDb = employeeRepository.getById(createdId);

        //Проверки
        assertAll(
                () -> assertThat(employee, isEqual(employeeDb)),
                //TODO: Написать BUG-репорт, что при создании Employee через API удаляется email

                () -> assertEquals(id, createdId)
                //TODO: Написать BUG-репорт, что при создании Employee через API изменяется заданный id
                // и возвращается автоматически присвоенный
        );
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
        //TODO: Написать BUG-репорт, что при запросе Employee через API поле "url" меняется на "avatar_url"
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.3 Получение сотрудника по id")
    public void shouldGetEmployeeById(EmployeeService employeeApiService,
                                      @TestProperties(testNum = 1) CompanyEntity company,
                                      @TestProperties(testNum = 1) EmployeeEntity employee) throws SQLException, IOException {

        Employee employeeApi = employeeApiService.getById(employee.getId());

        assertThat(employeeApi, isEqual(employee));
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.4 Изменение информации о сотруднике")
    public void shouldUpdateEmployeeLastName(EmployeeService employeeApiService,
                                             EmployeeRepository employeeRepository,
                                             @TestProperties(testNum = 2) CompanyEntity company,
                                             @TestProperties(testNum = 2) EmployeeEntity employee) throws SQLException, IOException {

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
                                   EmployeeRepository employeeRepository,
                                   CompanyEntity company) throws SQLException, IOException {

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

    @Test
    @Tag("Negative")
    @DisplayName("2.1 Добавление нового сотрудника без авторизации")
    public void shouldNotAddEmployeeWithoutAuth(EmployeeService employeeApiService,
                                                EmployeeRepository employeeRepository,
                                                CompanyEntity company) throws SQLException, IOException {

        //Создание объекта Employee с тестовыми данными
        int companyId = company.getId();
        int id = employeeRepository.getLast().getId();
        Employee employee = employeeApiService.generateEmployee();
        employee.setId(++id);
        employee.setCompanyId(companyId);

        //Добавление Employee через API
        List<EmployeeEntity> listBefore = employeeRepository.getAllByCompanyId(companyId);
        employeeApiService.logOut();    //Разлогиниваемся

        //Проверка, что при попытке создания Employee неавторизованным пользователем, появляется ошибка в employeeApiService
        assertThrows(AssertionError.class, () -> {
            employeeApiService.create(employee);
        });

        List<EmployeeEntity> listAfter = employeeRepository.getAllByCompanyId(companyId);

        //Проверка, что количество Employee для Company не увеличилось
        assertTrue(listBefore.containsAll(listAfter));
        assertEquals(listBefore.size(), listAfter.size());
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.2 Добавление нового сотрудника к отсутствующей компании")
    public void shouldNotAddEmployeeToAbsentCompany(EmployeeService employeeApiService,
                                                    EmployeeRepository employeeRepository,
                                                    CompanyRepository companyRepository) throws SQLException, IOException {

        //Создание объекта Employee с тестовыми данными
        int companyId = companyRepository.getLast().getId();
        int id = employeeRepository.getLast().getId();
        Employee employee = employeeApiService.generateEmployee();
        employee.setId(++id);
        employee.setCompanyId(++companyId);

        //Добавление Employee через API
        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        //Проверка, что при попытке создания Employee для отсутствующей компании, появляется ошибка в employeeApiService
        assertThrows(AssertionError.class, () -> {
            employeeApiService.create(employee);
        });
        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        //Проверка, что количество Employee не увеличилось
        assertTrue(listBefore.containsAll(listAfter));
        assertEquals(listBefore.size(), listAfter.size());
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.3 Добавление уже существующего сотрудника (поля)")
    public void shouldNotAddEmployeeDuplicate(EmployeeService employeeApiService,
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

        List<EmployeeEntity> listBefore = employeeRepository.getAll();
        createdId = employeeApiService.create(employee);
        employeeToDelete.add(createdId);
        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertTrue(listBefore.containsAll(listAfter)),
                //TODO: Написать BUG-репорт, что можно создать дубликата Employee
                //TODO: Доработать тест по результатам исправления BUG-репорт по возможности создания дубликатов Employee

                () -> assertEquals(listBefore.size(), listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.4 Добавление сотрудника на уже существующий id")
    public void shouldNotAddEmployeeWithOccupiedId(EmployeeService employeeApiService,
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

        Employee employeeSec = employeeApiService.generateEmployee();
        employeeSec.setId(id);
        employeeSec.setCompanyId(companyId);

        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        //Добавление Employee через API
        employeeApiService.logIn(login, password);
        createdId = employeeApiService.create(employeeSec);
        employeeToDelete.add(createdId);

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertTrue(listBefore.containsAll(listAfter)),
                //TODO: Написать BUG-репорт, что можно добавить нового Employee на уже занятый id (фактически новому
                // Employee выдаётся автоматически новый id) связан с BUG-репортом об игнорировании id при
                // создании нового Employee

                () -> assertEquals(listBefore.size(), listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.5 Изменение информации о сотруднике без авторизации")
    public void shouldNotAddEmployeeWithoutAuth(EmployeeService employeeApiService,
                                                @TestProperties(testNum = 3) CompanyEntity company,
                                                @TestProperties(testNum = 3) EmployeeEntity employee) throws SQLException, IOException {

        Employee employeeApi = employeeApiService.getById(employee.getId());

        employeeApi.setLastName(faker.name().lastName());
        employeeApi.setEmail(faker.internet().emailAddress("b" + faker.number().digits(6)));
        employeeApi.setUrl(faker.internet().url());
        employeeApi.setPhone(faker.number().digits(10));
        employeeApi.setIsActive(!employeeApi.getIsActive());

        employeeApiService.logOut();

        assertThrows(AssertionError.class, () -> employeeApiService.update(employeeApi));

        Employee employeeUpdated = employeeApiService.getById(employee.getId());

        assertThat(employeeUpdated, isEqual(employee));
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.6 Изменение информации о сотруднике по несуществующему id")
    public void shouldNotUpdateEmployeeWithWrongId(EmployeeService employeeApiService,
                                                   EmployeeRepository employeeRepository,
                                                   @TestProperties(testNum = 4) CompanyEntity company,
                                                   @TestProperties(testNum = 4) EmployeeEntity employee) throws SQLException, IOException {

        Employee employeeApi = employeeApiService.getById(employee.getId());
        employeeApi.setLastName(faker.name().lastName());
        employeeApi.setEmail(faker.internet().emailAddress("b" + faker.number().digits(6)));
        employeeApi.setUrl(faker.internet().url());
        employeeApi.setPhone(faker.number().digits(10));
        employeeApi.setIsActive(!employeeApi.getIsActive());

        int lastId = employeeRepository.getLast().getId();
        employeeApi.setId(lastId + 100);

        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        assertThrows(AssertionError.class, () -> employeeApiService.update(employeeApi));

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertTrue(listBefore.containsAll(listAfter)),
                () -> assertEquals(listBefore.size(), listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.7 Получение списка сотрудников несуществующей компании")
    public void shouldGetEmptyListEmployeeByWrongCompanyId(EmployeeService employeeApiService,
                                                           CompanyRepository companyRepository) throws SQLException, IOException {

        int companyId = companyRepository.getLast().getId() + 100;

        //Проверка, что возвращается пустой список Employee
        assertEquals(0, employeeApiService.getAllByCompanyId(companyId).size());
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.8 Получение списка сотрудников компании в которой нет сотрудников")
    public void shouldGetEmptyListEmployeeByEmptyCompany(EmployeeService employeeApiService,
                                                         CompanyEntity company) throws SQLException, IOException {

        int companyId = company.getId();

        //Проверка, что возвращается пустой список Employee
        assertEquals(0, employeeApiService.getAllByCompanyId(companyId).size());
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.9 Получение сотрудника по несуществующему id")
    public void shouldNotGetEmployeeByWrongId(EmployeeService employeeApiService,
                                              EmployeeRepository employeeRepository) throws SQLException, IOException {

        int id = employeeRepository.getLast().getId() + 100;

        //Проверка, что выбрасывается исключение при парсинге пустого тела ответа
        assertThrows(IllegalStateException.class, () -> employeeApiService.getById(id));
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.10 Добавление сотрудника без обязательного поля (id)")
    public void shouldNotAddEmployeeWithoutId(EmployeeService employeeApiService,
                                                   EmployeeRepository employeeRepository,
                                                   CompanyEntity company) throws SQLException, IOException {

        Employee employee = employeeApiService.generateEmployee();
        employee.setCompanyId(company.getId());

        employee.setId(0);

        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        assertThrows(AssertionError.class, () -> employeeApiService.create(employee));
        //TODO: Написать BUG-репорт, что не должны создаваться Employee с id = 0

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertTrue(listBefore.containsAll(listAfter)),
                () -> assertEquals(listBefore.size(), listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.11 Добавление сотрудника без обязательного поля (firstName)")
    public void shouldNotAddEmployeeWithoutFirstName(EmployeeService employeeApiService,
                                              EmployeeRepository employeeRepository,
                                              CompanyEntity company) throws SQLException, IOException {

        Employee employee = employeeApiService.generateEmployee();
        employee.setCompanyId(company.getId());
        employee.setId(employeeRepository.getLast().getId() + 1);

        employee.setFirstName(null);

        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        assertThrows(AssertionError.class, () -> employeeApiService.create(employee));

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertTrue(listBefore.containsAll(listAfter)),
                () -> assertEquals(listBefore.size(), listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.12 Добавление сотрудника без обязательного поля (lastName)")
    public void shouldNotAddEmployeeWithoutLastName(EmployeeService employeeApiService,
                                                     EmployeeRepository employeeRepository,
                                                     CompanyEntity company) throws SQLException, IOException {

        Employee employee = employeeApiService.generateEmployee();
        employee.setCompanyId(company.getId());
        employee.setId(employeeRepository.getLast().getId() + 1);

        employee.setLastName(null);

        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        assertThrows(AssertionError.class, () -> employeeApiService.create(employee));

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertTrue(listBefore.containsAll(listAfter)),
                () -> assertEquals(listBefore.size(), listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.13 Добавление сотрудника без обязательного поля (companyId)")
    public void shouldNotAddEmployeeWithoutCompanyId(EmployeeService employeeApiService,
                                                     EmployeeRepository employeeRepository,
                                                     CompanyEntity company) throws SQLException, IOException {

        Employee employee = employeeApiService.generateEmployee();
        employee.setId(employeeRepository.getLast().getId() + 1);

        employee.setCompanyId(0);

        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        assertThrows(AssertionError.class, () -> employeeApiService.create(employee));

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertTrue(listBefore.containsAll(listAfter)),
                () -> assertEquals(listBefore.size(), listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.14 Добавление сотрудника без необязательного поля (middleName)")
    public void shouldAddEmployeeWithoutMiddleName(EmployeeService employeeApiService,
                                                    EmployeeRepository employeeRepository,
                                                    CompanyEntity company) throws SQLException, IOException {

        Employee employee = employeeApiService.generateEmployee();
        employee.setCompanyId(company.getId());
        employee.setId(employeeRepository.getLast().getId() + 1);

        employee.setMiddleName(null);

        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        int id = employeeApiService.create(employee);
        employeeToDelete.add(id);
        EmployeeEntity employeeDb = employeeRepository.getById(id);

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertThat(employee, isEqual(employeeDb)),
                () -> assertFalse(listBefore.contains(employeeDb)),
                () -> assertTrue(listAfter.contains(employeeDb)),
                () -> assertEquals(listBefore.size() + 1, listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.15 Добавление сотрудника без необязательного поля (email)")
    public void shouldAddEmployeeWithoutEmail(EmployeeService employeeApiService,
                                                   EmployeeRepository employeeRepository,
                                                   CompanyEntity company) throws SQLException, IOException {

        Employee employee = employeeApiService.generateEmployee();
        employee.setCompanyId(company.getId());
        employee.setId(employeeRepository.getLast().getId() + 1);

        employee.setEmail(null);

        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        int id = employeeApiService.create(employee);
        employeeToDelete.add(id);
        EmployeeEntity employeeDb = employeeRepository.getById(id);

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertThat(employee, isEqual(employeeDb)),
                () -> assertFalse(listBefore.contains(employeeDb)),
                () -> assertTrue(listAfter.contains(employeeDb)),
                () -> assertEquals(listBefore.size() + 1, listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.16 Добавление сотрудника без необязательного поля (url)")
    public void shouldAddEmployeeWithoutUrl(EmployeeService employeeApiService,
                                              EmployeeRepository employeeRepository,
                                              CompanyEntity company) throws SQLException, IOException {

        Employee employee = employeeApiService.generateEmployee();
        employee.setCompanyId(company.getId());
        employee.setId(employeeRepository.getLast().getId() + 1);

        employee.setUrl(null);

        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        int id = employeeApiService.create(employee);
        employeeToDelete.add(id);
        EmployeeEntity employeeDb = employeeRepository.getById(id);

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertThat(employee, isEqual(employeeDb)),
                () -> assertFalse(listBefore.contains(employeeDb)),
                () -> assertTrue(listAfter.contains(employeeDb)),
                () -> assertEquals(listBefore.size() + 1, listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.17 Добавление сотрудника без необязательного поля (phone)")
    public void shouldAddEmployeeWithoutPhone(EmployeeService employeeApiService,
                                            EmployeeRepository employeeRepository,
                                            CompanyEntity company) throws SQLException, IOException {

        Employee employee = employeeApiService.generateEmployee();
        employee.setCompanyId(company.getId());
        employee.setId(employeeRepository.getLast().getId() + 1);

        employee.setPhone(null);

        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        int id = employeeApiService.create(employee);
        //TODO: Написать BUG-репорт, что не создаётся Employee без номера телефона (SC 500),
        // в Swagger поле Phone не отмечено как обязательное

        employeeToDelete.add(id);
        EmployeeEntity employeeDb = employeeRepository.getById(id);

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertThat(employee, isEqual(employeeDb)),
                () -> assertFalse(listBefore.contains(employeeDb)),
                () -> assertTrue(listAfter.contains(employeeDb)),
                () -> assertEquals(listBefore.size() + 1, listAfter.size())
        );
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.18 Добавление сотрудника без необязательного поля (birthdate)")
    public void shouldAddEmployeeWithoutBirthdate(EmployeeService employeeApiService,
                                            EmployeeRepository employeeRepository,
                                            CompanyEntity company) throws SQLException, IOException {

        Employee employee = employeeApiService.generateEmployee();
        employee.setCompanyId(company.getId());
        employee.setId(employeeRepository.getLast().getId() + 1);

        employee.setBirthdate(null);

        employeeApiService.logIn(login, password);
        List<EmployeeEntity> listBefore = employeeRepository.getAll();

        int id = employeeApiService.create(employee);
        employeeToDelete.add(id);
        EmployeeEntity employeeDb = employeeRepository.getById(id);

        List<EmployeeEntity> listAfter = employeeRepository.getAll();

        assertAll(
                //Проверка, что количество Employee не увеличилось
                () -> assertThat(employee, isEqual(employeeDb)),
                () -> assertFalse(listBefore.contains(employeeDb)),
                () -> assertTrue(listAfter.contains(employeeDb)),
                () -> assertEquals(listBefore.size() + 1, listAfter.size())
        );
    }

}
