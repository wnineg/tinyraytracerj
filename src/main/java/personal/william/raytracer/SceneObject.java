package personal.william.raytracer;

import java.util.Optional;

public interface SceneObject<P extends Positionable.Positioning> extends Positionable<P> {

    Optional<SurfacePoint> cast(P positioning, Vector3d orig, UnitVector3d dir);
}
