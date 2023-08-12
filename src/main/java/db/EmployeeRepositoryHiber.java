package db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import model.db.CompanyEntity;
import model.db.EmployeeEntity;
import org.hamcrest.core.AllOf;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;

public class EmployeeRepositoryHiber implements EmployeeRepository{
    private EntityManager em;

    public EmployeeRepositoryHiber(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<EmployeeEntity> getAllByCompanyId(int companyId) throws SQLException {
        TypedQuery<EmployeeEntity> query = em.createQuery(
                "SELECT e FROM EmployeeEntity e WHERE e.companyId = :id", EmployeeEntity.class);
        query.setParameter("id", companyId);
        return query.getResultList();
    }

    @Override
    public EmployeeEntity getById(int id) throws SQLException {
//        TypedQuery<EmployeeEntity> query = em.createQuery(
//                "SELECT e FROM EmployeeEntity e WHERE e.id = :id", EmployeeEntity.class);
//        query.setParameter("id", id);
//        return query.getSingleResult();

        /*Или*/

        return em.find(EmployeeEntity.class, id);
    }

    @Override
    public int create(EmployeeEntity e) throws SQLException {
//        Timestamp tmp = Timestamp.valueOf(LocalDateTime.now());
//        e.setCreateTimestamp(tmp);
//        e.setChangeTimestamp(tmp);
        int lastId = getLast().getId();
        e.setId(++lastId);

        //Сохранение сотрудника в БД
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        return e.getId();
    }

    @Override
    public int update(EmployeeEntity e) throws SQLException {
        e.setChangeTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        em.getTransaction().begin();
        em.persist(e);
//        em.getTransaction().commit();
        return 0;
    }

    @Override
    public void deleteById(int id) {
        EmployeeEntity employee = em.find(EmployeeEntity.class, id);
        em.getTransaction().begin();
        em.remove(employee);
        em.getTransaction().commit();
        System.out.println("Удален сотрудник с id = " + id);
    }

    @Override
    public EmployeeEntity getLast() {
        TypedQuery<EmployeeEntity> query = em.createQuery(
                "SELECT e FROM EmployeeEntity e ORDER BY e.id DESC LIMIT 1", EmployeeEntity.class);
        return query.getSingleResult();
    }
}
