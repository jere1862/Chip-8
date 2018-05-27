package com.jeremie.chip8;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Monitor monitor = new Monitor();
        initializeGameWindow(monitor);
        Chip8 chip8 = new Chip8(monitor);
        chip8.monitorTest();

    }

    private static void initializeGameWindow(Monitor monitor) {
        JFrame frame = new JFrame("Chip-8");
        frame.setLayout(new BorderLayout());
        frame.add(monitor, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);;
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
    }
}
