package personal.william.raytracer;

import java.awt.Image;

public interface Camera {

    VectorSpaceScene getScene();

    Image renderAsImage(int width, int height);
}
