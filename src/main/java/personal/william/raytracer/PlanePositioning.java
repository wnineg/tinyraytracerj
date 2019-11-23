package personal.william.raytracer;

import java.util.Objects;

public class PlanePositioning implements Positionable.Positioning {

    private final Vector3d origin;

    private final UnitVector3d directionX;
    private final UnitVector3d directionY;

    public PlanePositioning(Vector3d origin, UnitVector3d directionX, UnitVector3d directionY) {
        Objects.requireNonNull(origin, "origin cannot be null.");
        Objects.requireNonNull(directionX, "directionX cannot be null.");
        Objects.requireNonNull(directionY, "directionY cannot be null.");
        if (directionX.dot(directionY) != 0) {
            throw new IllegalArgumentException("directionX and directionY must be perpendicular to each other.");
        }

        this.origin = origin;
        this.directionX = directionX;
        this.directionY = directionY;
    }

    @Override
    public String toString() {
        return "PlanePositioning{" +
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
