package edu.eci.arsw.relicrush.gui;

import java.awt.Font;
import java.io.InputStream;

public class FontLoader {


    private static Font baseFont;

    public static Font getCustomFont(float size) {
        if (baseFont == null) {
            try {

                InputStream is = FontLoader.class.getResourceAsStream("/Minecraft.ttf");
                if (is != null) {
                    baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
                } else {
                    System.err.println(">>> ADVERTENCIA: No se encontró rpg_font.ttf");
                    baseFont = new Font("Monospaced", Font.BOLD, 12);
                }
            } catch (Exception e) {
                System.err.println(">>> ERROR al cargar la fuente: " + e.getMessage());
                baseFont = new Font("Monospaced", Font.BOLD, 12);
            }
        }

        return baseFont.deriveFont(size);
    }
}
