package personal.william.raytracer;

import java.util.Optional;

public class Sphere implements SceneObject {

    private final Material material;
    private final double radius;

    public Sphere(Material material, double radius) {
        this.material = material;
        this.radius = radius;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public Optional<SurfacePoint> cast(
            Vector3d positioning, Vector3d orig, Vector3d dir) {
        Vector3d ray = positioning.minus(orig);
        double rayToCenterDist = dir.dot(ray);
        double centerDistSquare = ray.dot(ray) - (rayToCenterDist * rayToCenterDist);
        double radiusSquare = radius * radius;
        if (centerDistSquare > (radiusSquare)) return Optional.empty();

        float intersectedDist = (float) Math.sqrt(radiusSquare - centerDistSquare);
        double hitDist = rayToCenterDist - intersectedDist;
        if (hitDist < 0) hitDist = rayToCenterDist + intersectedDist;
        if (hitDist < 0) return Optional.empty();

        Vector3d point = orig.plus(dir.times(hitDist));
        Vector3d normal = point.minus(positioning).normalize();
        return Optional.of(new SurfacePoint(this, point, normal, material));
    }

}
