package db;

import jdbcTest.Car;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.sql.SQLException;
import java.util.List;

public class CarRepositoryHiber{
    private EntityManager em;

    public CarRepositoryHiber(EntityManager em) {
        this.em = em;
    }

    public List<Car> getAll() throws SQLException {
//        TypedQuery<Car> query = em.createQuery("SELECT c FROM car c", Car.class);
//        return query.getResultList();

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Car> criteriaQuery = criteriaBuilder.createQuery(Car.class);
        Root<Car> carRoot = criteriaQuery.from(Car.class);
        TypedQuery<Car> queryResult = em.createQuery(criteriaQuery.select(carRoot));
//                        .where(criteriaBuilder.equal(carRoot.get("id"), id)))
//                .getSingleResult();
        return queryResult.getResultList();
    }

    public List<Car> getAll(boolean isActive) throws SQLException {
        return null;
    }

    public Car getLast() throws SQLException {
        return null;
    }

    public Car getById(int id) throws SQLException {
        return null;
    }

    public int create(String name) throws SQLException {
        return 0;
    }

    public int create(String name, String description) throws SQLException {
        return 0;
    }

    public void deleteById(int id) {

    }
}
