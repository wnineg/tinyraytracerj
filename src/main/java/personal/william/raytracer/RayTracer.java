package personal.william.raytracer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;

public class RayTracer {

    public static void main(String[] args) {
        final Vector3d cameraPos = new Vector3d(0f, 0f, 0f);
        final Vector3d cameraFacing = new Vector3d(0f, 0f, -1f);
        final Vector3d cameraUpside = new Vector3d(0f, 1f, 0f);
        final float fov = (float) Math.PI / 2;

        final int width = 1024;
        final int height = 768;

        Color bgColor = new Color(0.2f, 0.7f, 0.8f);
        Material ivory = new Material(0.3f, 0.6f, new Color(0.4f, 0.4f, 0.3f), 50f);
        Material redRubber = new Material(0.1f, 0.9f, new Color(0.3f, 0.1f, 0.1f), 10f);

        Vector3dSpaceScene scene = new Scene3d();

        scene.setBackgroundColor(bgColor);

        scene.putLight(new Light(1.5f), new Vector3d(-20f, 20f, 20f));
        scene.putLight(new Light(1.8f), new Vector3d(30f, 50f, -25f));
        scene.putLight(new Light(1.7f), new Vector3d(30f, 20f, 30f));

        scene.putObject(new Sphere(ivory, 2f), new Vector3d(-3f, 0f, -16f));
        scene.putObject(new Sphere(redRubber, 2f), new Vector3d(-1f, -1.5f, -12f));
        scene.putObject(new Sphere(redRubber, 3f), new Vector3d(1.5f, -0.5f, -18f));
        scene.putObject(new Sphere(ivory, 4f), new Vector3d(7f, 5f, -18f));

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
