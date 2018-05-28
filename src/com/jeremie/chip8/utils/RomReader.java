package com.jeremie.chip8.utils;

import java.io.*;
import java.nio.ByteBuffer;

public class RomReader {
    public byte[] read(String romName) {
        ClassLoader classLoader = getClass().getClassLoader();
        ByteBuffer bb = ByteBuffer.allocate(3584);
        File file = new File(classLoader.getResource(romName + ".rom").getFile());

        try {
            InputStream is = new FileInputStream(file);
            int byteRead;
            while((byteRead = is.read()) != -1) {
                bb.put((byte) (byteRead & 0xFF));
            }
        }catch(IOException ex) {
            ex.printStackTrace();
        }
        return bb.array();
    }
}
