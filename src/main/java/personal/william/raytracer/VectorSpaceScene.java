package personal.william.raytracer;

import org.jscience.mathematics.vector.Float64Vector;

import java.awt.Color;

public interface VectorSpaceScene {

    Camera setupCamera(
            Float64Vector position, Float64Vector faceDirection, Float64Vector upDirection, float fieldOfView);

    void setBackgroundColor(Color color);

    void putLight(Light light, Float64Vector position);

    void putObject(SceneObject object, Float64Vector position);
}
