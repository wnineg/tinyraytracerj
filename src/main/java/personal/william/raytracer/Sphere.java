package personal.william.raytracer;

import java.util.Optional;

public class Sphere implements SceneObject {

    private final Material material;
    private final float radius;

    public Sphere(Material material, float radius) {
        this.material = material;
        this.radius = radius;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public Optional<Vector3d> getFirstIntersection(
            Vector3d positioning, Vector3d orig, Vector3d dir) {
        Vector3d ray = positioning.minus(orig);
        float rayToCenterDist = dir.dot(ray);
        float centerDistSquare = ray.dot(ray)- (rayToCenterDist * rayToCenterDist);
        float radiusSquare = radius * radius;
        if (centerDistSquare > (radiusSquare)) return Optional.empty();

        float intersectedDist = (float) Math.sqrt(radiusSquare - centerDistSquare);
        float contactDist0 = rayToCenterDist - intersectedDist;
        if (contactDist0 < 0) contactDist0 = rayToCenterDist + intersectedDist;
        return contactDist0 >= 0 ? Optional.of(dir.times(contactDist0)) : Optional.empty();
    }

    @Override
    public Vector3d getNormalVector(Vector3d positioning, Vector3d point) {
        return point.minus(positioning).normalize();
    }
}
