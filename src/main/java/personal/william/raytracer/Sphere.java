package personal.william.raytracer;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;
import org.jscience.mathematics.vector.Vector;

import java.util.OptionalDouble;

public class Sphere implements VectorSpaceObject {

    private final Material material;
    private final Float64Vector center;
    private final float radius;

    public Sphere(Material material, Float64Vector center, float radius) {
        this.material = material;
        this.center = center;
        this.radius = radius;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public OptionalDouble calculateRayContactDistance(Vector<Float64> orig, Vector<Float64> dir) {
        Vector<Float64> ray = center.minus(orig);
        Float64 rayToCenterDist = dir.times(ray);
        Float64 centerDistSquare = ray.times(ray).minus(rayToCenterDist.times(rayToCenterDist));
        if (centerDistSquare.floatValue() > (radius * radius)) return OptionalDouble.empty();

        Float64 intersectedDist = Float64.valueOf(Math.sqrt((radius * radius) - centerDistSquare.floatValue()));
        Float64 contactDist0 = rayToCenterDist.minus(intersectedDist);
        Float64 contactDist1 = rayToCenterDist.plus(intersectedDist);
        if (contactDist0.compareTo(0) < 0) contactDist0 = contactDist1;
        return contactDist0.compareTo(0) >= 0 ? OptionalDouble.of(contactDist0.floatValue()) : OptionalDouble.empty();
    }

    @Override
    public Float64Vector normalize(Vector<Float64> point) {
        Float64Vector normal = Float64Vector.valueOf(point.minus(center));
        return normal.times(normal.norm().inverse());
    }
}
