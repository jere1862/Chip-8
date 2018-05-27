package com.jeremie.chip8;

import java.util.Arrays;

public class Chip8 {
    private static final int fontSet[] = {
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
    private int opcode;
    private byte[] memory;
    private int[] registers = new int[16];
    private int[] stack = new int[16];
    private boolean[] keyboard = new boolean[16];
    private int delayTimer;
    private int soundTimer;
    private int programCounter;
    private int stackPointer = 0;
    private int indexPointer;
    private Monitor monitor;

    public Chip8(Monitor monitor) {
        this.programCounter = 0x200;
        this.memory = new byte[4096];
        this.monitor = monitor;
        copyFontSetToMemory();
    }

    public void loadRom(byte[] rom) {
        System.arraycopy(rom, 0, memory, 0x200, rom.length);
    }


    public void decodeOpcode() {
        int msb = memory[programCounter] & 0xFF;
        int lsb = memory[programCounter + 1] & 0xFF;

        opcode = msb << 8 | lsb;
    }

    public void setKey(int key) {
        keyboard[key] = true;
    }

    public void unsetKey(int key) {
        keyboard[key] = false;
    }

    private void clearScreen() {
        monitor.clearScreen();
    }

    public void executeOpcode() {
        switch(opcode >> 12) {
            case 0x0:
                switch(opcode & 0xFF) {
                    case 0XE0:
                        clearScreen();
                        programCounter += 2;
                        break;
                    case 0XEE:
                        programCounter = stackPop();
                        break;
                }
                break;
            case 0x1:
                // Goto NNN
                {
                    int address = opcode & 0xFFF;
                    programCounter = address;
                }
                break;
            case 0x2:
                // Call subroutine
                // Goto NNN
                {
                    int address = opcode & 0xFFF;
                    // Put current address in the stack
                    stackPush(programCounter + 2);
                    programCounter = address;
                }
                break;
            case 0x3:
                // Skip next instruction if Vx == kk
                {
                    int registerValue = registers[opcode & 0xF00];
                    if (registerValue == (opcode & 0xFF)) {
                        programCounter += 2;
                    }
                    programCounter += 2;
                }
                break;
            case 0x4:
                // Skip next instruction if Vx != kk
                {
                    int registerValue = registers[opcode & 0xF00];
                    if (registerValue != (opcode & 0xFF)) {
                        programCounter += 2;
                    }
                    programCounter += 2;
                }
                break;
            case 0x5:
                // Skip next instruction if Vx == Vy
                if(registers[opcode & 0xF00] == registers[opcode & 0xF0]){
                    programCounter += 2;
                }
                programCounter += 2;
                break;
            case 0x6:
                // Vx = NN
                registers[opcode & 0xF00] = (opcode & 0xFF);
                programCounter += 2;
                break;
            case 0x7:
                // Vx += NN, no carry
                registers[opcode & 0xF00] += (opcode & 0xFF);
                programCounter += 2;
                break;
            case 0x8:
                switch(opcode & 0xF) {

                    case 0:
                        // Vx = Vy
                        registers[opcode & 0xF00] = registers[opcode & 0xF0];
                        programCounter += 2;
                        break;
                    case 1:
                        // Vx = Vx | Vy
                        registers[opcode & 0xF00] |= registers[opcode & 0xF0];
                        programCounter += 2;
                        break;
                    case 2:
                        // Vx = Vx & Vy
                        registers[opcode & 0xF00] &= registers[opcode & 0xF0];
                        programCounter += 2;
                        break;
                    case 3:
                        // Vx = Vx ^ Vy
                        registers[opcode & 0xF00] ^= registers[opcode & 0xF0];
                        programCounter += 2;
                        break;
                    case 4:
                        // Vx = Vx + Vy
                        {
                            int result = registers[opcode & 0xF00] + registers[opcode & 0xF0];
                            if ((result & 0xF00) != 0) {
                                registers[15] = result & 0x100;
                            }
                            registers[opcode & 0xF00] = result & 0xFF;
                        }
                        programCounter += 2;
                        break;
                    case 5:
                        // Vx -= Vy
                        if(registers[opcode & 0xF00] > registers[opcode & 0xF0]) {
                            registers[15] = 1;
                        }
                        registers[opcode & 0xF00] = (registers[opcode & 0xF00] - registers[opcode & 0xF0]) & 0xFF;
                        programCounter += 2;
                        break;
                    case 6:
                        // See wiki
                        // Vx = Vy >> 1
                        registers[opcode & 0xF00] = registers[opcode & 0xF0] >> 1;
                        registers[15] = registers[opcode & 0xF0] & 1;
                        programCounter += 2;
                        break;
                    case 7:
                        // Vx = Vy - Vx
                        {
                            int x = registers[opcode & 0xF00],
                                y = registers[opcode & 0xF0],
                                x7 = x >> 7,
                                y7 = y >> 7,
                                r7 = (y - x) >> 7,
                                borrow = (~y7 & x7) | (x7 & r7) | (r7 & ~y7);

                            registers[opcode & 0xF00] = r7;
                            registers[15] = borrow;
                            programCounter += 2;
                        }
                        break;
                    case 0xE:
                        // See wiki
                        // Vx = Vy = Vy << 1
                        registers[15] = registers[opcode & 0xF0] & 0x80;
                        registers[opcode & 0xF00] = registers[opcode & 0xF0] <<= 1;
                        programCounter += 2;
                        break;
                }
                break;
            case 0x9:
                // skip if (Vx!=Vy)
                if(registers[opcode & 0xF00] != registers[opcode & 0xF0]) {
                    programCounter += 2;
                }
                programCounter += 2;
                break;
            case 0xA:
                // I = NNN
                indexPointer = opcode & 0xFFF;
                programCounter += 2;
                break;
            case 0xB:
                // PC = V0 + NNN
                programCounter = registers[0] + (opcode & 0xFFF);
                break;
            case 0xC:
                // See Wiki
                // Vx = rand() & NN
                registers[opcode & 0xF00] = ((int) Math.round(Math.random() * 256) - 1) & (opcode & 0xFF);
                programCounter += 2;
                break;
            case 0xD:
                // See wiki
                // Draw sprite
                {
                    int height = opcode & 0xF,
                        x = opcode & 0xF00,
                        y = opcode & 0xF0;

                    byte[] sprite = Arrays.copyOfRange(memory, indexPointer, indexPointer + height);
                    boolean collision = monitor.drawSprite(x, y, sprite);
                    registers[15] = collision ? 1 : 0;

                }
                programCounter += 2;
                break;
            case 0xE:
                switch(opcode & 0xFF) {
                    case 0x9E:
                        // Skip next instruction if key is pressed
                        if(keyboard[opcode & 0xF00]) {
                            programCounter += 2;
                        }
                        programCounter += 2;
                        break;
                    case 0xA1:
                        // Skip next instruction if key is not pressed
                        if(!keyboard[opcode & 0xF00]) {
                            programCounter += 2;
                        }
                        programCounter += 2;
                        break;
                }
                break;
            case 0XF:
                switch(opcode & 0xFF) {
                    case 0x07:
                        // Vx = getDelay
                        registers[opcode & 0xF00] = delayTimer;
                        programCounter += 2;
                        break;
                    case 0x0A:
                        // Vx = getKey()
                        {
                            int i = 0;
                            while(!keyboard[i] || i != 16) {
                                i++;
                            }

                            if(i != 16) {
                                registers[opcode & 0xF00] = i;
                                programCounter += 2;
                            }
                        }
                        break;
                    case 0x15:
                        // Set delayTimer to Vx
                        delayTimer = registers[opcode & 0xF00];
                        programCounter += 2;
                        break;
                    case 0x18:
                        // Set sound_timer to Vx
                        soundTimer = registers[opcode & 0xF00];
                        programCounter += 2;
                        break;
                    case 0x1E:
                        // I += VX
                        indexPointer += registers[opcode & 0xF00];
                        programCounter += 2;
                        break;
                    case 0x29:
                        // I = sprite_addr[Vx]
                        // Since fonts start at 0, index pointer will be equal to Vx
                        indexPointer = opcode & 0xF00;
                        break;
                    case 0x33:
                        // Stores BCD representation of VX
                        {
                            int value = registers[opcode & 0xF00];
                            int hundreds = (int) (value / 100.0);
                            int tens = (int) ((value % 100) / 10);
                            int units = (int) (value % 10);
                            memory[indexPointer] = (byte) hundreds;
                            memory[indexPointer + 1] = (byte) tens;
                            memory[indexPointer + 2] = (byte) units;
                        }
                        programCounter += 2;
                        break;
                    case 0x55:
                        // Store V0 to Vx at memory I
                        {
                            int vx = opcode & 0xF00;
                            for(int i = 0; i < vx + 1; i++) {
                                byte register = (byte) registers[i];
                                memory[indexPointer] = register;
                                indexPointer++;
                            }
                        }
                        programCounter += 2;
                        break;
                    case 0x65:
                        // Fill V0 to Vx from memory using I
                        {
                            int vx = opcode & 0xF00;
                            for(int i = 0; i < vx + 1; i++) {
                                registers[i] = memory[indexPointer];
                                indexPointer++;
                            }
                        }
                        programCounter += 2;
                        break;
                }
                break;
        }
    }

    private void stackPush(int address) {
        stack[stackPointer] = address;
        stackPointer++;
    }

    private int stackPop() {
        stackPointer--;
        int address = stack[stackPointer];
        stack[stackPointer] = 0;
        return address;
    }

    private void log(String input) {
        System.out.println("Opcode: " + input);
    }

    private void copyFontSetToMemory() {
        for(int i = 0; i < fontSet.length; i++) {
            memory[i] = (byte) (fontSet[i] & 0xFF);
        }
    }
}
