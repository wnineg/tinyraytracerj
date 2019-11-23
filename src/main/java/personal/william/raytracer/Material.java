package personal.william.raytracer;

import java.awt.Color;

public class Material {

    private final float specularAlbedo;
    private final float diffuseAlbedo;
    private final Color diffuseColor;
    private final float specularExponent;

    public Material(float specularAlbedo, float diffuseAlbedo, Color diffuseColor, float specularExponent) {
        this.specularAlbedo = specularAlbedo;
        this.diffuseAlbedo = diffuseAlbedo;
        this.diffuseColor = diffuseColor;
        this.specularExponent = specularExponent;
    }

    public float getSpecularAlbedo() {
        return specularAlbedo;
    }

    public float getDiffuseAlbedo() {
        return diffuseAlbedo;
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public float getSpecularExponent() {
        return specularExponent;
    }
}
