package personal.william.raytracer;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;
import org.jscience.mathematics.vector.Vector;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
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
        Sphere sphere = new Sphere(new Material(new Color(0.4f, 0.4f, 0.3f)), Float64Vector.valueOf(-3f, 0f, -16f), 2);

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                float x = (float) ((2 * (i + 0.5) / (float) width - 1) * Math.tan(fov / 2.0) * width / (float) height);
                float y = (float) -((2 * (j + 0.5) / (float) height - 1) * Math.tan(fov / 2.0));
                Float64Vector dir = Float64Vector.valueOf(x, y, -1);
                dir = dir.times(dir.norm().inverse());
                frameBuffer[i][j] = castRay(light, dir, sphere).orElse(bgColor);
            }
        }

        displayImage(frameBuffer);
    }

    private static Optional<Color> castRay(Vector<Float64> orig, Vector<Float64> dir, VectorSpaceObject object) {
        OptionalDouble contactDist = object.calculateRayContactDistance(orig, dir);
        return contactDist.isPresent() ? Optional.of(object.getMaterial().getDiffuseColor()) : Optional.empty();
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
