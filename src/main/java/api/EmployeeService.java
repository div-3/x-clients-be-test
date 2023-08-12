package api;

import model.api.Company;
import model.api.Employee;

import java.io.IOException;
import java.util.List;

public interface EmployeeService {
    void setURI(String uri);
    List<Employee> getAllByCompanyId(int companyId) throws IOException;

    Employee generateEmployee();

    Employee getById(int id) throws IOException;

    int create(Employee employee) throws IOException;

    void deleteById(int id);
    void deleteByCompanyId(int companyId);

    Employee edit(Employee employee);

    void logIn(String login, String password);

    void logOut();
}
