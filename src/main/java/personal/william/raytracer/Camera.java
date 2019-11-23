package personal.william.raytracer;

import java.awt.Image;

public interface Camera {

    Vector3dSpaceScene getScene();

    Image renderAsImage(int width, int height);
}
