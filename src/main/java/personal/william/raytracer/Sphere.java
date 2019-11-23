package personal.william.raytracer;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;
import org.jscience.mathematics.vector.Vector;

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
    public Optional<Float64Vector> getFirstIntersection(
            Float64Vector positioning, Float64Vector orig, Float64Vector dir) {
        Vector<Float64> ray = positioning.minus(orig);
        Float64 rayToCenterDist = dir.times(ray);
        Float64 centerDistSquare = ray.times(ray).minus(rayToCenterDist.times(rayToCenterDist));
        if (centerDistSquare.floatValue() > (radius * radius)) return Optional.empty();

        Float64 intersectedDist = Float64.valueOf(Math.sqrt((radius * radius) - centerDistSquare.floatValue()));
        Float64 contactDist0 = rayToCenterDist.minus(intersectedDist);
        if (contactDist0.compareTo(0) < 0) contactDist0 = rayToCenterDist.plus(intersectedDist);
        return contactDist0.compareTo(0) >= 0
                ? Optional.of(dir.times(contactDist0.floatValue())) : Optional.empty();
    }

    @Override
    public Float64Vector getNormalVector(Float64Vector positioning, Float64Vector point) {
        return VectorUtils.normalize(Float64Vector.valueOf(point.minus(positioning)));
    }
}
