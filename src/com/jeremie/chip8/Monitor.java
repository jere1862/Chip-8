package com.jeremie.chip8;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.image.BufferedImage;
import java.security.Key;
import java.util.Arrays;

public class Monitor extends JPanel implements ActionListener {
    private static final int scale = 16;
    private static final Dimension DIMENSION = new Dimension(64 * scale, 32 * scale);
    private BufferedImage canvas;
    private int[] pixel = new int[scale*scale];
    private int[] unsetPixel = new int[scale*scale];
    private KeyAdapter keyAdapter;

    public Monitor() {
        Arrays.fill(pixel, Color.WHITE.getRGB());
        this.setPreferredSize(DIMENSION);
        this.canvas = new BufferedImage(DIMENSION.width, DIMENSION.height, BufferedImage.TYPE_INT_ARGB);
        this.setBackground(Color.BLACK);
        setFocusable(true);
    }

    public void setKeyAdapter(KeyAdapter keyAdapter) {
        this.keyAdapter = keyAdapter;
        addKeyListener(keyAdapter);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //repaint();
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
        int xScaled = (x*scale) % DIMENSION.width;
        int yScaled = (y*scale) % DIMENSION.height;

        canvas.getRGB(xScaled, yScaled, scale, scale, rgb, 0, 0);
        if(rgb[0] == -1) {
            unsetPixel(xScaled,yScaled);
        }else{
            canvas.setRGB(xScaled, yScaled, scale, scale, pixel, 0, 0);
        }
        return rgb[0] == -1;
    }

    public void unsetPixel(int x, int y) {
        canvas.setRGB(x, y, scale, scale, unsetPixel, 0, 0);
    }

    public boolean drawSprite(int x, int y, byte[] sprite) {
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

        repaint();
        return collision;
    }

    public void clearScreen() {
        for(int i = 0; i < 64; i++) {
            for(int j = 0; j < 32; j++) {
                unsetPixel(i, j);
            }
        }
    }
}
