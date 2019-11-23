package personal.william.raytracer;

import java.awt.Color;

public class Material {

    private final double specularAlbedo;
    private final double diffuseAlbedo;
    private final Color diffuseColor;
    private final double specularExponent;

    public Material(double specularAlbedo, double diffuseAlbedo, Color diffuseColor, double specularExponent) {
        this.specularAlbedo = specularAlbedo;
        this.diffuseAlbedo = diffuseAlbedo;
        this.diffuseColor = diffuseColor;
        this.specularExponent = specularExponent;
    }

    public double getSpecularAlbedo() {
        return specularAlbedo;
    }

    public double getDiffuseAlbedo() {
        return diffuseAlbedo;
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public double getSpecularExponent() {
        return specularExponent;
    }
}
