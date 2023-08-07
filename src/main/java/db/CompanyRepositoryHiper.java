package db;

import Model.CompanyDBEntity;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;

public class CompanyRepositoryHiper implements CompanyRepository{
    private EntityManager em;

    public CompanyRepositoryHiper(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<CompanyDBEntity> getAll() throws SQLException {
        return null;
    }

    @Override
    public List<CompanyDBEntity> getAll(boolean isActive) throws SQLException {
        return null;
    }

    @Override
    public CompanyDBEntity getLast() throws SQLException {
        return null;
    }

    @Override
    public CompanyDBEntity getById(int id) throws SQLException {
        return null;
    }

    @Override
    public int create(String name) throws SQLException {
        return 0;
    }

    @Override
    public int create(String name, String description) throws SQLException {
        return 0;
    }

    @Override
    public void deleteById(int id) {

    }
}
