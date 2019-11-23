package personal.william.raytracer;

import java.awt.*;

public class Material {

    private final Color diffuseColor;

    public Material(Color diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }
}
