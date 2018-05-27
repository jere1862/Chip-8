package com.jeremie.chip8;

import java.io.*;
import java.nio.ByteBuffer;

public class RomReader {
    public byte[] read(String romName) {
        ClassLoader classLoader = getClass().getClassLoader();
        ByteBuffer bb = ByteBuffer.allocate(3584);
        File file = new File(classLoader.getResource(romName + ".rom").getFile());

        try {
            InputStream is = new FileInputStream(file);
            byte byteRead;
            while((byteRead = (byte) is.read()) != -1) {
                bb.put(byteRead);
            }
        }catch(IOException ex) {
            ex.printStackTrace();
        }
        return bb.array();
    }
}
