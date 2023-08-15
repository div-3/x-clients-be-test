package test;

import api.AuthService;
import api.EmployeeService;
import db.CompanyRepository;
import db.EmployeeRepository;
import ext.*;
import ext.hibernate.HiberCompanyRepositoryResolver;
import ext.hibernate.HiberEMFResolver;
import ext.hibernate.HiberEmployeeRepositoryResolver;
import io.restassured.common.mapper.TypeRef;
import jakarta.persistence.EntityManagerFactory;
import model.api.Employee;
import model.db.CompanyEntity;
import model.db.EmployeeEntity;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static ext.CommonHelper.getProperties;
import static ext.IsEmployeeEqual.isEqual;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/*
 * Тесты:
 * 1. Позитивные:
 * 1.1 Добавление нового сотрудника к компании +
 * 1.2 Получение списка сотрудников компании +
 * 1.3 Получение сотрудника по id +
 * 1.4 Изменение информации о сотруднике+
 *
 * 2. Негативные:
 * 2.1 Добавление нового сотрудника без авторизации +
 * 2.2 Добавление нового сотрудника к отсутствующей компании +
 * 2.5 Изменение информации о сотруднике без авторизации +
 * 2.6 Изменение информации о сотруднике по несуществующему id +
 * 2.7 Получение списка сотрудников несуществующей компании +
 * 2.8 Получение списка сотрудников компании в которой нет сотрудников +
 * 2.9 Получение сотрудника по несуществующему id +
 * 2.10 Добавление сотрудника без обязательного поля (id)
 * 2.11 Добавление сотрудника без обязательного поля (firstName)
 * 2.12 Добавление сотрудника без обязательного поля (lastName)
 * 2.13 Добавление сотрудника без обязательного поля (companyId)
 * 2.14 Добавление сотрудника без необязательного поля (middleName)
 * 2.15 Добавление сотрудника без необязательного поля (email)
 * 2.16 Добавление сотрудника без необязательного поля (url)
 * 2.17 Добавление сотрудника без необязательного поля (phone)
 * 2.18 Добавление сотрудника без необязательного поля (birthdate)
 * */


//В тестах используется для работы: с БД - Hibernate, с API - RestAssured.
@DisplayName("Employee business tests:")
@ExtendWith({CompanyResolver.class,
        EmployeeResolver.class,
        CompanyServiceResolver.class,
        EmployeeServiceResolver.class,
        HiberEMFResolver.class,
        HiberEmployeeRepositoryResolver.class,
        HiberCompanyRepositoryResolver.class})
public class EmployeeContractTest {
    private final static String PROPERTIES_FILE_PATH = "src/main/resources/API_x_client.properties";
    private final String ADD_RESPONSE_BODY_SCHEMA = "{\"$schema\": \"http://json-schema.org/draft-04/schema#\",\"type\": \"object\",\"properties\": {\"id\": {\"type\": \"integer\"}},\"required\": [\"id\"]}";
    private final String ERROR_RESPONSE_BODY_SCHEMA = "{ \"$schema\": \"http://json-schema.org/draft-04/schema#\", \"type\": \"object\", \"properties\": { \"statusCode\": { \"type\": \"integer\" }, \"message\": { \"type\": \"string\" } }, \"required\": [ \"statusCode\", \"message\" ] }";
    private final String UPDATE_BODY_SCHEMA = "{ \"$schema\": \"http://json-schema.org/draft-04/schema#\", \"type\": \"object\", \"properties\": { \"id\": { \"type\": \"integer\" }, \"isActive\": { \"type\": \"boolean\" }, \"email\": { \"type\": \"string\" }, \"url\": { \"type\": \"string\" } }, \"required\": [ \"id\", \"isActive\", \"email\", \"url\" ] }";
    private final String GET_ALL_BODY_SCHEMA = "{ \"$schema\": \"http://json-schema.org/draft-04/schema#\", \"type\": \"array\", \"items\": [ { \"type\": \"object\", \"properties\": { \"id\": { \"type\": \"integer\" }, \"firstName\": { \"type\": \"string\" }, \"lastName\": { \"type\": \"string\" }, \"middleName\": { \"type\": \"string\" }, \"companyId\": { \"type\": \"integer\" }, \"email\": { \"type\": \"string\" }, \"url\": { \"type\": \"string\" }, \"phone\": { \"type\": \"string\" }, \"birthdate\": { \"type\": \"string\" }, \"isActive\": { \"type\": \"boolean\" } }, \"required\": [ \"id\", \"firstName\", \"lastName\", \"middleName\", \"companyId\", \"email\", \"url\", \"phone\", \"birthdate\", \"isActive\" ] } ] }";
    private final String GET_ONE_BODY_SCHEMA = "{ \"$schema\": \"http://json-schema.org/draft-04/schema#\", \"type\": \"object\", \"properties\": { \"id\": { \"type\": \"integer\" }, \"firstName\": { \"type\": \"string\" }, \"lastName\": { \"type\": \"string\" }, \"middleName\": { \"type\": \"string\" }, \"companyId\": { \"type\": \"integer\" }, \"email\": { \"type\": \"string\" }, \"url\": { \"type\": \"string\" }, \"phone\": { \"type\": \"string\" }, \"birthdate\": { \"type\": \"string\" }, \"isActive\": { \"type\": \"boolean\" } }, \"required\": [ \"id\", \"firstName\", \"lastName\", \"middleName\", \"companyId\", \"email\", \"url\", \"phone\", \"birthdate\", \"isActive\" ] }";
    private final int SHIFT = 100;  //Сдвиг от последнего найденного объекта для тестов с неправильными id
    private static Properties properties = new Properties();
    private static String baseUriString = "";
    private static String basePathString = "";
    private static String login = "";
    private static String password = "";
    private final AuthService authService = AuthService.getInstance();
    private final Faker faker = new Faker(new Locale("ru"));
    private static List<Integer> companyToDelete = new ArrayList<>();
    private static List<Integer> employeeToDelete = new ArrayList<>();


    //Инициализация Hibernate (EntityManagerFactory)
    @BeforeAll
    public static void setUp(EntityManagerFactory emf) {
        properties = getProperties(PROPERTIES_FILE_PATH);
        baseUriString = properties.getProperty("baseURI");
        basePathString = "/employee";
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
        int id = employeeRepository.getLast().getId() + 1;
        Employee employee = employeeApiService.generateEmployee();
        employee.setId(id);
        employee.setCompanyId(companyId);

        String token = authService.logIn(login, password);

        //Добавление Employee через API
        int createdId = given()
                .log().ifValidationFails()
                .baseUri(baseUriString + basePathString)
                .header("x-client-token", token)
                .contentType("application/json; charset=utf-8")
                .body(employee)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(401)
                .contentType("application/json; charset=utf-8")

                //Валидация схемы JSON:
                //https://www.tutorialspoint.com/validate-json-schema-in-rest-assured
                //Генератор схемы на основе JSON https://www.liquid-technologies.com/online-json-to-schema-converter
                //1. Вариант со схемой в файле
//                .body(matchesJsonSchemaInClasspath("employee_update_response.json"))
                //2. Вариант со схемой в строке
                .body(matchesJsonSchema(ADD_RESPONSE_BODY_SCHEMA))
                .extract()
                .path("id");

        employeeToDelete.add(createdId);

        assertEquals(id, createdId);
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.2 Получение списка сотрудников компании")
    public void shouldGetEmployeeByCompanyId(EmployeeService employeeApiService,
                                             EmployeeRepository employeeRepository,
                                             @TestProperties(testNum = 1) CompanyEntity company,
                                             @TestProperties(testNum = 1, itemCount = 3) List<EmployeeEntity> employeesBd) throws SQLException, IOException {

        int companyId = company.getId();

        List<Employee> employeesApi =
                given()
                        .log().ifValidationFails()
                        .baseUri(baseUriString + basePathString)
                        .param("company", companyId)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .log().ifValidationFails()
                        .contentType("application/json; charset=utf-8")
                        .body(matchesJsonSchema(GET_ALL_BODY_SCHEMA))
                        //TODO: Написать BUG-репорт, что формат тела ответа на GET запрос не совпадает
                        // с требованиями в Swagger ("avatar_url" вместо "url")
                        .extract()
                        .body().as(new TypeRef<List<Employee>>() {
                        });

        assertEquals(employeesBd.size(), employeesApi.size());
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.3 Получение сотрудника по id")
    public void shouldGetEmployeeById(EmployeeService employeeApiService,
                                      @TestProperties(testNum = 2) CompanyEntity company,
                                      @TestProperties(testNum = 2) EmployeeEntity employee) throws SQLException, IOException {

        int id = employee.getId();
        Employee employeeApi = given()
                .baseUri(baseUriString + basePathString + "/" + id)
                .log().ifValidationFails()
                .header("accept", "application/json")
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json; charset=utf-8")
                .body(matchesJsonSchema(GET_ONE_BODY_SCHEMA))
                //TODO: Написать BUG-репорт, что формат тела ответа на GET запрос не совпадает
                // с требованиями в Swagger ("avatar_url" вместо "url")
                .extract()
                .body().as(new TypeRef<Employee>() {
                           }
                );

        assertThat(employeeApi, isEqual(employee));
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.4 Изменение информации о сотруднике")
    public void shouldUpdateEmployeeLastName(EmployeeService employeeApiService,
                                             EmployeeRepository employeeRepository,
                                             @TestProperties(testNum = 3) CompanyEntity company,
                                             @TestProperties(testNum = 3) EmployeeEntity employee) throws SQLException, IOException {

        Employee employeeApi = employeeApiService.getById(employee.getId());
        String lastName = faker.name().lastName();
        String email = faker.internet().emailAddress("b" + faker.number().digits(6));
        String url = faker.internet().url();
        String phone = faker.number().digits(10);
        boolean isActive = !employee.isActive();

        String token = authService.logIn(login, password);

        int id = given()
                .log().ifValidationFails()
                .header("x-client-token", token)
                .baseUri(baseUriString + basePathString + "/" + employeeApi.getId())
                .contentType("application/json; charset=utf-8")
                .body("{\"lastName\": \"" + lastName + "\"," +
                        "\"email\": \"" + email + "\"," +
                        "\"url\": \"" + url + "\"," +
                        "\"phone\": \"" + phone + "\"," +
                        "\"isActive\": " + isActive + "}")
                .when()
                .patch()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json; charset=utf-8")
                .assertThat()
                .body(matchesJsonSchema(UPDATE_BODY_SCHEMA))
                .extract().path("id");

        assertEquals(employee.getId(), id);
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
        String message =
                given()
                        .log().ifValidationFails()
                        .baseUri(baseUriString + basePathString)
                        .contentType("application/json; charset=utf-8")
                        .body(employee)
                        .when()
                        .post()
                        .then()
                        .log().ifValidationFails()
                        .statusCode(401)
                        .contentType("application/json; charset=utf-8")
                        .body(matchesJsonSchema(ERROR_RESPONSE_BODY_SCHEMA))
                        .extract()
                        .path("message");

        assertEquals("Unauthorized", message);
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.2 Добавление нового сотрудника к отсутствующей компании")
    public void shouldNotAddEmployeeToAbsentCompany(EmployeeService employeeApiService,
                                                    EmployeeRepository employeeRepository,
                                                    CompanyRepository companyRepository) throws SQLException, IOException {

        //Создание объекта Employee с тестовыми данными
        int companyId = companyRepository.getLast().getId() + SHIFT;  //Установка id несуществующей компании
        int id = employeeRepository.getLast().getId() + 1;
        Employee employee = employeeApiService.generateEmployee();
        employee.setId(id);
        employee.setCompanyId(companyId);

        String token = authService.logIn(login, password);

        //Добавление Employee через API
        String message =
                given()
                        .log().ifValidationFails()
                        .baseUri(baseUriString + basePathString)
                        .header("x-client-token", token)
                        .contentType("application/json; charset=utf-8")
                        .body(employee)
                        .when()
                        .post()
                        .then()
                        .log().ifValidationFails()
                        .statusCode(500)
                        .contentType("application/json; charset=utf-8")
                        .body(matchesJsonSchema(ERROR_RESPONSE_BODY_SCHEMA))
                        .extract()
                        .path("message");

        assertEquals("Internal server error", message);
        //TODO: Написать BUG-репорт, что при ошибке в запросе на создание Employee выдаётся SC 500 вместо SC4XX
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.5 Изменение информации о сотруднике без авторизации")
    public void shouldNotUpdateEmployeeWithoutAuth(EmployeeService employeeApiService,
                                                   @TestProperties(testNum = 3) CompanyEntity company,
                                                   @TestProperties(testNum = 3) EmployeeEntity employee) throws SQLException, IOException {

        Employee employeeApi = employeeApiService.getById(employee.getId());
        String lastName = faker.name().lastName();
        String email = faker.internet().emailAddress("b" + faker.number().digits(6));
        String url = faker.internet().url();
        String phone = faker.number().digits(10);
        boolean isActive = !employee.isActive();

        String message =
                given()
                        .log().ifValidationFails()
                        .baseUri(baseUriString + basePathString + "/" + employeeApi.getId())
                        .contentType("application/json; charset=utf-8")
                        .body("{\"lastName\": \"" + lastName + "\"," +
                                "\"email\": \"" + email + "\"," +
                                "\"url\": \"" + url + "\"," +
                                "\"phone\": \"" + phone + "\"," +
                                "\"isActive\": " + isActive + "}")
                        .when()
                        .patch()
                        .then()
                        .log().ifValidationFails()
                        .statusCode(401)
                        .contentType("application/json; charset=utf-8")
                        .assertThat()
                        .body(matchesJsonSchema(ERROR_RESPONSE_BODY_SCHEMA))
                        .extract()
                        .path("message");

        assertEquals("Unauthorized", message);
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.6 Изменение информации о сотруднике по несуществующему id")
    public void shouldNotUpdateEmployeeWithWrongId(EmployeeService employeeApiService,
                                                   EmployeeRepository employeeRepository,
                                                   CompanyEntity company) throws SQLException, IOException {

        Employee employeeApi = employeeApiService.generateEmployee();
        int lastId = employeeRepository.getLast().getId();
        employeeApi.setId(lastId + SHIFT);
        employeeApi.setCompanyId(company.getId());

        String token = authService.logIn(login, password);

        String message =
                given()
                        .log().ifValidationFails()
                        .header("x-client-token", token)
                        .baseUri(baseUriString + basePathString + "/" + employeeApi.getId())
                        .contentType("application/json; charset=utf-8")
                        .body("{\"lastName\": \"" + employeeApi.getLastName() + "\"," +
                                "\"email\": \"" + employeeApi.getEmail() + "\"," +
                                "\"url\": \"" + employeeApi.getUrl() + "\"," +
                                "\"phone\": \"" + employeeApi.getPhone() + "\"," +
                                "\"isActive\": " + employeeApi.getIsActive() + "}")
                        .when()
                        .patch()
                        .then()
                        .log().ifValidationFails()
                        .statusCode(500)
                        .contentType("application/json; charset=utf-8")
                        .assertThat()
                        .body(matchesJsonSchema(ERROR_RESPONSE_BODY_SCHEMA))
                        .extract()
                        .path("message");

        assertEquals("Internal server error", message);
        //TODO: Написать BUG-репорт, что при ошибке в id в запросе на изменение Employee выдаётся SC 500 вместо SC4XX
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.7 Получение списка сотрудников несуществующей компании")
    public void shouldNotGetEmployeeByWrongCompanyId(CompanyRepository companyRepository) throws SQLException, IOException {

        int companyId = companyRepository.getLast().getId() + SHIFT;

        String message =
                given()
                        .log().ifValidationFails()
                        .baseUri(baseUriString + basePathString)
                        .param("company", companyId)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        //TODO: Запросить у аналитика или PM требования к информация в Swagger по возвращаемым
                        // кодам при ошибках в запросах Employee по companyId
                        .log().ifValidationFails()
                        .contentType("application/json; charset=utf-8")
                        .extract()
                        .body().asString();

        assertEquals("[]", message);
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.8 Получение списка сотрудников компании в которой нет сотрудников")
    public void shouldGetEmptyListEmployeeByEmptyCompany(CompanyEntity company) throws SQLException, IOException {

        int companyId = company.getId();

        String message =
                given()
                        .log().ifValidationFails()
                        .baseUri(baseUriString + basePathString)
                        .param("company", companyId)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .log().ifValidationFails()
                        .contentType("application/json; charset=utf-8")
                        .extract()
                        .body().asString();

        assertEquals("[]", message);
    }

    @Test
    @Tag("Negative")
    @DisplayName("2.9 Получение сотрудника по несуществующему id")
    public void shouldNotGetEmployeeByWrongId(EmployeeRepository employeeRepository) throws SQLException, IOException {

        int id = employeeRepository.getLast().getId() + SHIFT;
        String message =
                given()
                        .baseUri(baseUriString + basePathString + "/" + id)
                        .log().ifValidationFails()
                        .header("accept", "application/json")
                        .when()
                        .get()
                        .then()
                        .log().ifValidationFails()
                        .statusCode(404)
                        //TODO: Написать BUG-репорт, что при запросе несуществующего сотрудника возвращается SC 200 вместо SC404
                        .contentType("application/json; charset=utf-8")
                        .body(matchesJsonSchema(ERROR_RESPONSE_BODY_SCHEMA))
                        .extract()
                        .path("message");

        assertEquals("Not found", message);
        //TODO: Написать BUG-репорт, что при запросе несуществующего сотрудника не возвращается тело ответа с "message":"Not found"
    }

    @ParameterizedTest(name = "Отсутствие полей в запросе на создание")
    @MethodSource("getEmployeeJsonStringWithoutFields")
    @Tag("Negative")
    @DisplayName("2.10 Добавление сотрудника без обязательного поля (id)")
    public void shouldNotAddEmployeeWithoutId(String jsonEmployeeString) throws SQLException, IOException {

        String token = authService.logIn(login, password);

        //Добавление Employee через API
        int createdId = given()
                .log().ifValidationFails()
                .baseUri(baseUriString + basePathString)
                .header("x-client-token", token)
                .contentType("application/json; charset=utf-8")
                .body(jsonEmployeeString)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .contentType("application/json; charset=utf-8")
                .body(matchesJsonSchema(ADD_RESPONSE_BODY_SCHEMA))
                .extract()
                .path("id");
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

    @Test
    public void ts() {
        String[] str = getEmployeeJsonStringWithoutFields();
        for (String s : str) {
            System.out.println(s);
        }
    }

    private static Map<String, String> getFieldsString(Employee employee) {
        Map<String, String> fields = new HashMap<>();

        fields.put("\"id\": ", String.valueOf(employee.getId()).concat(","));
        fields.put("\"firstName\": \"", employee.getFirstName() + "\",");
        fields.put("\"lastName\": \"", employee.getLastName() + "\",");
        fields.put("\"middleName\": \"", employee.getMiddleName() + "\",");
        fields.put("\"companyId\": ", String.valueOf(employee.getCompanyId()).concat(","));
        fields.put("\"email\": \"", employee.getEmail() + "\",");
        fields.put("\"url\": \"", employee.getUrl() + "\",");
        fields.put("\"phone\": \"", employee.getPhone() + "\",");
        fields.put("\"birthdate\": \"", employee.getBirthdate() + "\",", );
        fields.put("\"isActive\": ", String.valueOf(employee.getIsActive()));

        return fields;
    }

    private static String[] getEmployeeJsonStringWithoutRequiredFields(EmployeeService employeeService,
                                                                       EmployeeRepository employeeRepository,
                                                                       CompanyEntity company){
        Employee employee = employeeService.generateEmployee();
        employee.setCompanyId(company.getId());
        employee.setId(employeeRepository.getLast().getId());
        Map<String, String> employeeFields = getFieldsString(employee);
        System.out.println(employeeFields.toString());
        List<String> requiredParameters = List.of("id", "firstName", "lastName", "isActive", "companyId");
    }


    private static String[] getEmployeeJsonStringWithoutFields() {

        List<String> optionParameters = List.of("middleName", "email", "url", "phone", "birthdate");

        List<String> baseJson = List.of("{",
                "  \"id\": 649,",
                "  \"firstName\": \"TSЛюбовь\",",
                "  \"lastName\": \"Крылова\",",
                "  \"middleName\": \"Борисовна\",",
                "  \"companyId\": 377,",
                "  \"email\": \"a22417@mail.ru\",",
                "  \"url\": \"http://www.xn---xn--80aeahfa-v1k6l6a8gxh7b.com/cum\",",
                "  \"phone\": \"9364071439\",",
                "  \"birthdate\": \"1978-06-26\",",
                "  \"isActive\": true",
                "}");

        String[] jsonString = new String[baseJson.size() - 2];

        for (int i = 1; i < baseJson.size() - 1; i++) {

            String tmp = baseJson.get(0);
            for (int j = 1; j < baseJson.size() - 1; j++) {
                if (i != j) tmp = tmp + baseJson.get(j);
            }

            //Убираем лишнюю запятую в случае отсутствия последнего поля
            if (tmp.charAt(tmp.length() - 1) == ',') tmp = tmp.substring(0, tmp.length() - 1);
            tmp = tmp + baseJson.get(baseJson.size() - 1);
            jsonString[i - 1] = tmp;
        }
        return jsonString;
    }

}
