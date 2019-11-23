package personal.william.raytracer;

import org.jscience.mathematics.vector.Float64Vector;

public class Light {

    private final Float64Vector position;
    private final float intensity;

    public Light(Float64Vector position, float intensity) {
        this.position = position;
        this.intensity = intensity;
    }

    public Float64Vector getPosition() {
        return position;
    }

    public float getIntensity() {
        return intensity;
    }
}
