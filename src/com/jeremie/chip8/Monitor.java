package com.jeremie.chip8;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Monitor extends JPanel {
    private BufferedImage canvas;
    private static final int scale = 16;
    private int[] pixel = new int[scale*scale];
    private int[] unsetPixel = new int[scale*scale];
    private static final Dimension dimension = new Dimension(64 * scale, 32 * scale);

    public Monitor() {
        Arrays.fill(pixel, Color.WHITE.getRGB());
        this.setPreferredSize(this.dimension);
        canvas = new BufferedImage(this.dimension.width, this.dimension.height, BufferedImage.TYPE_INT_ARGB);
        this.setBackground(Color.BLACK);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);
    }

    public boolean setPixel(int x, int y) {
        // Returns true if a pixel was already set at this position
        // If one pixel was white, then all of them were in the upscaled one
        int[] rgb = new int[16];

        canvas.getRGB(x*scale, y*scale, scale, scale, rgb, 0, 0);
        if(rgb[0] == -1) {
            unsetPixel(x,y);
        }else{
            canvas.setRGB(x*scale, y*scale, scale, scale, pixel, 0, 0);
        }

        return rgb[0] == -1;
    }

    public void unsetPixel(int x, int y) {
        canvas.setRGB(x*scale, y*scale, scale, scale, unsetPixel, 0, 0);
    }

    public boolean drawSprite(int x, int y, short[] sprite) {
        boolean collision = false;
        for(int i = 0; i < sprite.length; i++) {
            short line = sprite[i];
            for (int j = 0; j < 8; j++) {
                int bit = (line >> (7 - j)) & 1;
                if (bit == 1) {
                    collision = collision || setPixel(j + x, i + y);
                }
            }
        }
        return collision;
    }

    public void clearScreen() {
        for(int i = 0; i < getHeight(); i++) {
            for(int j = 0; j < getWidth(); j++) {
                canvas.setRGB(i, j, Color.BLACK.getRGB());
            }
        }
    }
}
