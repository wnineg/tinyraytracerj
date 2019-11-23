package personal.william.raytracer;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;
import org.jscience.mathematics.vector.Vector;

import java.util.OptionalDouble;

public interface VectorSpaceObject {

    Material getMaterial();

    OptionalDouble calculateRayContactDistance(Vector<Float64> orig, Vector<Float64> dir);

    Float64Vector normalize(Vector<Float64> point);
}
