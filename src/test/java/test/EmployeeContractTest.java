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
 * 2.10 Не добавление сотрудника без обязательного поля (набор параметризованных тестов) +
 * 2.11 Добавление сотрудника без необязательного поля (набор параметризованных тестов) +
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

    @BeforeEach
    public void coolDownBefore() throws InterruptedException {
        Thread.sleep(1000);
    }

    @AfterEach
    public void coolDownAfter() throws InterruptedException {
        Thread.sleep(1000);
    }

    //Очистка тестовых данных
    @AfterAll
    public static void cleanTD(CompanyRepository companyRepository,
                               EmployeeRepository employeeRepository) {
        employeeRepository.clean("");
        companyRepository.clean("");

//        for (int i : employeeToDelete) {
//            employeeRepository.deleteById(i);
//        }
//        for (int i : companyToDelete) {
//            companyRepository.deleteById(i);
//        }
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
                .statusCode(201)
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
    @MethodSource("getEmployeeJsonStringWithoutRequiredFields")
    @Tag("Negative")
    @DisplayName("2.10 Не добавление сотрудника без обязательного поля:")
    public void shouldNotAddEmployeeWithoutRequiredField(String jsonEmployeeString) throws SQLException, IOException, InterruptedException {

        String token = authService.logIn(login, password);

        //Добавление Employee через API
        String message =
                given()
                        .log().ifValidationFails()
                        .baseUri(baseUriString + basePathString)
                        .header("x-client-token", token)
                        .contentType("application/json; charset=utf-8")
                        .body(jsonEmployeeString)
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
        //TODO: Написать BUG-репорт, что создаются Employee без полей: "id", "isActive" (в Swagger отмечены как обязательные)
    }

    @ParameterizedTest(name = "Отсутствие полей в запросе на создание")
    @MethodSource("getEmployeeJsonStringWithoutOptionalFields")
    @Tag("Negative")
    @DisplayName("2.11 Добавление сотрудника без необязательного поля:")
    public void shouldAddEmployeeWithoutOptionalFields(String jsonEmployeeString) throws SQLException, IOException, InterruptedException {

        String token = authService.logIn(login, password);

        //Добавление Employee через API
        int createdId =
                given()
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

        employeeToDelete.add(createdId);
        //TODO: Написать BUG-репорт, что не создаются Employee без поля "phone" (в Swagger отмечен как необязательный)
    }

    //Провайдер JSON строк с полями Employee (без обязательных полей)
    private static String[] getEmployeeJsonStringWithoutRequiredFields() {
        //Получаем Map с полями тестового Employee
        Map<String, Object> employeeFields = getFieldsString(null);

        //Задаём поля, для формирования усечённых JSON для Employee
        List<String> requiredParameters =
                List.of("\"id\": ", "\"firstName\": \"", "\"lastName\": \"", "\"isActive\": ", "\"companyId\": ");

        return getStringArrayForSelectedFields(employeeFields, requiredParameters);
    }

    //Провайдер JSON строк с полями Employee (без опциональных полей)
    private static String[] getEmployeeJsonStringWithoutOptionalFields() {
        //Получаем Map с полями тестового Employee
        Map<String, Object> employeeFields = getFieldsString(null);

        //Задаём поля, для формирования усечённых JSON для Employee
        List<String> optionalParameters =
                List.of("\"middleName\": \"", "\"email\": \"", "\"url\": \"", "\"phone\": \"", "\"birthdate\": \"");

        return getStringArrayForSelectedFields(employeeFields, optionalParameters);
    }

    private static Map<String, Object> getFieldsString(Employee employee) {
        if (employee == null) {
            employee = new Employee();
            employee.setId(649);
            employee.setFirstName("TS_Любовь");
            employee.setLastName("Крылова");
            employee.setMiddleName("Борисовна");
            employee.setCompanyId(377);
            employee.setEmail("a22417@mail.ru");
            employee.setUrl("http://www.xn---xn-fa-v1k6l6a8gxh7b.com/cum");
            employee.setPhone("9364071439");
            employee.setBirthdate("1978-06-26");
            employee.setIsActive(true);
        }

        Map<String, Object> fields = new HashMap<>();

        fields.put("\"id\": ", employee.getId());
        fields.put("\"firstName\": \"", employee.getFirstName() + "\"");
        fields.put("\"lastName\": \"", employee.getLastName() + "\"");
        fields.put("\"middleName\": \"", employee.getMiddleName() + "\"");
        fields.put("\"companyId\": ", employee.getCompanyId());
        fields.put("\"email\": \"", employee.getEmail() + "\"");
        fields.put("\"url\": \"", employee.getUrl() + "\"");
        fields.put("\"phone\": \"", employee.getPhone() + "\"");
        fields.put("\"birthdate\": \"", employee.getBirthdate() + "\"");
        fields.put("\"isActive\": ", employee.getIsActive());

        return fields;
    }

    private static String[] getStringArrayForSelectedFields(Map<String, Object> employeeFields, List<String> parameters) {
        String[] jsonString = new String[parameters.size()];
        int i = 0;
        for (String s : parameters) {
            Object o = employeeFields.get(s);
            employeeFields.entrySet().removeIf(entry -> entry.getKey().contains(s));
            String tmp = employeeFields.toString();
            tmp = tmp.replaceAll("=", "");
            jsonString[i++] = tmp;
            employeeFields.put(s, o);
        }
        return jsonString;
    }
}
