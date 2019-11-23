package personal.william.raytracer;

import org.jscience.mathematics.vector.Float64Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;

public class RayTracer {

    public static void main(String[] args) {
        final Float64Vector cameraPos = Float64Vector.valueOf(0f, 0f, 0f);
        final Float64Vector cameraFacing = Float64Vector.valueOf(0f, 0f, -1f);
        final Float64Vector cameraUpside = Float64Vector.valueOf(0f, 1f, 0f);
        final float fov = (float) Math.PI / 2;

        final int width = 1024;
        final int height = 768;

        Color bgColor = new Color(0.2f, 0.7f, 0.8f);
        Material ivory = new Material(0.3f, 0.6f, new Color(0.4f, 0.4f, 0.3f), 50f);
        Material redRubber = new Material(0.1f, 0.9f, new Color(0.3f, 0.1f, 0.1f), 10f);

        VectorSpaceScene scene = new Scene3d();

        scene.setBackgroundColor(bgColor);

        scene.putLight(new Light(1.5f), Float64Vector.valueOf(-20f, 20f, 20f));
        scene.putLight(new Light(1.8f), Float64Vector.valueOf(30f, 50f, -25f));
        scene.putLight(new Light(1.7f), Float64Vector.valueOf(30f, 20f, 30f));

        scene.putObject(new Sphere(ivory, 2f), Float64Vector.valueOf(-3f, 0f, -16f));
        scene.putObject(new Sphere(redRubber, 2f), Float64Vector.valueOf(-1f, -1.5f, -12f));
        scene.putObject(new Sphere(redRubber, 3f), Float64Vector.valueOf(1.5f, -0.5f, -18f));
        scene.putObject(new Sphere(ivory, 4f), Float64Vector.valueOf(7f, 5f, -18f));

        Camera camera = scene.setupCamera(cameraPos, cameraFacing, cameraUpside, fov);

        Image image = camera.renderAsImage(width, height);

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
