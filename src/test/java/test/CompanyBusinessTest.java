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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@DisplayName("CompanyEntity Business Test:")
@ExtendWith({CompanyServiceResolver.class, HiberSessionResolver.class, HiberCompanyRepositoryResolver.class})
public class CompanyBusinessTest {

    //Инициализация Hibernate (EntityManagerFactory)
    @BeforeAll
    public static void setUpHibernate(EntityManagerFactory emf){
    }

    @Test
    @DisplayName("Получение списка компаний GET")
    public void shouldGetCompanyList(CompanyService apiService, CompanyRepositoryHiber repository) throws SQLException, IOException {
        List<CompanyEntity> companiesDb = repository.getAll();
        System.out.println(companiesDb.toString());

        System.out.println("\n-------------------\n Компании по API\n-------------------\n");
        List<Company> companiesApi = apiService.getAll();
        System.out.println(companiesApi.toString());

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
