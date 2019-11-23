package personal.william.raytracer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RayTracer {

    public static void main(String[] args) {
        final int width = 1024;
        final int height = 768;
        final Color[][] frameBuffer = new Color[width][height];

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                frameBuffer[i][j] = new Color(((float) j) / height, ((float) i) / width, 0);
            }
        }

        displayImage(frameBuffer);
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
