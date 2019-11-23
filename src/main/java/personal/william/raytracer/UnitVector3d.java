package personal.william.raytracer;

import java.util.Objects;

public class UnitVector3d extends Vector3d {

    public static final UnitVector3d ZERO = new UnitVector3d(0, 0, 0) {
        @Override
        public double norm() {
            return 0;
        }

        @Override
        public Vector3d plus(Vector3d vector) {
            Objects.requireNonNull(vector, "vector cannot be null.");

            return vector;
        }

        @Override
        public Vector3d minus(Vector3d vector) {
            Objects.requireNonNull(vector, "vector cannot be null.");

            return vector.negate();
        }

        @Override
        public Vector3d times(double scalar) {
            return this;
        }

        @Override
        public Vector3d divide(double scalar) {
            return this;
        }

        @Override
        public double dot(Vector3d vector) {
            Objects.requireNonNull(vector, "vector cannot be null.");

            return 0;
        }

        @Override
        public Vector3d cross(Vector3d vector) {
            Objects.requireNonNull(vector, "vector cannot be null.");

            return this;
        }

        @Override
        public double getAngle(Vector3d vector) {
            Objects.requireNonNull(vector, "vector cannot be null.");

            throw new AlgebraException("Arbitrary angle with zero vector.");
        }
    };

    public static final UnitVector3d X = new OneUnitVector(1, 0, 0);
    public static final UnitVector3d Y = new OneUnitVector(0, 1, 0);
    public static final UnitVector3d Z = new OneUnitVector(0, 0, 1);

    private UnitVector3d(double x, double y, double z) {
        super(x, y, z);
    }

    public static UnitVector3d normalize(double x, double y, double z) {
        double norm = Math.sqrt((x * x) + (y * y) + (z * z));
        if (norm == 0) return ZERO;
        else if (norm == 1) return new OneUnitVector(x, y, z);
        else return new OneUnitVector((x / norm), (y / norm), (z / norm));
    }

    public static UnitVector3d normalize(Vector3d vector) {
        Objects.requireNonNull(vector, "vector cannot be null.");

        if (vector instanceof UnitVector3d) return (UnitVector3d) vector;
        return normalize(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public UnitVector3d normalize() {
        return this;
    }

    @Override
    public UnitVector3d negate() {
        return new OneUnitVector(-getX(), -getY(), -getZ());
    }

    private static class OneUnitVector extends UnitVector3d {
        private OneUnitVector(double x, double y, double z) {
            super(x, y, z);
        }

        @Override
        public double norm() {
            return 1;
        }
    }
}
