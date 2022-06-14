package com.example.bluetoothclient;

import static com.example.bluetoothclient.MessageUtils.HEADER_LEN;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamParser {



    private List<Byte> buffer = new ArrayList<>();

    private int intFromBytes(List<Byte> byte4){
        int result = 0;
        for (byte b: byte4) {
            result = result<<8;
            result+= (b & 0xff);
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Byte> parse(Byte[] bytes, int numBytes){
        Log.d(" ", "parse: " + Arrays.asList(bytes));
        buffer.addAll(Arrays.asList(bytes).subList(0, numBytes));
        byte[] header = {bytes[0]};
        Log.d("buffer", String.valueOf(buffer));
        if(HEADER_LEN.get(buffer.get(0)) > buffer.size())
            return null;

        int len = intFromBytes(buffer.subList(1, 5));
        Log.d("PARSE msg_len", String.valueOf(len));
        Log.d("PARSE size", String.valueOf(buffer.size()));

        if(buffer.size() >= len) {
            Log.d("PARSE size", String.valueOf(buffer.subList(len, buffer.size())));
            List<Byte> result = buffer.stream().collect(Collectors.toList());
            buffer = new ArrayList<>();
            return result;
        }
        return null;
    }
}
