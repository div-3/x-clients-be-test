package test;/*
* При тестировании API-контракта, основанного на RESTful стиле, следует проверить следующие аспекты:
1. Проверка статуса ответа: Проверьте, что статус ответа соответствует ожидаемому, например, 200 (OK), 201 (Created),
* 400 (Bad Request) и т. д. Это позволяет убедиться, что запрос обработан в соответствии с контрактом.
2. Проверка структуры и формата данных: Проверьте, что структура и формат данных, возвращаемых в ответе, соответствуют
* ожидаемому контракту. Это включает проверку наличия и типов полей, формата даты и времени, использование
* JSON или XML и т. д.
3. Проверка правильности данных: Проверьте, что данные, возвращаемые в ответе, являются правильными и полными.
* Проверьте значения полей, сравните их с ожидаемыми значениями. Если API предоставляет возможность фильтрации,
* сортировки или пагинации, также проверьте, что эти функции работают правильно.
4. Проверка работы методов и ресурсов API: Проверьте, что все методы и ресурсы API работают корректно в соответствии
* с описанием контракта. Выполните тесты на создание, чтение, обновление и удаление ресурсов (CRUD-операции).
* Проверьте, что методы возвращают корректные статусы и данные.
5. Проверка обработки ошибок и исключений: Проверьте, что API правильно обрабатывает ошибки и исключения.
* Проверьте, что при передаче некорректных данных API возвращает соответствующие коды ошибок и сообщения.
6. Проверка безопасности: Проверьте, что API в соответствии с контрактом применяет соответствующие механизмы
* безопасности, такие как авторизация и аутентификация, безопасные методы передачи данных (например, HTTPS)
* и другие меры безопасности.
7. Проверка производительности: Если производительность является важным аспектом для вашего API, проверьте,
* что он соответствует ожиданиям по скорости и пропускной способности. Используйте средства для измерения времени
* выполнения запросов и проверки пороговых значений.
В целом, при тестировании API-контракта на основе RESTful стиля необходимо удостовериться, что он соответствует
* установленным стандартам и спецификациям, а также выполняет требования функциональности, безопасности,
* производительности и других необходимых аспектов в соответствии с ожиданиями пользователей и бизнеса.*/

import api.AuthService;
import db.CompanyRepository;
import ext.JDBCCompanyRepositoryResolver;
import ext.JDBCConnectionResolver;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import model.api.Company;
import model.db.CompanyEntity;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static ext.CommonHelper.getProperties;
import static ext.IsCompanyEqual.isEqual;
import static ext.MyMatchers.isCompaniesEqual;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Тесты:
 * Позитивные:
 * 1. Получить список компаний +
 * 2. Добавление новой компании +
 * 3. Получение компании по ID +
 * 4. Изменение компании по ID +
 * 5. Удаление компании по ID +
 * 6. Активировать компанию по ID (доделать)
 * 7. Деактивировать компанию по ID (доделать)*/

//В тестах используется для работы: с БД - JDBC, с API - RestAssured.
//Тесты выполняются в последовательном режиме
@DisplayName("Company contract tests:")
@ExtendWith({JDBCConnectionResolver.class, JDBCCompanyRepositoryResolver.class})
public class CompanyContractTest {
    private final static String PROPERTIES_FILE_PATH = "src/main/resources/API_x_client.properties";
    private final static AuthService AUTH_SERVICE = AuthService.getInstance();
    private static Properties properties = new Properties();
    private static String baseUriString = "";
    private static String basePathString = "";
    private static String login = "";
    private static String password = "";
    private final static String PREFIX = "TS_";
    private String companyName = "";
    private String companyDescription = "";
    private String newName = "";
    private String newDescription = "";

    @BeforeAll
    public static void setUpBeforeAll() {
        properties = getProperties(PROPERTIES_FILE_PATH);
    }

    @BeforeEach
    public void setUp(Connection connection) throws InterruptedException {
        //Библиотека с рандомными данными DataFaker
        Faker faker = new Faker(new Locale("ru"));
        companyName = PREFIX + faker.company().name();
        companyDescription = PREFIX + faker.company().industry();
        newName = PREFIX + faker.company().name();
        newDescription = PREFIX + faker.company().industry();

        baseUriString = properties.getProperty("baseURI");
        basePathString = "/company";

        login = properties.getProperty("login");
        password = properties.getProperty("password");

        Thread.sleep(500);
    }

    @AfterEach
    public void coolDownAfter() throws InterruptedException {
        Thread.sleep(500);
    }

    //Очистка тестовых данных
    @AfterAll
    public static void cleanTD(Connection connection, CompanyRepository companyRepository) throws SQLException {
        companyRepository.clean("");
    }

    @Test
    @Tag("Positive")
    @DisplayName("1. Получить список компаний")
    public void shouldGetCompaniesList() {
        Response response = getGetResponse();
        response
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json; charset=utf-8");

        String s = response.body().asString();
        //Проверка, что в JSON возвращается массив
        assertTrue(s.startsWith("["));
        assertTrue(s.endsWith("]"));
    }

    @Test
    @Tag("Positive")
    @DisplayName("2. Добавление новой компании")
    public void shouldAddNewCompany(CompanyRepository companyRepository) throws SQLException {
        //Авторизация и получение токена
        String token = AUTH_SERVICE.logIn(login, password);

        //Создание новой компании с companyName, companyDescription
        int id = createAndGetNewCompanyId(token, companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);

        //Получение компании из DB
        CompanyEntity companyEntity = companyRepository.getById(id);

        assertEquals(companyName, companyEntity.getName());
        assertEquals(companyDescription, companyEntity.getDescription());
        assertTrue(companyEntity.isActive());
        assertNull(companyEntity.getDeletedAt());
    }

    @Test
    @Tag("Positive")
    @DisplayName("3. Получение компании по ID")
    public void shouldGetCompanyById(CompanyRepository repository) throws SQLException {

        //Создание компании через БД
        int createdId = repository.create(companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(createdId > 0);

        //Получение компании по API по ID
        Company companyAPIResult =
                given()
                        .log().ifValidationFails()
                        .baseUri(baseUriString + basePathString + "/" + createdId)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .log().ifValidationFails()
                        .contentType("application/json; charset=utf-8")
                        .extract()
                        .body()
                        .as(Company.class);

        //Получение компании из DB по ID
        CompanyEntity companyEntityExpected = repository.getById(createdId);

        //Проверка, что по Id мы получили одинаковые компании по API и из БД
        System.out.println("Проверка по моему матчеру");
        assertTrue(isCompaniesEqual(companyAPIResult, companyEntityExpected));
        System.out.println("Проверка по матчеру для Hamcrest");
        assertThat(companyAPIResult, isEqual(companyEntityExpected));
    }

    @Test
    @Tag("Positive")
    @DisplayName("4. Изменение компании по ID")
    public void shouldPatchCompanyById(CompanyRepository repository) throws SQLException {

        //Аутентификация и получение токена
        String token = AUTH_SERVICE.logIn(login, password);

        //Создание новой компании с companyName, companyDescription
        int id = repository.create(companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);

        //Изменение компании по ID
        Company companyEntityAfterPatch =
                given()
                        .baseUri(baseUriString + basePathString + "/" + id)
                        .header("x-client-token", token)
                        .log().ifValidationFails()
                        .contentType("application/json; charset=utf-8")
                        .body("{\"name\": \"" + newName + "\",\"description\": \"" + newDescription + "\"}")
                        .when()
                        .patch()
                        .then()
                        .log().ifValidationFails()
                        .statusCode(200)
                        .contentType("application/json; charset=utf-8")
                        .extract()
                        .body()
                        .as(Company.class);

        //Проверка тела ответа на команду PATCH
        assertEquals(id, companyEntityAfterPatch.getId());
        assertEquals(newName, companyEntityAfterPatch.getName());
        assertEquals(newDescription, companyEntityAfterPatch.getDescription());
        assertTrue(companyEntityAfterPatch.isActive());

        //Проверка, что в БД по ID компания с изменёнными данными
        CompanyEntity companyEntityById = repository.getById(id);

        assertEquals(id, companyEntityById.getId());
        assertEquals(newName, companyEntityById.getName());
        assertEquals(newDescription, companyEntityById.getDescription());
        assertTrue(companyEntityById.isActive());

        //Проверка, что дата изменения не равна дате создания
        assertNotEquals(companyEntityById.getCreateDateTime(), companyEntityById.getChangedTimestamp());
        assertNull(companyEntityById.getDeletedAt());
    }

    @Test
    @Tag("Positive")
    @DisplayName("5. Удаление компании по ID")
    public void shouldDeleteCompanyById(CompanyRepository repository) throws SQLException {
        //Аутентификация и получение токена
        String token = AUTH_SERVICE.logIn(login, password);

        //Создание новой компании с companyName, companyDescription
        int id = repository.create(companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);

        //Проверка, что в БД у компании deletedAt нулевое
        CompanyEntity companyEntityDB = repository.getById(id);
        assertNull(companyEntityDB.getDeletedAt());

        //Удаление компании по ID
        deleteCompanyById(token, id);

        //Задержка для гарантированного прохождения теста
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Проверка, что по ID компания больше недоступна (дополнительно, проверка ответа на запрос GET с невалидным ID)
        given()
                .baseUri(baseUriString + basePathString + "/" + id)
                .log().ifValidationFails()
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                //-----------------------BUG--------------------------------//
                .statusCode(200)            //TODO: Добавить негативный тест на получение GET отсутствующей компании.
                //TODO: 4. Написать BUG-репорт. SC должен быть 404, если компании нет (в негативном тесте)
//                .header("Content-Length", nullValue())               //Проверка, что Content-Length = null
                .header("Content-Length", equalTo("0"))      //Проверка, что "Content-Length" = "0"
                .body(emptyOrNullString());                            //Проверка, что тело ответа пустое

        //Проверка, что в БД у компании deletedAt ненулевое
        companyEntityDB = repository.getById(id);
        assertNotNull(companyEntityDB.getDeletedAt());
    }

    private static int createAndGetNewCompanyId(String token, String companyName, String companyDescription) {
        return given()
                .log().all()
                .baseUri(baseUriString + basePathString)
                .header("x-client-token", token)
                .contentType("application/json; charset=utf-8")
                .body("{\"name\": \"" + companyName + "\",\"description\": \"" + companyDescription + "\"}")
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(201)
                .contentType("application/json; charset=utf-8")
                .extract()
                .path("id");
    }

    private static Response getGetResponse() {
        return given()
                .baseUri(baseUriString + basePathString)
                .when()
                .get()
                .then()
                .extract()
                .response();
    }

    private static List<CompanyEntity> getGetResponse(Response response) {
        return response
                .then()
                .extract()
                .body()
                .as(new TypeRef<List<CompanyEntity>>() {
                });
    }

    private static void deleteCompanyById(String token, int id) {
        //Удаление компании по ID
        given()
                .header("x-client-token", token)
                .log().ifValidationFails()
                .when()
                .baseUri(baseUriString + basePathString + "/delete/" + id)
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }
}
