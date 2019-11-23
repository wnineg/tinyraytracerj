package personal.william.raytracer;

import java.util.Objects;

public class ParallelogramPlanePositioning implements Positionable.Positioning {

    private final Vector3d origin;

    private final UnitVector3d directionX;
    private final UnitVector3d directionY;

    public ParallelogramPlanePositioning(Vector3d origin, UnitVector3d directionX, UnitVector3d directionY) {
        Objects.requireNonNull(origin, "origin cannot be null.");
        Objects.requireNonNull(directionX, "directionX cannot be null.");
        Objects.requireNonNull(directionY, "directionY cannot be null.");

        this.origin = origin;
        this.directionX = directionX;
        this.directionY = directionY;
    }

    @Override
    public String toString() {
        return "ParallelogramPlanePositioning{" +
                "origin=" + origin +
                ", x=" + directionX +
                ", y=" + directionY +
                '}';
    }

    public Vector3d getOrigin() {
        return origin;
    }

    public UnitVector3d getDirectionX() {
        return directionX;
    }

    public UnitVector3d getDirectionY() {
        return directionY;
    }
}
