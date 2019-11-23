package personal.william.raytracer;

import java.util.Objects;

public class Vector3d {

    private final double x;
    private final double y;
    private final double z;

    private Double norm;

    protected Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3d of(double x, double y, double z) {
        return (x == 0 && y == 0 && z == 0) ? UnitVector3d.ZERO : new Vector3d(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3d vector3d = (Vector3d) o;
        return Double.compare(vector3d.x, x) == 0 &&
                Double.compare(vector3d.y, y) == 0 &&
                Double.compare(vector3d.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "{" + x + ',' + y + ',' + z + '}';
    }

    public final double getX() {
        return x;
    }

    public final double getY() {
        return y;
    }

    public final double getZ() {
        return z;
    }

    public double norm() {
        return norm != null ? norm : calculateNorm();
    }

    public UnitVector3d normalize() {
        return UnitVector3d.normalize(this);
    }

    public Vector3d negate() {
        return new Vector3d(-x, -y, -z);
    }

    public Vector3d plus(Vector3d vector) {
        Objects.requireNonNull(vector, "vector cannot be null.");

        return Vector3d.of((this.x + vector.x), (this.y + vector.y), (this.z + vector.z));
    }

    public Vector3d minus(Vector3d vector) {
        Objects.requireNonNull(vector, "vector cannot be null.");

        return Vector3d.of((this.x - vector.x), (this.y - vector.y), (this.z - vector.z));
    }

    public Vector3d times(double scalar) {
        return scalar == 0 ? UnitVector3d.ZERO : new Vector3d(x * scalar, y * scalar, z * scalar);
    }

    public Vector3d divide(double scalar) {
        if (scalar == 0) throw new ArithmeticException("Divided by zero.");
        if (scalar == 1) return this;
        return new Vector3d(x / scalar, y / scalar, z / scalar);
    }

    public double dot(Vector3d vector) {
        Objects.requireNonNull(vector, "vector cannot be null.");

        return (this.x * vector.x) + (this.y * vector.y) + (this.z * vector.z);
    }

    public Vector3d cross(Vector3d vector) {
        Objects.requireNonNull(vector, "vector cannot be null.");

        if (vector.norm() == 0) return UnitVector3d.ZERO;

        return new Vector3d(
                ((this.y * vector.z) - (this.z * vector.y)),
                ((this.x * vector.z) - (this.z * vector.x)),
                ((this.x * vector.y) - (this.y * vector.x)));
    }

    public double getAngle(Vector3d vector) {
        Objects.requireNonNull(vector, "vector cannot be null.");

        if (this.norm() == 0 || vector.norm() == 0) throw new AlgebraException("Arbitrary angle with zero vector.");

        Vector3d a = this.normalize();
        Vector3d b = vector.normalize();
        return Math.acos(a.dot(b));
    }

    private double calculateNorm() {
        norm = Math.sqrt((x * x) + (y * y) + (z * z));
        return norm;
    }
}
