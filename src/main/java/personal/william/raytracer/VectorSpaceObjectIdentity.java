package personal.william.raytracer;

import java.util.Objects;

public class VectorSpaceObjectIdentity {

    private final int id;
    private final String name;

    public VectorSpaceObjectIdentity(int id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name cannot be null.");
    }

    @Override
    public String toString() {
        return "VectorSpaceObjectIdentity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VectorSpaceObjectIdentity that = (VectorSpaceObjectIdentity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
