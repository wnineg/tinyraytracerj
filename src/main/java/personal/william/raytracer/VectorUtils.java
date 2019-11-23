package personal.william.raytracer;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;

public final class VectorUtils {

    public static Float64Vector normalize(Float64Vector vector) {
        Float64 norm = vector.norm();
        return norm.equals(1.0) ? vector : vector.times(norm.inverse());
    }
}
