package personal.william.raytracer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;

public class RayTracer {

    public static void main(String[] args) {
        final Vector3d cameraPos = Vector3d.of(0, 0, 0);
        final UnitVector3d cameraFacing = UnitVector3d.normalize(0, 0, -1);
        final UnitVector3d cameraUpside = UnitVector3d.normalize(0, 1, 0);
        final double fov = Math.PI / 2;

        final int width = 1024;
        final int height = 768;

        Color bgColor = new Color(0.2f, 0.7f, 0.8f);
        Material ivory = new Material(0.3, 0.6, new Color(0.4f, 0.4f, 0.3f), 0.1, 50, 1, 0);
        Material redRubber = new Material(0.1, 0.9, new Color(0.3f, 0.1f, 0.1f), 0, 10, 1, 0);
        Material mirror = new Material(10, 0, new Color(1.0f, 1.0f, 1.0f), 0.8, 1425, 1, 0);
        Material glass = new Material(0.5, 0, new Color(0.6f, 0.7f, 0.8f), 0.1, 125, 1.5, 0.8);

        Vector3dSpaceScene scene = new Scene3d();

        scene.setBackgroundColor(bgColor);

        scene.putLight(new Light(1.5), Vector3d.of(-20, 20, 20));
        scene.putLight(new Light(1.8), Vector3d.of(30, 50, -25));
        scene.putLight(new Light(1.7), Vector3d.of(30, 20, 30));

        scene.putObject(new Sphere(ivory, 2), Vector3d.of(-3, 0, -16));
        scene.putObject(new Sphere(glass, 2), Vector3d.of(-1f, -1.5f, -12f));
        scene.putObject(new Sphere(redRubber, 3), Vector3d.of(1.5, -0.5, -18));
        scene.putObject(new Sphere(mirror, 4), Vector3d.of(7, 5, -18));

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
