package JDBCTest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JDBCTest {

    public static void main(String[] args) throws SQLException {
        //Получение параметров подключения из файла
        Properties JDBCProperties = getProperties();
        System.out.println(JDBCProperties.getProperty("text"));

        try (Connection connection = DriverManager.getConnection(
                JDBCProperties.getProperty("connectionString"),
                JDBCProperties.getProperty("user"),
                JDBCProperties.getProperty("password")))
            {
            //Получение и печать списка компаний
//            printList(getCarListFromDB(connection));
            printList(getListFromDB(connection, Car.class));

            System.out.println("----------------------\nДобавляем машины\n---------------------");
            Car car1 = new Car("М345ЕМ49", "Honda Civic", "Пономарёв Л.К.");
            Car car2 = new Car("Л111НВ77", "Toyota Corola", "Романцов Р.Р.");

            int rowAffected = insertIntoCar(connection, car1);
            rowAffected += insertIntoCar(connection, car2);
            System.out.println("Добавлено записей:" + rowAffected);

            printList(getCarListFromDB(connection));

            System.out.println("----------------------\nУдаляем машины\n---------------------");
            rowAffected = deleteFromCar(connection, car1);
            rowAffected += deleteFromCar(connection, car2);
            System.out.println("Удалено записей:" + rowAffected);

            printList(getCarListFromDB(connection));
        }catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    //Получить параметры подключения к DB
    private static Properties getProperties(){
        File prop = new File("src/main/resources/JDBC.properties");
        Properties JDBCProperties = new Properties();
        try {
            JDBCProperties.load(new FileReader(prop));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return JDBCProperties;
    }

    private static int deleteFromCar(Connection connection, Car car) throws SQLException {
        String deleteQuery = "DELETE FROM car WHERE id = '" + car.getId() + "';";
//        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM car WHERE id = '" + car.getId() + "';");
        return connection.createStatement().executeUpdate(deleteQuery);
//        preparedStatement.executeQuery();
    }

    private static int insertIntoCar(Connection connection, Car car) throws SQLException {
        String query = "INSERT INTO car (\"id\", \"model\", \"owner\") " +
                "VALUES ('" + car.getId() + "', '" + car.getModel() + "', '" + car.getOwner() + "')";
        return connection.createStatement().executeUpdate(query);
    }

    private static List<Car> getCarListFromDB(Connection connection) throws SQLException {
        String query = "SELECT * FROM car;";
        ResultSet resultSet = connection.createStatement().executeQuery(query);
        List<Car> cars = new ArrayList<>();
        int i = 0;
        while (resultSet.next()){
            String id = resultSet.getString("id");
            String model = resultSet.getString("model");
            String owner = resultSet.getString("owner");
            cars.add(i, new Car(id, model, owner));
            i++;
        }
        return cars;
    }

    private static <T> List<T> getListFromDB(Connection connection, T t) throws SQLException {
        String query = "SELECT * FROM car;";
        ResultSet resultSet = connection.createStatement().executeQuery(query);
        List<T> t1 = new ArrayList<>();
        int i = 0;
        while (resultSet.next()){

            String id = resultSet.getString("id");
            String model = resultSet.getString("model");
            String owner = resultSet.getString("owner");
            System.out.println(t.getClass().getName().toString());
            switch (t.getClass().getName()){
                case "JDBCTest.Car":
                    T t2 =  (T) new Car(id, model, owner);
            }
            T t2 = null;
            t1.add(i, t2);
            i++;
        }
        return t1;
    }

    private static <X> void printList(List<X> cList){
        for (X x: cList) {
            System.out.println(x);
        }
    }


}
