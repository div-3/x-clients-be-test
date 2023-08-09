package api;

import io.restassured.common.mapper.TypeRef;
import model.api.Company;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;

public class CompanyServiceImpl implements CompanyService{
    private final static String propertiesFilePath = "src/main/resources/API_x_client.properties";
    private Properties properties = getProperties(propertiesFilePath);



    @Override
    public List<Company> getAll() throws IOException {
        String login = properties.getProperty("login");
        String password = properties.getProperty("password");
        URI uri;
        try {
            uri = new URI(properties.getProperty("baseURI") + "/company");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return given()
                .when()
                .get(uri)
                .then()
                .extract().response().then().extract().body().as(new TypeRef<List<Company>>() {});
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
        return 0;
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
