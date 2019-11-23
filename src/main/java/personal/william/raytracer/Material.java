package personal.william.raytracer;

import java.awt.Color;

public class Material {

    private final double specularAlbedo;
    private final double diffuseAlbedo;
    private final Color diffuseColor;
    private final double reflectionAlbedo;
    private final double specularExponent;
    private final double refractiveIndex;
    private final double refractiveAlbedo;

    public Material(
            double specularAlbedo,
            double diffuseAlbedo, Color diffuseColor,
            double reflectionAlbedo, double specularExponent,
            double refractiveIndex, double refractiveAlbedo) {
        this.specularAlbedo = specularAlbedo;
        this.diffuseAlbedo = diffuseAlbedo;
        this.diffuseColor = diffuseColor;
        this.reflectionAlbedo = reflectionAlbedo;
        this.specularExponent = specularExponent;
        this.refractiveIndex = refractiveIndex;
        this.refractiveAlbedo = refractiveAlbedo;
    }

    @Override
    public String toString() {
        return "Material{" +
                "specularAlbedo=" + specularAlbedo +
                ", diffuseAlbedo=" + diffuseAlbedo +
                ", diffuseColor=" + diffuseColor +
                ", reflectionAlbedo=" + reflectionAlbedo +
                ", specularExponent=" + specularExponent +
                ", refractiveIndex=" + refractiveIndex +
                ", refractiveAlbedo=" + refractiveAlbedo +
                '}';
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

    public double getRefractiveIndex() {
        return refractiveIndex;
    }

    public double getRefractiveAlbedo() {
        return refractiveAlbedo;
    }
}
