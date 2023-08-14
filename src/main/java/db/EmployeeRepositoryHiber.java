package db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.db.EmployeeEntity;
import net.datafaker.Faker;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class EmployeeRepositoryHiber implements EmployeeRepository{
    private EntityManager em;
    private Faker faker = new Faker(new Locale("RU"));

    public EmployeeRepositoryHiber(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<EmployeeEntity> getAllByCompanyId(int companyId) throws SQLException {
        TypedQuery<EmployeeEntity> query = em.createQuery(
                "SELECT e FROM EmployeeEntity e WHERE companyId = :id", EmployeeEntity.class);
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
        int lastId = getLast().getId();
        e.setId(++lastId);

        //Сохранение сотрудника в БД
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        return e.getId();
    }

    @Override
    public EmployeeEntity create(int companyId) throws SQLException {

        EmployeeEntity employee = new EmployeeEntity();
        int lastId = getLast().getId();
        employee.setId(lastId + 1);

        String[] name = faker.name().nameWithMiddle().split(" ");
        employee.setFirstName("TS_" + name[0]);
        employee.setLastName(name[2]);
        employee.setMiddleName(name[1]);

        employee.setCompanyId(companyId);

        employee.setEmail(faker.internet().emailAddress("a" + faker.number().digits(5)));

        employee.setAvatarUrl(faker.internet().url());
//        employee.setPhone(faker.phoneNumber().phoneNumber()); //Не проходит по формату

        //TODO: Написать BUG-репорт - при создании с неправильным телефоном возвращается ошибка 500 вместо 400
        employee.setPhone(faker.number().digits(10));

        Timestamp tmp = Timestamp.valueOf(LocalDateTime.now());
        employee.setCreateTimestamp(tmp);
        employee.setChangeTimestamp(tmp);

        employee.setBirthdate(Date.valueOf(faker.date().birthday("YYYY-MM-dd")));

        employee.setActive(true);

        //Сохранение сотрудника в БД
        em.getTransaction().begin();
        em.persist(employee);
        em.getTransaction().commit();
        return employee;
    }

    @Override
    public int update(EmployeeEntity e) throws SQLException {
        e.setChangeTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
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

    @Override
    public List<EmployeeEntity> getAll() {
        TypedQuery<EmployeeEntity> query = em.createQuery("SELECT e FROM EmployeeEntity e", EmployeeEntity.class);
        return query.getResultList();
    }
}
