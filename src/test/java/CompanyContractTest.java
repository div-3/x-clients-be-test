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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

/*
* Тесты:
* Позитивные:
* 1. Получить список компаний +
* 2. Авторизация +
* 3. Добавление новой компании +
* 4. Получение компании по ID
* 5. Изменение компании по ID
* 6. Удаление компании по ID
* 7. Активировать компанию по ID
* 8. Деактивировать компанию по ID*/
@DisplayName("X-Clients-Be contract tests:")
public class CompanyContractTest {
    private final String login = "raphael";
    private final String password = "cool-but-crude";

    @BeforeEach
    public void setUp(){
        baseURI = "https://x-clients-be.onrender.com";
        basePath = "/company";
    }

    @Test
    @Tag("Positive")
    @DisplayName("1. Получить список компаний")
    public void shouldGetCompaniesList(){
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
    public void shouldAuth(){
        String token = getAuthToken(login, password);

        //Проверка, что токен с нужной длиной
        assertEquals(148, token.length());
    }

    @Test
    @Tag("Positive")
    @DisplayName("3. Добавление новой компании")
    public void shouldAddNewCompany(){
        //Аутентификация и получение токена

        String companyName = "ООО Рога и копыта";
        String companyDescription = "Все понемногу";

        String token = getAuthToken(login, password);
        int id = createAndGetNewCompanyId(token, companyName, companyDescription);

        //Проверка, что номер новой компании больше 0
        assertTrue(id > 0);
    }

    private static int createAndGetNewCompanyId(String token, String companyName, String companyDescription) {
        return given()
                .header("x-client-token", token)
                .contentType("application/json; charset=utf-8")
                .body("{\"name\": \"" + companyName + "\",\"description\": \"" + companyDescription + "\"}")
                .contentType(ContentType.JSON)
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
        return response.then().extract().body().as(new TypeRef<List<Company>>() {});
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


}
