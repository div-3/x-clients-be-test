package test;

import api.CompanyService;
import db.CompanyRepository;
import ext.CompanyServiceResolver;
import ext.hibernate.HiberCompanyRepositoryResolver;
import ext.hibernate.HiberEMFResolver;
import jakarta.persistence.EntityManagerFactory;
import model.api.Company;
import model.db.CompanyEntity;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
//Тесты выполняются в последовательном режиме
@DisplayName("Company business tests:")
@ExtendWith({CompanyServiceResolver.class, HiberEMFResolver.class, HiberCompanyRepositoryResolver.class})
public class CompanyBusinessTest {
    private final static String PROPERTIES_FILE_PATH = "src/main/resources/API_x_client.properties";
    private final static String PREFIX = "TS_";
    private String companyName = "";
    private String companyDescription = "";
    Faker faker = new Faker(new Locale("RU"));
    private static Properties properties = new Properties();
    private static String baseUriString;
    private static String login;
    private static String password;

    //Инициализация Hibernate (EntityManagerFactory)
    @BeforeAll
    public static void setUpBeforeAll(EntityManagerFactory emf) {
        properties = getProperties(PROPERTIES_FILE_PATH);
        baseUriString = properties.getProperty("baseURI");
        login = properties.getProperty("login");
        password = properties.getProperty("password");
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        //Библиотека с рандомными данными DataFaker
        Faker faker = new Faker(new Locale("ru"));
        companyName = PREFIX + faker.company().name();
        companyDescription = PREFIX + faker.company().industry();

        Thread.sleep(500);
    }

    @AfterEach
    public void coolDownAfter() throws InterruptedException {
        Thread.sleep(500);
    }

    //Очистка тестовых данных
    @AfterAll
    public static void cleanTD(CompanyRepository companyRepository) throws SQLException {
        companyRepository.clean("");
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.1 Добавление новой компании")
    public void shouldAddCompany(CompanyService apiService, CompanyRepository repository) throws SQLException {
        apiService.logIn(login, password);

        int id = apiService.create(companyName, companyDescription);

        CompanyEntity newCompanyDb = repository.getById(id);
        assertEquals(companyName, newCompanyDb.getName());
        assertEquals(companyDescription, newCompanyDb.getDescription());
    }

    @Test
    @Tag("Positive")
    @DisplayName("1.2 Получение списка компаний GET")
    public void shouldGetCompanyList(CompanyService apiService, CompanyRepository repository) throws SQLException {
        List<CompanyEntity> companiesDb = repository.getAll();
        List<Company> companiesApi = apiService.getAll();

        //Сравнение листов компаний
        assertEquals(companiesDb.size(), companiesApi.size());

        //Перекладываем в Map для быстрого поиска
        Map<Integer, CompanyEntity> mapDb = new HashMap<>();

        mapDb = companiesDb
                .stream()
                .collect(Collectors.toMap(
                        c -> c.getId(),
                        c -> c
                ));

        //Или
//        for (CompanyEntity c : companiesDb) {
//            mapDb.put(c.getId(), c);
//        }

        for (Company c : companiesApi) {
            assertThat(c, isEqual(mapDb.get(c.getId())));
        }
    }
}
