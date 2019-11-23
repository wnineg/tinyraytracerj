package personal.william.raytracer;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalDouble;

public class RayTracer {

    public static void main(String[] args) {
        final int width = 1024;
        final int height = 768;
        final float fov = (float) Math.PI / 2;
        final Color[][] frameBuffer = new Color[width][height];

        Float64Vector light = Float64Vector.valueOf(0f, 0f, 0f);
        Color bgColor = new Color(0.2f, 0.7f, 0.8f);
        Material ivory = new Material(0.3f, 0.6f, new Color(0.4f, 0.4f, 0.3f), 50f);
        Material redRubber = new Material(0.1f, 0.9f, new Color(0.3f, 0.1f, 0.1f), 10f);

        Collection<Light> lights = new ArrayList<>(3);
        lights.add(new Light(Float64Vector.valueOf(-20f, 20f, 20f), 1.5f));
        lights.add(new Light(Float64Vector.valueOf(30f, 50f, -25f), 1.8f));
        lights.add(new Light(Float64Vector.valueOf(30f, 20f, 30f), 1.7f));

        Collection<VectorSpaceObject> objects = new ArrayList<>(4);
        objects.add(new Sphere(ivory, Float64Vector.valueOf(-3f, 0f, -16f), 2));
        objects.add(new Sphere(redRubber, Float64Vector.valueOf(-1f, -1.5f, -12f), 2));
        objects.add(new Sphere(redRubber, Float64Vector.valueOf(1.5f, -0.5f, -18f), 3));
        objects.add(new Sphere(ivory, Float64Vector.valueOf(7f, 5f, -18f), 4));

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                float x = (float) ((2 * (i + 0.5) / (float) width - 1) * Math.tan(fov / 2.0) * width / (float) height);
                float y = (float) -((2 * (j + 0.5) / (float) height - 1) * Math.tan(fov / 2.0));
                Float64Vector dir = Float64Vector.valueOf(x, y, -1);
                dir = dir.times(dir.norm().inverse());
                frameBuffer[i][j] = castRay(light, dir, lights, objects).orElse(bgColor);
            }
        }

        displayImage(frameBuffer);
    }

    private static Optional<Color> castRay(
            Float64Vector orig, Float64Vector dir,
            Collection<Light> lights, Collection<VectorSpaceObject> objects) {
        Optional<RayHit> optHit = intersectScene(orig, dir, objects);
        if (! optHit.isPresent()) return Optional.empty();

        RayHit hit = optHit.get();
        float diffuseLightIntensity = 0;
        float specularLightIntensity = 0;
        for (Light light : lights) {
            Float64Vector lightDir = light.getPosition().minus(hit.getPoint());
            lightDir = lightDir.times(lightDir.norm().inverse());
            diffuseLightIntensity += light.getIntensity() * Math.max(0f, lightDir.times(hit.getNormal()).floatValue());
            specularLightIntensity +=
                    calculateSpecularIntensity(
                            dir.opposite(), lightDir.opposite(), hit.getNormal(), light, hit.getMaterial());
        }
        float specular = specularLightIntensity * hit.getMaterial().getSpecularAlbedo();
        float[] rgb = hit.getMaterial().getDiffuseColor().getRGBColorComponents(null);
        float r = rgb[0] * diffuseLightIntensity * hit.getMaterial().getDiffuseAlbedo() + specular;
        float g = rgb[1] * diffuseLightIntensity * hit.getMaterial().getDiffuseAlbedo() + specular;
        float b = rgb[2] * diffuseLightIntensity * hit.getMaterial().getDiffuseAlbedo() + specular;
        float max = Math.max(r, Math.max(g, b));
        if (max > 1) {
            r = r / max;
            g = g / max;
            b = b / max;
        }
        return Optional.of(new Color(r, g, b));
    }

    private static class RayHit {

        private final Float64Vector point;
        private final Float64Vector normal;
        private final Material material;

        public RayHit(Float64Vector point, Float64Vector normal, Material material) {
            this.point = point;
            this.normal = normal;
            this.material = material;
        }

        public Float64Vector getPoint() {
            return point;
        }

        public Float64Vector getNormal() {
            return normal;
        }

        public Material getMaterial() {
            return material;
        }
    }

    private static Optional<RayHit> intersectScene(
            Float64Vector orig, Float64Vector dir, Collection<VectorSpaceObject> objects) {
        float shortestDist = Float.MAX_VALUE;
        Float64Vector hit = null;
        Float64Vector normal = null;
        Material material = null;
        for (VectorSpaceObject object : objects) {
            OptionalDouble contactDist = object.calculateRayContactDistance(orig, dir);
            if (! contactDist.isPresent()) continue;

            double dist = contactDist.getAsDouble();
            if (dist >= shortestDist) continue;

            shortestDist = (float) dist;
            hit = orig.plus(dir.times(Float64.valueOf(shortestDist)));
            normal = object.normalize(hit);
            material = object.getMaterial();
        }

        // Ignore the rays which go too far away
        return shortestDist < 1000 ? Optional.of(new RayHit(hit, normal, material)) : Optional.empty();
    }

    private static float calculateSpecularIntensity(
            Float64Vector orig, Float64Vector light, Float64Vector normal, Light lightSource, Material material) {
        Float64Vector reflect = light.minus(normal.times(2.0).times(light.times(normal)));
        float reflectIntensity = reflect.times(orig).floatValue();
        if (reflectIntensity <= 0) return 0f;
        return (float) (lightSource.getIntensity() * Math.pow(reflectIntensity, material.getSpecularExponent()));
    }

    private static void displayImage(Color[][] rgbBuffer) {
        final int width = rgbBuffer.length;
        final int height = rgbBuffer[0].length;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                image.setRGB(x, y, rgbBuffer[x][y].getRGB());
            }
        }

        ImageIcon icon = new ImageIcon(image);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(width, height);

        JLabel label = new JLabel();
        label.setIcon(icon);
        frame.add(label);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
