package com.jeremie.chip8;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Runner extends JFrame {
    private Monitor monitor;
    private Chip8 chip8;
    private RomReader romReader;
    private Timer clock;
    private ActionListener actionListener;

    public Runner() {
        super("Chip-8");
        monitor = new Monitor();
        chip8 = new Chip8(monitor);
        monitor.setKeyAdapter(new GameAdapter(this.chip8));
        romReader = new RomReader();

        this.actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chip8.cycle();
            }
        };

        clock = new Timer(6, actionListener);
        clock.setRepeats(true);
        initializeFrame();
    }


    public void start(String gameName) {
        byte[] rom = romReader.read(gameName);
        chip8.loadRom(rom);
        clock.start();
    }

    private void initializeFrame() {
        setLayout(new BorderLayout());
        add(monitor, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
    }


    private class GameAdapter extends KeyAdapter {
        private Chip8 chip8;

        GameAdapter(Chip8 chip8) {
            this.chip8 = chip8;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            this.chip8.setKey(mapKey(e));
        }

        @Override
        public void keyPressed(KeyEvent e) {
            this.chip8.unsetKey(mapKey(e));
        }
    }

    private int mapKey(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_1:
                return 1;
            case KeyEvent.VK_2:
                return 2;
            case KeyEvent.VK_3:
                return 3;
            case KeyEvent.VK_4:
                return 0xC;
            case KeyEvent.VK_Q:
                return 4;
            case KeyEvent.VK_W:
                return 5;
            case KeyEvent.VK_E:
                return 6;
            case KeyEvent.VK_R:
                return 0xD;
            case KeyEvent.VK_A:
                return 7;
            case KeyEvent.VK_S:
                return 8;
            case KeyEvent.VK_D:
                return 9;
            case KeyEvent.VK_F:
                return 0xE;
            case KeyEvent.VK_Z:
                return 0xA;
            case KeyEvent.VK_X:
                return 0;
            case KeyEvent.VK_C:
                return 0xB;
            case KeyEvent.VK_V:
                return 0xF;
            default:
                return -1;
        }
    }
}
