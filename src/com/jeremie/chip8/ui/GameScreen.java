package com.jeremie.chip8.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class GameScreen extends JPanel implements ActionListener {
    private static final int scale = 16;
    private static final Dimension DIMENSION = new Dimension(64 * scale, 32 * scale);
    private BufferedImage canvas;
    private int[] whitePixel = new int[scale*scale];
    private int[] blackPixel = new int[scale*scale];

    public GameScreen() {
        Arrays.fill(whitePixel, Color.WHITE.getRGB());
        Arrays.fill(blackPixel, Color.BLACK.getRGB());
        this.setPreferredSize(DIMENSION);
        this.canvas = new BufferedImage(DIMENSION.width, DIMENSION.height, BufferedImage.TYPE_INT_ARGB);
        this.setBackground(Color.BLACK);
        setFocusable(true);
    }

    public void setKeyAdapter(KeyAdapter keyAdapter) {
        addKeyListener(keyAdapter);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);
    }

    public void drawScreen(byte[] screen) {
        for(int i = 0; i < 32; i++) {
            for(int j = 0; j < 64; j++) {
                byte pixel = screen[j + 64*i];
                int[] pixelColor = (pixel == 1) ? whitePixel: blackPixel;

                int xScaled = (j*scale);
                int yScaled = (i*scale);
                canvas.setRGB(xScaled, yScaled, scale, scale, pixelColor, 0, 0);
            }
        }
        repaint();
    }
}
