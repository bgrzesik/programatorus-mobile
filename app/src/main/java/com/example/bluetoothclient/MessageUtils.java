package com.example.bluetoothclient;

import java.nio.ByteBuffer;
import java.util.Map;

public interface MessageUtils {

    // Inner consts
    int MESSAGE_SEND = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_TOAST = 3;

    String TOAST = "toast";



    // consts shared with server
    byte CLEAR = 0;
    byte LOG_TEXT = 1;
    byte JSON_FILES_DATA = 2;
    byte GET_FILES_REQUEST = 3;
    byte FLASH_REQUEST = 4;
    Map<Byte, Integer> HEADER_LEN = Map.of(
            LOG_TEXT, 5,
            JSON_FILES_DATA, 5,
            GET_FILES_REQUEST, 5,
            CLEAR, 5,
            FLASH_REQUEST, 5
    );

    public static byte[] getHeader(byte type, int dataLength){
        byte[] result = new byte[HEADER_LEN.get(type)];
        result[0]=type;
        int i=1;
        for (byte b : ByteBuffer.allocate(4).putInt(dataLength+5).array()) {
            result[i] = b;
            i++;
        }
        return result;
    }
}
