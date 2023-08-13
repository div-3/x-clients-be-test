package api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import model.api.Company;
import model.api.Employee;
import net.datafaker.Faker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static io.restassured.RestAssured.given;

public class EmployeeServiceImpl implements EmployeeService {
    private final static String propertiesFilePath = "src/main/resources/API_x_client.properties";
    private Properties properties;
    private String uri;
    private String login = "";
    private String password = "";
    private String token = "";
    private boolean isAuth;
    private Map<String, String> headers = new HashMap<>();
    private AuthService authService = AuthService.getInstance();
    private Faker faker = new Faker(new Locale("ru"));


    public EmployeeServiceImpl(String uri) {
        this.uri = uri;
    }

    @Override
    public void setURI(String uri) {
        this.uri = uri;
    }

    @Override
    public List<Employee> getAllByCompanyId(int companyId) throws IOException {
        return given()
                .baseUri(uri + "/employee")
                .log().ifValidationFails()
                .param("company", companyId)
                .headers(headers)
                .header("accept", "application/json")
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .extract()
                .response()
                .then()
                .extract()
                .body().as(new TypeRef<List<Employee>>() {
                });
    }

    @Override
    public Employee generateEmployee() {
        Employee employee = new Employee();
        employee.setId(0);
        String[] name = faker.name().nameWithMiddle().split(" ");
        employee.setFirstName(name[0]);
        employee.setLastName(name[2]);
        employee.setMiddleName(name[1]);
        employee.setCompanyId(0);
        employee.setEmail(faker.internet().emailAddress("a" + faker.number().digits(5)));
        employee.setUrl(faker.internet().url());
//        employee.setPhone(faker.phoneNumber().phoneNumber()); //Не проходит по формату
        //TODO: Написать BUG-репорт - при создании с неправильным телефоном возвращается ошибка 500 вместо 400
        employee.setPhone(faker.number().digits(10));
        employee.setBirthdate(faker.date().birthday("YYYY-MM-dd"));
        employee.setIsActive(true);
        return employee;
    }

    @Override
    public Employee getById(int id) throws IOException {
        return given()
                .baseUri(uri + "/employee" + "/" + id)
                .log().ifValidationFails()
                .headers(headers)
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .extract()
                .response()
                .then()
                .extract()
                .body().as(new TypeRef<Employee>() {
                });
    }

    @Override
    public int create(Employee employee) throws IOException {
        return given()
                .log().all()
                .headers(headers)
                .baseUri(uri + "/employee")
                .contentType("application/json; charset=utf-8")
                .body(employee)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(201)
                .contentType("application/json; charset=utf-8")
                .extract().path("id");
    }

    @Override
    public int update(Employee employee) {
        return given()
                .log().all()
                .headers(headers)
                .baseUri(uri + "/employee" + "/" + employee.getId())
                .contentType("application/json; charset=utf-8")
                .body("{\"lastName\": \"" + employee.getLastName() + "\"," +
                        "\"email\": \"" + employee.getEmail() + "\"," +
                        "\"url\": \"" + employee.getUrl() + "\"," +
                        "\"phone\": \"" + employee.getPhone() + "\"," +
                        "\"isActive\": " + employee.getIsActive() + "}")
                .when()
                .patch()
                .then()
                .log().all()
//                .statusCode(201)
                .contentType("application/json; charset=utf-8")
                .extract().path("id");
    }


    //    @Override
    public int create(String name, String description) throws IOException {
        return given()
                .log().ifValidationFails()
                .headers(headers)
                .baseUri(uri + "/employee")
                .contentType("application/json; charset=utf-8")
                .body("{\"name\": \"" + name + "\",\"description\": \"" + description + "\"}")
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .contentType("application/json; charset=utf-8")
                .extract().path("id");
    }

    @Override
    public void deleteById(int id) {

    }

    @Override
    public void deleteByCompanyId(int companyId) {

    }

    @Override
    public Employee edit(Employee employee) {
        return null;
    }

    @Override
    public void logIn(String login, String password) {
//        this.token = given()
//                .baseUri(uri + "/auth/login")
//                .log().ifValidationFails(LogDetail.ALL)             //Логирование при ошибке
//                .contentType("application/json; charset=utf-8")
//                .body("{\"username\": \"" + login + "\", \"password\": \"" + password + "\"}")
//                .when()
//                .post()
//                .then()
//                .log().ifValidationFails()
//                .statusCode(201)                                    //Проверка статус-кода
//                .contentType("application/json; charset=utf-8")     //Проверка content-type
//                .extract()
//                .path("userToken").toString();
        this.token = authService.logIn(login, password);
        if (!token.equals("")) {
//            isAuth = true;
            //Если залогинены, то добавляем токен в headers
            headers.put("x-client-token", token);
        }
    }

    @Override
    public void logOut() {
        authService.logOut(login);
        token = "";
//        isAuth = false;
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
