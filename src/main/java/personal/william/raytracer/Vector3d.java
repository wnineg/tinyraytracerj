package personal.william.raytracer;

import java.util.Objects;

public class Vector3d {

    private final double x;
    private final double y;
    private final double z;

    private Double norm;
    private Vector3d normalized;

    public Vector3d(double x, double y, double z) {
        this(x, y, z, null, null);
    }

    private Vector3d(double x, double y, double z, Double norm, Vector3d normalized) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.norm = norm;
        this.normalized = normalized;
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

    public Vector3d normalize() {
        return normalized != null ? normalized : calculateNormalized();
    }

    public Vector3d negate() {
        return new Vector3d(-x, -y, -z);
    }

    public Vector3d plus(Vector3d vector) {
        Objects.requireNonNull(vector, "vector cannot be null.");

        return new Vector3d((this.x + vector.x), (this.y + vector.y), (this.z + vector.z));
    }

    public Vector3d minus(Vector3d vector) {
        Objects.requireNonNull(vector, "vector cannot be null.");

        return new Vector3d((this.x - vector.x), (this.y - vector.y), (this.z - vector.z));
    }

    public Vector3d times(double scalar) {
        return new Vector3d(x * scalar, y * scalar, z * scalar);
    }

    public Vector3d divide(double scalar) {
        return new Vector3d(x / scalar, y / scalar, z / scalar);
    }

    public double dot(Vector3d vector) {
        Objects.requireNonNull(vector, "vector cannot be null.");

        return (this.x * vector.x) + (this.y * vector.y) + (this.z * vector.z);
    }

    public Vector3d cross(Vector3d vector) {
        Objects.requireNonNull(vector, "vector cannot be null.");

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

    private Vector3d calculateNormalized() {
        double norm = norm();
        if (norm == 1) {
            normalized = this;
            return normalized;
        }

        normalized = divide(norm);
        return normalized;
    }
}
