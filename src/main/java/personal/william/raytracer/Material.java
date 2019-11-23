package personal.william.raytracer;

import java.awt.Color;

public class Material {

    private final double specularAlbedo;
    private final double diffuseAlbedo;
    private final Color diffuseColor;
    private final double reflectionAlbedo;
    private final double specularExponent;

    public Material(
            double specularAlbedo,
            double diffuseAlbedo, Color diffuseColor,
            double reflectionAlbedo, double specularExponent) {
        this.specularAlbedo = specularAlbedo;
        this.diffuseAlbedo = diffuseAlbedo;
        this.diffuseColor = diffuseColor;
        this.reflectionAlbedo = reflectionAlbedo;
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

    public double getReflectionAlbedo() {
        return reflectionAlbedo;
    }

    public double getSpecularExponent() {
        return specularExponent;
    }
}
