package db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.db.CompanyEntity;

import java.sql.SQLException;
import java.util.List;

public class CompanyRepositoryHiber implements CompanyRepository{
    private EntityManager em;

    public CompanyRepositoryHiber(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<CompanyEntity> getAll() throws SQLException {
        TypedQuery<CompanyEntity> query = em.createQuery("SELECT c FROM CompanyEntity c WHERE c.deletedAt is null", CompanyEntity.class);
        return query.getResultList();

        //Тот же запрос, но через Hibenate API
//        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
//        CriteriaQuery<CompanyDBEntity> criteriaQuery = criteriaBuilder.createQuery(CompanyDBEntity.class);
//        Root<CompanyDBEntity> companyRoot = criteriaQuery.from(CompanyDBEntity.class);
//        TypedQuery<CompanyDBEntity> queryResult = em.createQuery(criteriaQuery.select(companyRoot));
////                        .where(criteriaBuilder.equal(companyRoot.get("id"), id)))
////                .getSingleResult();
//        return queryResult.getResultList();
    }

    @Override
    public List<CompanyEntity> getAll(boolean isActive) throws SQLException {
        return null;
    }

    @Override
    public CompanyEntity getLast() throws SQLException {
        return null;
    }

    @Override
    public CompanyEntity getById(int id) throws SQLException {
        TypedQuery<CompanyEntity> query = em.createQuery("SELECT c FROM CompanyEntity c WHERE c.deletedAt is null and c.id =:id", CompanyEntity.class);
        query.setParameter("id", id);
        return query.getSingleResult();
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
        TypedQuery<CompanyEntity> query = em.createQuery("DELETE c FROM CompanyEntity c WHERE c.id =:id", CompanyEntity.class);
        query.setParameter("id", id);
        int count = query.executeUpdate();
        System.out.println("Удалена компания с id = " + id + " количество = " + count);
    }
}
