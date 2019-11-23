package personal.william.raytracer;

import org.jscience.mathematics.vector.Float64Vector;

import java.util.Optional;

public interface SceneObject {

    Material getMaterial();

    Optional<Float64Vector> getFirstIntersection(Float64Vector positioning, Float64Vector orig, Float64Vector dir);

    Float64Vector getNormalVector(Float64Vector positioning, Float64Vector point);
}
