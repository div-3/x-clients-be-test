package ext;

import Model.Company;
import Model.CompanyDBEntity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


//https://www.baeldung.com/hamcrest-custom-matchers     - Подробное описание создания собственных матчеров
public class IsCompanyEqual extends TypeSafeMatcher<Company> {
    private CompanyDBEntity companyDB;
    List<String> errors = new ArrayList<>();

    IsCompanyEqual(CompanyDBEntity companyDBEntity){
        this.companyDB = companyDBEntity;
    }

    @Override
    protected boolean matchesSafely(Company company) {
        if (companyDB.getId() != company.getId()) errors.add("id");
        if (!companyDB.getName().equals(company.getName())) errors.add("name");
        if (!companyDB.getDescription().equals(company.getDescription())) errors.add("description");
        if (companyDB.isActive() != company.isActive()) errors.add("isActive");
        if (errors.size() == 0) return true;
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Company from DB: " + companyDB.toString() + " Errors in fields:" + errors.toString());
    }

    public static Matcher<Company> isEqual(CompanyDBEntity companyDB){
        return new IsCompanyEqual(companyDB);
    }
}
