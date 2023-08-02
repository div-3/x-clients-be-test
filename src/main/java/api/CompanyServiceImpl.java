package api;

import Model.Company;

import java.io.IOException;
import java.util.List;

public class CompanyServiceImpl implements CompanyService{
    @Override
    public List<Company> getAll() throws IOException {
        return null;
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
}
