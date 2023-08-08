package test;

import Model.CompanyDBEntity;
import db.CompanyRepositoryHiber;
import ext.HiberCompanyRepositoryResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.util.List;

@DisplayName("Company Business Test:")
@ExtendWith(HiberCompanyRepositoryResolver.class)
public class CompanyBusinessTest {

    @Test
    @DisplayName("Получение списка компаний GET")
    public void shouldGetCompanyList(CompanyRepositoryHiber repository) throws SQLException {
        List<CompanyDBEntity> companies = repository.getAll();
        System.out.println(companies.toString());
    }

}
