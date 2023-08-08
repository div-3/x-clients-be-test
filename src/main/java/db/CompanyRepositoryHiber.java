package db;

import Model.CompanyDBEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.sql.SQLException;
import java.util.List;

public class CompanyRepositoryHiber implements CompanyRepository{
    private EntityManager em;

    public CompanyRepositoryHiber(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<CompanyDBEntity> getAll() throws SQLException {
//        TypedQuery<CompanyDBEntity> query = em.createQuery("SELECT c FROM company c WHERE c.deleted_at is not null", CompanyDBEntity.class);
        TypedQuery<CompanyDBEntity> query = em.createQuery("SELECT c FROM company c", CompanyDBEntity.class);
        return query.getResultList();
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
