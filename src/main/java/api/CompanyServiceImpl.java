package api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import model.api.Company;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CompanyServiceImpl implements CompanyService{
    private final static String propertiesFilePath = "src/main/resources/API_x_client.properties";
    private Properties properties;
    private String uri;
    private String login = "";
    private String password = "";
    private String token = "";
    private boolean isAuth;
    Map<String, String> headers = new HashMap<>();


    public CompanyServiceImpl(String uri) {
        this.uri = uri;
    }

    @Override
    public void setURI(String uri) {
        this.uri = uri;
    }

    @Override
    public List<Company> getAll() throws IOException {
        return given()
                .baseUri(uri + "/company")
                .headers(headers)
                .when().get()
                .then().extract().response().then().extract().body().as(new TypeRef<List<Company>>() {});
    }

    @Override
    public List<Company> getAll(boolean isActive) throws IOException {
        return null;
    }

    @Override
    public Company getById(int id) throws IOException {
        return null;
    }

    @Override
    public int create(String name) throws IOException {

        return 0;
    }

    @Override
    public int create(String name, String description) throws IOException {
        return given()
                .headers(headers)
                .baseUri(uri + "/company")
                .contentType("application/json; charset=utf-8")
                .body("{\"name\": \"" + name + "\",\"description\": \"" + description + "\"}")
                .when()
                .post()
                .then()
                .statusCode(201)
                .contentType("application/json; charset=utf-8")
                .extract().path("id");
    }

    @Override
    public void deleteById(int id) {

    }

    @Override
    public Company edit(int id, String newName) {
        return null;
    }

    @Override
    public Company edit(int id, String newName, String newDescription) {
        return null;
    }

    @Override
    public Company changeStatus(int id, boolean isActive) {
        return null;
    }

    @Override
    public void logIn(String login, String password) {
        this.token = given()
                .baseUri(uri + "/auth/login")
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

        if (token != "") {
            isAuth = true;
            //Если залогинены, то добавляем токен в headers
            headers.put("x-client-token", token);
        }
    }

    @Override
    public void logOut() {
        token = "";
        isAuth = false;
        //Если разлогинены, то убираем токен из headers
        headers.remove("x-client-token");
    }


    //Получить параметры из файла
    public Properties getProperties(String path) {
        File propFile = new File(path);
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(propFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
