package jdbcTest;

import jakarta.persistence.*;

import java.util.Objects;

@Table(name = "car")
@Entity
public class Car {
    @Id
    private String id;
    private String model;
    private String owner;

    public Car() {
    }

    public Car(String id, String model, String owner) {
        this.id = id;
        this.model = model;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(id, car.id) && Objects.equals(model, car.model) && Objects.equals(owner, car.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, model, owner);
    }

    @Override
    public String toString() {
        return "Car{" +
                "id='" + id + '\'' +
                ", model='" + model + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}
