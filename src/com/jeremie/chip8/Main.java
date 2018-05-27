package com.jeremie.chip8;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Monitor monitor = new Monitor();
        initializeGameWindow(monitor);
        Chip8 chip8 = new Chip8(monitor);
        Scanner scanner = new Scanner(System.in);

        RomReader rr = new RomReader();
        byte[] rom = rr.read("Testrom");
        chip8.loadRom(rom);


    }

    private int keyPressed(KeyEvent e) {
       int key = e.getKeyCode();
       switch(key) {
           case KeyEvent.VK_1:
               return 1;
               break;
           case KeyEvent.VK_2:
               return 2;
               break;
           case KeyEvent.VK_3:
               return 3;
               break;
               case KeyEvent.VK_4;
       }
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
