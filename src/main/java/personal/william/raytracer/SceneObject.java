package personal.william.raytracer;

import java.util.Optional;

public interface SceneObject {

    Material getMaterial();

    Optional<SurfacePoint> cast(Vector3d positioning, Vector3d orig, Vector3d dir);
}
