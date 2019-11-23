package personal.william.raytracer;

import java.util.Optional;

public class Sphere implements SceneObject<Sphere.Positioning> {

    public static class Positioning implements Positionable.Positioning {

        private final Vector3d center;

        public Positioning(Vector3d center) {
            this.center = center;
        }

        public Positioning(double x, double y, double z) {
            this(Vector3d.of(x, y, z));
        }

        public Vector3d getCenter() {
            return center;
        }
    }

    private final Material material;
    private final double radius;

    public Sphere(Material material, double radius) {
        this.material = material;
        this.radius = radius;
    }

    @Override
    public Optional<SurfacePoint> cast(Positioning positioning, Vector3d orig, UnitVector3d dir) {
        Vector3d center = positioning.getCenter();
        Vector3d ray = center.minus(orig);
        double rayToCenterDist = dir.dot(ray);
        double centerDistSquare = ray.dot(ray) - (rayToCenterDist * rayToCenterDist);
        double radiusSquare = radius * radius;
        if (centerDistSquare > (radiusSquare)) return Optional.empty();

        float intersectedDist = (float) Math.sqrt(radiusSquare - centerDistSquare);
        double hitDist = rayToCenterDist - intersectedDist;
        if (hitDist < 0) hitDist = rayToCenterDist + intersectedDist;
        if (hitDist < 0) return Optional.empty();

        Vector3d point = orig.plus(dir.times(hitDist));
        UnitVector3d normal = point.minus(center).normalize();
        return Optional.of(new SurfacePoint(this, point, normal, material));
    }

}
