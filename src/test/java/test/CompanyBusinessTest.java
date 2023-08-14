package test;

import api.CompanyService;
import db.CompanyRepository;
import ext.CompanyServiceResolver;
import ext.hibernate.HiberCompanyRepositoryResolver;
import ext.hibernate.HiberSessionResolver;
import jakarta.persistence.EntityManagerFactory;
import model.api.Company;
import model.db.CompanyEntity;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static ext.CommonHelper.getProperties;
import static ext.IsCompanyEqual.isEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*
* Тесты:
* 1. Позитивные:
* 1.1 Добавление новой компании +
* 1.2 Получение списка компаний GET +
* 1.3 Получение компании по id (доделать)
* 1.4 Изменение информации по компании (доделать)
* 1.5 Деактивация компании (доделать)
* 1.6 Активация компании (доделать)
* 1.7 Удаление компании (доделать)
* 1.8 Добавление 50 компаний (доделать)
* 2. Негативные:
* 2.1 Добавление компании без авторизации (доделать)*/


//В тестах используется для работы: с БД - Hibernate, с API - RestAssured.
@DisplayName("Company business tests:")
@ExtendWith({CompanyServiceResolver.class, HiberSessionResolver.class, HiberCompanyRepositoryResolver.class})
public class CompanyBusinessTest {
    private final static String PROPERTIES_FILE_PATH = "src/main/resources/API_x_client.properties";
    Faker faker = new Faker(new Locale("RU"));
    private static Properties properties = new Properties();
    private static String baseUriString;
    private static String login;
    private static String password;
    private static List<Integer> companyToDelete = new ArrayList<>();


    //Инициализация Hibernate (EntityManagerFactory)
    @BeforeAll
    public static void setUp(EntityManagerFactory emf){
        properties = getProperties(PROPERTIES_FILE_PATH);
        baseUriString = properties.getProperty("baseURI");
        login = properties.getProperty("login");
        password = properties.getProperty("password");
    }

    //Очистка тестовых данных
    @AfterAll
    public static void cleanTD(CompanyRepository companyRepository) {
        for (int i : companyToDelete) {
            companyRepository.deleteById(i);
        }
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.1 Добавление новой компании")
    public void shouldAddCompany(CompanyService apiService, CompanyRepository repository) throws SQLException, IOException {
        apiService.logIn(login, password);
        String name = faker.company().name();
        String description = faker.company().profession();

        int id = apiService.create(name, description);
        companyToDelete.add(id);

        CompanyEntity newCompanyDb = repository.getById(id);
        assertEquals(name, newCompanyDb.getName());
        assertEquals(description, newCompanyDb.getDescription());
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.2 Получение списка компаний GET")
    public void shouldGetCompanyList(CompanyService apiService, CompanyRepository repository) throws SQLException, IOException {
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
}
