package test;

import api.CompanyService;
import db.CompanyRepositoryHiber;
import ext.CompanyServiceResolver;
import ext.hibernate.HiberCompanyRepositoryResolver;
import ext.hibernate.HiberSessionResolver;
import jakarta.persistence.EntityManagerFactory;
import model.api.Company;
import model.db.CompanyEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static ext.IsCompanyEqual.isEqual;
import static ext.CommonHelper.getProperties;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*
* Тесты:
* 1. Позитивные:
* 1.1 Добавление новой компании
* 1.2 Получение списка компаний GET
* 1.3 Получение компании по id
* 1.4 Изменение информации по компании
* 1.5 Деактивация компании
* 1.6 Активация компании
* 1.7 Удаление компании
* 1.8 Добавление 50 компаний
* 2. Негативные:
* 2.1 Добавление компании без авторизации*/

@DisplayName("CompanyEntity Business Test:")
@ExtendWith({CompanyServiceResolver.class, HiberSessionResolver.class, HiberCompanyRepositoryResolver.class})
public class CompanyBusinessTest {
    private final static String propertiesFilePath = "src/main/resources/API_x_client.properties";
    private static Properties properties = new Properties();
    private static String baseUri;
    private static String login;
    private static String password;


    //Инициализация Hibernate (EntityManagerFactory)
    @BeforeAll
    public static void setUp(EntityManagerFactory emf){
        properties = getProperties(propertiesFilePath);
        baseUri = properties.getProperty("baseURI");
        login = properties.getProperty("login");
        password = properties.getProperty("password");
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.1 Добавление новой компании")
    public void shouldAddCompany(CompanyService apiService, CompanyRepositoryHiber repository) throws SQLException, IOException {
        apiService.logIn(login, password);
        int id = apiService.create("TestCompany", "TestDescription");
        CompanyEntity newCompanyDb = repository.getById(id);
        assertEquals("TestCompany", newCompanyDb.getName());
        assertEquals("TestDescription", newCompanyDb.getDescription());
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.2 Получение списка компаний GET")
    public void shouldGetCompanyList(CompanyService apiService, CompanyRepositoryHiber repository) throws SQLException, IOException {
        List<CompanyEntity> companiesDb = repository.getAll();
        List<Company> companiesApi = apiService.getAll();

        //Сравнение листов компаний
        assertEquals(companiesDb.size(), companiesApi.size());

        //Перекладываем в Map для быстрого поиска
        Map<Integer, CompanyEntity> mapDb = new HashMap<>();
        for (CompanyEntity c: companiesDb) {
            mapDb.put(c.getId(), c);
        }

//        companiesApi.get(0).setActive(false);     //Проверка работы теста
        for (Company c: companiesApi) {
            assertThat(c, isEqual(mapDb.get(c.getId())));
        }
    }
//    @Test
//    @DisplayName("Получение списка компаний GET")
//    public void shouldGetCompanyList1(CompanyRepositoryHiber repository) throws SQLException {
//        List<CompanyDBEntity> companies = repository.getAll();
//        System.out.println(companies.toString());
//    }
//    @Test
//    @DisplayName("Получение списка компаний GET")
//    public void shouldGetCompanyList2(CompanyRepositoryHiber repository) throws SQLException {
//        List<CompanyDBEntity> companies = repository.getAll();
//        System.out.println(companies.toString());
//    }
//    @Test
//    @DisplayName("Получение списка компаний GET")
//    public void shouldGetCompanyList3(CompanyRepositoryHiber repository) throws SQLException {
//        List<CompanyDBEntity> companies = repository.getAll();
//        System.out.println(companies.toString());
//    }
//    @Test
//    @DisplayName("Получение списка компаний GET")
//    public void shouldGetCompanyList4(CompanyRepositoryHiber repository) throws SQLException {
//        List<CompanyDBEntity> companies = repository.getAll();
//        System.out.println(companies.toString());
//    }    @Test
//    @DisplayName("Получение списка компаний GET")
//    public void shouldGetCompanyList5(CompanyRepositoryHiber repository) throws SQLException {
//        List<CompanyDBEntity> companies = repository.getAll();
//        System.out.println(companies.toString());
//    }
//    @Test
//    @DisplayName("Получение списка компаний GET")
//    public void shouldGetCompanyList6(CompanyRepositoryHiber repository) throws SQLException {
//        List<CompanyDBEntity> companies = repository.getAll();
//        System.out.println(companies.toString());
//    }    @Test
//    @DisplayName("Получение списка компаний GET")
//    public void shouldGetCompanyList7(CompanyRepositoryHiber repository) throws SQLException {
//        List<CompanyDBEntity> companies = repository.getAll();
//        System.out.println(companies.toString());
//    }
//    @Test
//    @DisplayName("Получение списка компаний GET")
//    public void shouldGetCompanyList8(CompanyRepositoryHiber repository) throws SQLException {
//        List<CompanyDBEntity> companies = repository.getAll();
//        System.out.println(companies.toString());
//    }



}
