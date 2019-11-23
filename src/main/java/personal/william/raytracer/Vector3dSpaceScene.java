package personal.william.raytracer;

import java.awt.Color;

public interface Vector3dSpaceScene {

    Camera setupCamera(Vector3d position, UnitVector3d faceDirection, UnitVector3d upDirection, double fieldOfView);

    void setBackgroundColor(Color color);

    void setRefractiveIndex(float index);

    void putLight(Light light, Vector3d position);

    void putObject(SceneObject object, Vector3d position);
}
