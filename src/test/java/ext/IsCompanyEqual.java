package ext;

import model.db.CompanyEntity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.List;


//https://www.baeldung.com/hamcrest-custom-matchers     - Подробное описание создания собственных матчеров
public class IsCompanyEqual extends TypeSafeMatcher<model.api.Company> {
    private CompanyEntity companyEntityDB;
    List<String> errors = new ArrayList<>();

    IsCompanyEqual(CompanyEntity companyEntity){
        this.companyEntityDB = companyEntity;
    }

    @Override
    protected boolean matchesSafely(model.api.Company company) {
        if (companyEntityDB.getId() != company.getId()) errors.add("id");
        if (!companyEntityDB.getName().equals(company.getName())) errors.add("name");
        if (companyEntityDB.getDescription() == null || company.getDescription() == null){
            if (companyEntityDB.getDescription() != company.getDescription()) errors.add("description");
        } else if (!companyEntityDB.getDescription().equals(company.getDescription())) errors.add("description");
        if (companyEntityDB.isActive() != company.isActive()) errors.add("isActive");
        if (errors.size() == 0) return true;
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("CompanyEntity from DB: " + companyEntityDB.toString() + " Errors in fields:" + errors.toString());
    }

    public static Matcher<model.api.Company> isEqual(CompanyEntity companyEntityDB){
        return new IsCompanyEqual(companyEntityDB);
    }
}
