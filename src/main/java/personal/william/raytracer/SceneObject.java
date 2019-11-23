package personal.william.raytracer;

import java.util.Optional;

public interface SceneObject {

    Material getMaterial();

    Optional<Vector3d> getFirstIntersection(Vector3d positioning, Vector3d orig, Vector3d dir);

    Vector3d getNormalVector(Vector3d positioning, Vector3d point);
}
