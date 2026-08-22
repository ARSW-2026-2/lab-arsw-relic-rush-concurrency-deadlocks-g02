package edu.eci.arsw.relicrush.gui;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        try {
            java.net.URL imgURL = getClass().getResource(imagePath);

            if (imgURL != null) {
                System.out.println(">>> ¡ÉXITO! Imagen encontrada en: " + imgURL);
                backgroundImage = ImageIO.read(imgURL);
            } else {
                System.err.println(">>> ERROR FATAL: Java devolvió null. No se encontró: " + imagePath);
                System.err.println(">>> Revisa que el archivo esté en target/classes/");
            }
        } catch (Exception e) {
            System.err.println(">>> ERROR al leer la imagen: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
