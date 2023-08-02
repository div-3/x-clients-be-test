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

import Model.Company;
import Model.CompanyDBEntity;
import db.CompanyRepository;
import ext.CompanyDBRepositoryResolver;
import ext.ConnectionResolver;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


import static ext.MyMatchers.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Тесты:
 * Позитивные:
 * 1. Получить список компаний +
 * 2. Авторизация +
 * 3. Добавление новой компании +
 * 4. Получение компании по ID +
 * 5. Изменение компании по ID +
 * 6. Удаление компании по ID +
 * 7. Активировать компанию по ID
 * 8. Деактивировать компанию по ID*/
@DisplayName("X-Clients-Be contract tests:")
@ExtendWith({ConnectionResolver.class, CompanyDBRepositoryResolver.class})
public class CompanyContractTest {
    private final static String propertiesFilePath = "src/main/resources/API_x_client.properties";
    private static Properties properties = new Properties();
    private static String login = "";
    private static String password = "";
    private static String companyName = "ООО Рога и копыта";
    private static String companyDescription = "Все понемногу";
    private static String newName = "ООО РиК2";
    private static String newDescription = "То же самое";

    @BeforeAll
    public static void setUpBeforeAll(Connection connection) {
        properties = getAPIProperties();
    }

    @BeforeEach
    public void setUp() {
        baseURI = properties.getProperty("baseURI");
        basePath = "/company";
        login = properties.getProperty("login");
        password = properties.getProperty("password");
    }

    @AfterAll
    public static void clearUp() {
        List<CompanyDBEntity> listToDelete = getGetResponse(getGetResponse());
        String token = getAuthToken(login, password);
        for (CompanyDBEntity c : listToDelete) {
            if (c.getName().equals(companyName) || c.getName().equals(newName))
                deleteCompanyById(token, c.getId());
        }
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
    @DisplayName("2. Авторизация")
    public void shouldAuth() {
        String token = getAuthToken(login, password);

        //Проверка, что токен с нужной длиной
        assertEquals(148, token.length());
    }

    @Test
    @Tag("Positive")
    @DisplayName("3. Добавление новой компании")
    public void shouldAddNewCompany(CompanyRepository companyRepository) throws SQLException {
        //Авторизация и получение токена
        String token = getAuthToken(login, password);

        //Создание новой компании с companyName, companyDescription
        int id = createAndGetNewCompanyId(token, companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);

        //Получение компании из DB
        CompanyDBEntity companyDBEntity = companyRepository.getById(id);

        assertEquals(companyName, companyDBEntity.getName());
        assertEquals(companyDescription, companyDBEntity.getDescription());
        assertTrue(companyDBEntity.isActive());
        assertNull(companyDBEntity.getDeletedAt());
    }

    @Test
    @Tag("Positive")
    @DisplayName("4. Получение компании по ID")
    public void shouldGetCompanyById(CompanyRepository repository) throws SQLException {

        //Библиотека с рандомными данными DataFaker
        Faker faker = new Faker(new Locale("ru"));
        companyName = faker.company().name();
        companyDescription = faker.company().industry();

        //Создание компании через БД
        int createdId = repository.create(companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(createdId > 0);

        //Получение компании по API по ID
        Company companyAPIResult = given().basePath("/company/" + createdId).when()
                .get()
                .then()
                .statusCode(200)
                .contentType("application/json; charset=utf-8")
                .extract().body().as(Company.class);

        //Получение компании из DB по ID
        CompanyDBEntity companyDBEntityExpected = repository.getById(createdId);

        //Проверка, что по Id мы получили одинаковые компании по API и из БД
        assertTrue(isCompaniesEqual(companyAPIResult, companyDBEntityExpected));
    }

    // 3 теста для проверки работы Resolver'ов
//    @Test
//    public void testById(CompanyRepository repository) throws SQLException {
//        CompanyDBEntity company = repository.getAll().get(0);
//        System.out.println(company.toString());
//    }
//
//    @Test
//    public void testById2(CompanyRepository repository) throws SQLException {
//        CompanyDBEntity company = repository.getAll().get(0);
//        System.out.println(company.toString());
//    }
//
//    @Test
//    public void testById3(CompanyRepository repository) throws SQLException {
//        CompanyDBEntity company = repository.getAll().get(0);
//        System.out.println(company.toString());
//    }

    @Test
    @Tag("Positive")
    @DisplayName("5. Изменение компании по ID")
    public void shouldPatchCompanyById(CompanyRepository repository) throws SQLException {

        //Аутентификация и получение токена
        String token = getAuthToken(login, password);

        //Создание новой компании с companyName, companyDescription
        int id = repository.create(companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);

        //Изменение компании по ID
        CompanyDBEntity companyDBEntityAfterPatch = given()
                .header("x-client-token", token)
                .log().ifValidationFails()
                .contentType("application/json; charset=utf-8")
                .body("{\"name\": \"" + newName + "\",\"description\": \"" + newDescription + "\"}")
                .when()
                .basePath(basePath + "/" + id)
                .patch()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json; charset=utf-8")
                .extract()
                .body().as(CompanyDBEntity.class);

        //Проверка тела ответа на команду PATCH
        assertEquals(id, companyDBEntityAfterPatch.getId());
        assertEquals(newName, companyDBEntityAfterPatch.getName());
        assertEquals(newDescription, companyDBEntityAfterPatch.getDescription());
        assertTrue(companyDBEntityAfterPatch.isActive());
        assertNotEquals(companyDBEntityAfterPatch.getCreateDateTime(), companyDBEntityAfterPatch.getLastChangedDateTime());     //Проверка, что дата изменения не равна дате создания
        assertNull(companyDBEntityAfterPatch.getDeletedAt());

        //Проверка, что в БД по ID компания с изменёнными данными
        CompanyDBEntity companyDBEntityById = repository.getById(id);

        assertEquals(id, companyDBEntityById.getId());
        assertEquals(newName, companyDBEntityById.getName());
        assertEquals(newDescription, companyDBEntityById.getDescription());
        assertTrue(companyDBEntityById.isActive());
        assertNotEquals(companyDBEntityById.getCreateDateTime(), companyDBEntityById.getLastChangedDateTime());     //Проверка, что дата изменения не равна дате создания
        assertNull(companyDBEntityById.getDeletedAt());
    }

    @Test
    @Tag("Positive")
    @DisplayName("6. Удаление компании по ID")
    public void shouldDeleteCompanyById(CompanyRepository repository) throws SQLException {
        //Аутентификация и получение токена
        String token = getAuthToken(login, password);

        //Создание новой компании с companyName, companyDescription
        int id = repository.create(companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);

        //Проверка, что в БД у компании deletedAt нулевое
        CompanyDBEntity companyDB = repository.getById(id);
        assertNull(companyDB.getDeletedAt());

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
                .basePath("/company/" + id)
                .log().ifValidationFails()
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                //-----------------------BUG--------------------------------//
                .statusCode(200)            //Завести BUG-репорт. SC должен быть 404, если компании нет
//                .header("Content-Length", nullValue())               //Проверка, что Content-Length = null
                .header("Content-Length", equalTo("0"))      //Проверка, что "Content-Length" = "0"
                .body(emptyOrNullString());                            //Проверка, что тело ответа пустое

        //Проверка, что в БД у компании deletedAt ненулевое
        companyDB = repository.getById(id);
        assertNotNull(companyDB.getDeletedAt());
    }

    private static int createAndGetNewCompanyId(String token, String companyName, String companyDescription) {
        return given()
                .header("x-client-token", token)
                .contentType("application/json; charset=utf-8")
                .body("{\"name\": \"" + companyName + "\",\"description\": \"" + companyDescription + "\"}")
                .when()
                .post()
                .then()
                .statusCode(201)
                .contentType("application/json; charset=utf-8")
                .extract().path("id");
    }

    private static Response getGetResponse() {
        return given()
                .when()
                .get()
                .then()
                .extract().response();
    }

    private static List<CompanyDBEntity> getGetResponse(Response response) {
        return response.then().extract().body().as(new TypeRef<List<CompanyDBEntity>>() {
        });
    }

    private static String getAuthToken(String login, String password) {
        return given()
                .basePath("/auth/login")
                .log().ifValidationFails(LogDetail.ALL)             //Логирование при ошибке
                .contentType("application/json; charset=utf-8")
                .body("{\"username\": \"" + login + "\", \"password\": \"" + password + "\"}")
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(201)                                    //Проверка статус-кода
                .contentType("application/json; charset=utf-8")     //Проверка content-type
                .extract()
                .path("userToken").toString();
    }

    private static void deleteCompanyById(String token, int id) {
        //Удаление компании по ID
        given()
                .header("x-client-token", token)
                .log().ifValidationFails()
                .when()
                .basePath(basePath + "/delete/" + id)
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }


    //Получение настроек подключения по API из файла .properties
    private static Properties getAPIProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(new File(propertiesFilePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

}
