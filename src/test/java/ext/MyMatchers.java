package ext;

import Model.Company;
import Model.CompanyDBEntity;

public class MyMatchers {
    public static boolean isCompaniesEqual(Company companyByAPI, CompanyDBEntity companyByDB){
        if (companyByDB.getId() != companyByAPI.getId()) return false;
        if (!companyByDB.getName().equals(companyByAPI.getName())) return false;
        if (!companyByDB.getDescription().equals(companyByAPI.getDescription())) return false;
        if (companyByDB.isActive() != companyByAPI.isActive()) return false;
        return true;
    }
}
