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

    public void cycle() {
        decodeOpcode();
        executeOpcode();
        //byte[] test = {(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80};
        //monitor.drawSprite(2, 0, test);

        if(delayTimer > 0) {
            delayTimer--;
        }
    }

    public void decodeOpcode() {
        int msb = memory[programCounter] & 0xFF;
        int lsb = memory[programCounter + 1] & 0xFF;

        opcode = msb << 8 | lsb;
    }

    public void setKey(int key) {
        if(key < 16) {
            keyboard[key & 0xF] = true;
        }
    }

    public void unsetKey(int key) {
        if(key < 16) {
            keyboard[key & 0xF] = false;
        }
    }

    private void clearScreen() {
        monitor.clearScreen();
    }

    public void executeOpcode() {
        byte[] test = {(byte) 0xA0};
        //monitor.drawSprite(0, 1, test);
        //System.out.println(opcode);
        int vx = (opcode & 0xF00) >> 8;
        int vy = (opcode & 0xF0) >> 4;

        switch(opcode >> 12) {
            case 0x0:
                switch(opcode & 0xFF) {
                    case 0xE0:
                        clearScreen();
                        programCounter += 2;
                        break;
                    case 0xEE:
                        programCounter = stackPop();
                        break;
                }
                break;
            case 0x1:
                // Goto NNN
                {
                    programCounter = opcode & 0xFFF;
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
                    int registerValue = registers[vx];
                    if (registerValue == (opcode & 0xFF)) {
                        programCounter += 2;
                    }
                    programCounter += 2;
                }
                break;
            case 0x4:
                // Skip next instruction if Vx != kk
                {
                    int registerValue = registers[vx];
                    if (registerValue != (opcode & 0xFF)) {
                        programCounter += 2;
                    }
                    programCounter += 2;
                }
                break;
            case 0x5:
                // Skip next instruction if Vx == Vy
                if(registers[vx] == registers[vy]){
                    programCounter += 2;
                }
                programCounter += 2;
                break;
            case 0x6:
                // Vx = NN
                registers[vx] = (opcode & 0xFF);
                programCounter += 2;
                break;
            case 0x7:
                // Vx += NN, no carry
                registers[vx] = (registers[vx] + (opcode & 0xFF)) & 0xFF;
                programCounter += 2;
                break;
            case 0x8:
                switch(opcode & 0xF) {
                    case 0:
                        // Vx = Vy
                        registers[vx] = registers[vy];
                        programCounter += 2;
                        break;
                    case 1:
                        // Vx = Vx | Vy
                        registers[vx] |= registers[vy];
                        programCounter += 2;
                        break;
                    case 2:
                        // Vx = Vx & Vy
                        registers[vx] &= registers[vy];
                        programCounter += 2;
                        break;
                    case 3:
                        // Vx = Vx ^ Vy
                        registers[vx] ^= registers[vy];
                        programCounter += 2;
                        break;
                    case 4:
                        // Vx = Vx + Vy
                        {
                            int result = registers[vx] + registers[vy];
                            if ((result & 0xF00) != 0) {
                                registers[15] = 1;
                            }
                            registers[vx] = result & 0xFF;
                        }
                        programCounter += 2;
                        break;
                    case 5:
                        // Vx -= Vy
                        if(registers[vx] > registers[vy]) {
                            registers[15] = 1;
                        }
                        registers[vx] = (registers[vx] - registers[vy]) & 0xFF;
                        programCounter += 2;
                        break;
                    case 6:
                        // See wiki
                        // Vx = Vy >> 1
                        registers[vx] = registers[vy] >> 1;
                        registers[15] = registers[vy] & 1;
                        programCounter += 2;
                        break;
                    case 7:
                        // Vx = Vy - Vx
                        {
                            int x = registers[vx],
                                y = registers[vy],
                                x7 = x >> 7,
                                y7 = y >> 7,
                                r7 = (y - x) >> 7,
                                borrow = (~y7 & x7) | (x7 & r7) | (r7 & ~y7);

                            registers[vx] = r7;
                            registers[15] = borrow;
                            programCounter += 2;
                        }
                        break;
                    case 0xE:
                        // See wiki
                        // Vx = Vy = Vy << 1
                        registers[15] = registers[vy] & 0x80;
                        registers[vx] = registers[vy] <<= 1;
                        programCounter += 2;
                        break;
                }
                break;
            case 0x9:
                // skip if (Vx!=Vy)
                if(registers[vx] != registers[vy]) {
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
                registers[vx] = ((int) Math.round(Math.random() * 256) - 1) & (opcode & 0xFF);
                programCounter += 2;
                break;
            case 0xD:
                // Draw sprite
                {
                    int height = opcode & 0xF,
                        x = registers[vx],
                        y = registers[vy];

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
                        if(keyboard[registers[vx]]) {
                            programCounter += 2;
                        }
                        programCounter += 2;
                        break;
                    case 0xA1:
                        // Skip next instruction if key is not pressed
                        if(!keyboard[registers[vx]]) {
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
                        registers[vx] = delayTimer;
                        programCounter += 2;
                        break;
                    case 0x0A:
                        // Vx = getKey()
                        {
                            int i = 0;
                            while(i != 16) {
                                if(keyboard[i]) {
                                    break;
                                }else{
                                    i++;
                                }
                            }

                            if(i != 16) {
                                registers[vx] = i;
                                programCounter += 2;
                            }
                        }
                        break;
                    case 0x15:
                        // Set delayTimer to Vx
                        delayTimer = registers[vx];
                        programCounter += 2;
                        break;
                    case 0x18:
                        // Set sound_timer to Vx
                        soundTimer = registers[vx];
                        programCounter += 2;
                        break;
                    case 0x1E:
                        // I += VX
                        indexPointer += registers[vx];
                        programCounter += 2;
                        break;
                    case 0x29:
                        // I = sprite_addr[Vx]
                        // Since fonts start at 0, index pointer will be equal to Vx
                        indexPointer = registers[vx];
                        programCounter += 2;
                        break;
                    case 0x33:
                        // Stores BCD representation of VX
                        {
                            int value = registers[vx];
                            int hundreds = (int) (value / 100.0);
                            int tens = (value % 100) / 10;
                            int units = value % 10;
                            memory[indexPointer] = (byte) hundreds;
                            memory[indexPointer + 1] = (byte) tens;
                            memory[indexPointer + 2] = (byte) units;
                        }
                        programCounter += 2;
                        break;
                    case 0x55:
                        // Store V0 to Vx at memory I
                        {
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
