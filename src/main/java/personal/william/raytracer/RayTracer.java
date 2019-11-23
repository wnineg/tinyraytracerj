package personal.william.raytracer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;

public class RayTracer {

    public static void main(String[] args) {
        final Vector3d cameraPos = new Vector3d(0, 0, 0);
        final Vector3d cameraFacing = new Vector3d(0, 0, -1);
        final Vector3d cameraUpside = new Vector3d(0, 1, 0);
        final double fov = Math.PI / 2;

        final int width = 1024;
        final int height = 768;

        Color bgColor = new Color(0.2f, 0.7f, 0.8f);
        Material ivory = new Material(0.3, 0.6, new Color(0.4f, 0.4f, 0.3f), 50);
        Material redRubber = new Material(0.1, 0.9, new Color(0.3f, 0.1f, 0.1f), 10);

        Vector3dSpaceScene scene = new Scene3d();

        scene.setBackgroundColor(bgColor);

        scene.putLight(new Light(1.5), new Vector3d(-20, 20, 20));
        scene.putLight(new Light(1.8), new Vector3d(30, 50, -25));
        scene.putLight(new Light(1.7), new Vector3d(30, 20, 30));

        scene.putObject(new Sphere(ivory, 2), new Vector3d(-3, 0, -16));
        scene.putObject(new Sphere(redRubber, 2), new Vector3d(-1, -1.5, -12));
        scene.putObject(new Sphere(redRubber, 3), new Vector3d(1.5, -0.5, -18));
        scene.putObject(new Sphere(ivory, 4), new Vector3d(7, 5, -18));

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
