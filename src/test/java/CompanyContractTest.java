/*
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
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import org.apache.http.impl.io.ContentLengthInputStream;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;

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
public class CompanyContractTest {
    private final static String login = "raphael";
    private final static String password = "cool-but-crude";
    private final static String companyName = "ООО Рога и копыта";
    private final static String companyDescription = "Все понемногу";
    private final static String newName = "ООО РиК2";
    private final static String newDescription = "То же самое";

    @BeforeEach
    public void setUp() {
        baseURI = "https://x-clients-be.onrender.com";
        basePath = "/company";
    }

    @AfterAll
    public static void clearUp() {
        List<Company> listToDelete = getGetResponse(getGetResponse());
        String token = getAuthToken(login, password);
        for (Company c : listToDelete) {
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
    public void shouldAddNewCompany() {
        //Аутентификация и получение токена
        String token = getAuthToken(login, password);

        //Создание новой компании с companyName, companyDescription
        int id = createAndGetNewCompanyId(token, companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);
    }

    @Test
    @Tag("Positive")
    @DisplayName("4. Получение компании по ID")
    public void shouldGetCompanyById() {
        //Создаём компанию
        String token = getAuthToken(login, password);

        //Создание новой компании с companyName, companyDescription
        int id = createAndGetNewCompanyId(token, companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);

        //Получаем компанию из общего списка
        List<Company> companies = getGetResponse(getGetResponse());
        Company companyExpected = null;
        for (Company c : companies) {
            if (c.getId() == id) companyExpected = c;
        }

        //Получение компании по ID
        Company companyResult = given().basePath("/company/" + id).when()
                .get()
                .then()
                .statusCode(200)
                .contentType("application/json; charset=utf-8")
                .extract().body().as(Company.class);

        //Проверка, что по Id мы получили такую же компанию, что и в общем списке
        assertEquals(companyExpected, companyResult);
    }

    @Test
    @Tag("Positive")
    @DisplayName("5. Изменение компании по ID")
    public void shouldPatchCompanyById() {


        //Аутентификация и получение токена
        String token = getAuthToken(login, password);

        //Создание новой компании с companyName, companyDescription
        int id = createAndGetNewCompanyId(token, companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);

        //Изменение компании по ID
        Company companyAfterPatch = given()
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
                .body().as(Company.class);

        //Проверка тела ответа на команду PATCH
        assertEquals(id, companyAfterPatch.getId());
        assertEquals(newName, companyAfterPatch.getName());
        assertEquals(newDescription, companyAfterPatch.getDescription());
        assertFalse(companyAfterPatch.isActive());
        assertNotEquals(companyAfterPatch.getCreateDateTime(), companyAfterPatch.getLastChangedDateTime());     //Проверка, что дата изменения не равна дате создания
        assertNull(companyAfterPatch.getDeletedAt());

        //Проверка, что по ID компания с изменёнными данными
        Company companyById = given()
                .basePath("/company/" + id)
                .log().ifValidationFails()
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json; charset=utf-8")
                .extract()
                .body().as(Company.class);

        //Проверка тела ответа на команду PATCH
        assertEquals(id, companyById.getId());
        assertEquals(newName, companyById.getName());
        assertEquals(newDescription, companyById.getDescription());
        assertFalse(companyById.isActive());
        assertNotEquals(companyById.getCreateDateTime(), companyById.getLastChangedDateTime());     //Проверка, что дата изменения не равна дате создания
        assertNull(companyById.getDeletedAt());
    }

    @Test
    @Tag("Positive")
    @DisplayName("6. Удаление компании по ID")
    public void shouldDeleteCompanyById() {
        //Аутентификация и получение токена
        String token = getAuthToken(login, password);

        //Создание новой компании с companyName, companyDescription
        int id = createAndGetNewCompanyId(token, companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);

        //Удаление компании по ID
        deleteCompanyById(token, id);

        //Задержка для гарантированного прохождения теста
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Проверка, что по ID компания больше недоступна
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
    }

    private static int createAndGetNewCompanyId(String token, String companyName, String companyDescription) {
        return given()
                .header("x-client-token", token)
                .contentType("application/json; charset=utf-8")
                .body("{\"name\": \"" + companyName + "\",\"description\": \"" + companyDescription + "\"}")
//                .contentType(ContentType.JSON)
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

    private static List<Company> getGetResponse(Response response) {
        return response.then().extract().body().as(new TypeRef<List<Company>>() {
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

}
