package com.jeremie.chip8;

import java.util.Arrays;

public class Chip8 {
    private static final short fontset[] = {
            0xF0, 0x90, 0x90, 0x90, 0xF0,		// 0
            0x20, 0x60, 0x20, 0x20, 0x70,		// 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0,		// 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0,		// 3
            0x90, 0x90, 0xF0, 0x10, 0x10,		// 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0,		// 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0,		// 6
            0xF0, 0x10, 0x20, 0x40, 0x40,		// 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0,		// 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0,		// 9
            0xF0, 0x90, 0xF0, 0x90, 0x90,		// A
            0xE0, 0x90, 0xE0, 0x90, 0xE0,		// B
            0xF0, 0x80, 0x80, 0x80, 0xF0,		// C
            0xE0, 0x90, 0x90, 0x90, 0xE0,		// D
            0xF0, 0x80, 0xF0, 0x80, 0xF0,		// E
            0xF0, 0x80, 0xF0, 0x80, 0x80		// F
    };

    // Opcodes are stored big-endian
    private short opcode;
    private byte[] memory;
    private byte[] registers = new byte[16];
    private short addressRegister;
    private short[] stack = new short[16];
    private byte delayTimer;
    private byte soundTimer;
    private short programCounter;
    private byte stackPointer;
    private short indexPointer;
    private Monitor monitor;

    // Max sprite size: 8*15
    private byte[] sprite = new byte[15];


    public Chip8(Monitor monitor) {
        this.programCounter = 0x200;
        this.memory = new byte[4096];
        this.monitor = monitor;
        copyFontsetToMemory();
    }

    public void monitorTest(){
        monitor.drawSprite(0,0, Arrays.copyOfRange(fontset, 0, 5));
    }

    public void parseOpcode(short opcode) {
        switch(opcode) {

        }
    }

    public void decodeOpcode() {
        byte msb = memory[programCounter];
        byte lsb = memory[programCounter + 1];

        opcode = (short) (msb << 8 | lsb);
    }

    public void executeOpcode() {
        switch(opcode >> 12) {
            case 0x0:
                switch(opcode & 0xFF) {
                    case 0XE0:
                        log("Clear screen");
                        break;
                    case 0XEE:
                        break;
                }
                break;
            case 0x1:
                // Goto NNN
                break;
            case 0x2:
                // Call subroutine
                break;
            case 0x3:
                // Skip next instruction if
                break;
            case 0x4:
                // Skip next instruction if
                break;
            case 0x5:
                // Skip next instruction if
                break;
            case 0x6:
                // Vx = NN
                break;
            case 0x7:
                // Vx += NN
                // No carry
                break;
            case 0x8:
                switch(opcode & 0XF) {
                    case 0:
                        // Vx = Vy
                        break;
                    case 1:
                        // Vx = Vx | Vy
                        break;
                    case 2:
                        // Vx = Vx & Vy
                        break;
                    case 3:
                        // Vx = Vx ^ Vy
                        break;
                    case 4:
                        // Vx = Vx + Vy
                        // With carry flag
                        break;
                    case 5:
                        // Vx -= Vy
                        // With borrow flag
                        break;
                    case 6:
                        // See wiki
                        // Vx = Vy >> 1
                        break;
                    case 7:
                        // Vx = Vy - Vx
                        break;
                    case 0xE:
                        // See wiki
                        // Vx = Vy = Vy << 1
                        break;
                }
                break;
            case 0x9:
                // skip if (Vx!=Vy)
                break;
            case 0xA:
                // I = NNN
                break;
            case 0xB:
                // PC = V0 + NNN
                break;
            case 0xC:
                // See Wiki
                // Vx = rand() & NN
                break;
            case 0xD:
                // See wiki
                // Draw sprite
                break;
            case 0xE:
                switch(opcode & 0xFF) {
                    case 0x9E:
                        // Skip next instruction if key is pressed
                        break;
                    case 0xA1:
                        // Skip next instruction if key is not pressed
                        break;
                }
                break;
            case 0XF:
                switch(opcode & 0xFF) {
                    case 0x07:
                        // Vx = getDelay
                        break;
                    case 0x0A:
                        // See wiki
                        // Vx = getKey()
                        break;
                    case 0x15:
                        // Set delayTimer to Vx
                        break;
                    case 0x18:
                        // Set sound_timer to Vx
                        break;
                    case 0x1E:
                        // I += VX
                        break;
                    case 0x29:
                        // I = sprite_addr[Vx]
                        // Sets I to the location of the character in VX
                        break;
                    case 0x33:
                        // Stores BCD representation of VX
                        // See Wiki
                        break;
                    case 0x55:
                        // Store V0 to Vx at memory I
                        break;
                    case 0x65:
                        // Fill V0 to Vx from memory using I
                        break;
                }
                break;
        }
    }

    private void log(String input) {
        System.out.println("Opcode: " + input);
    }

    private void copyFontsetToMemory() {
        for(int i = 0; i < fontset.length; i++) {
            memory[i] = (byte) fontset[i];
        }
    }
}
