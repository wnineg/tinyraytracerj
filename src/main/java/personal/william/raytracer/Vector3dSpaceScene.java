package personal.william.raytracer;

import java.awt.Color;

public interface Vector3dSpaceScene {

    Camera setupCamera(Vector3d position, Vector3d faceDirection, Vector3d upDirection, float fieldOfView);

    void setBackgroundColor(Color color);

    void putLight(Light light, Vector3d position);

    void putObject(SceneObject object, Vector3d position);
}
