package com.example.bluetoothclient;

import static com.example.bluetoothclient.MessageUtils.*;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SendTextActivity extends AppCompatActivity {

    private static final String TAG = "TextActivity";


//    private StreamParser streamParser = new StreamParser();
    TextView chosenBoard;
    TextView chosenBinFile;
    EditText msgEdit;
    Button submitMsg;

    AppCompatActivity activity = this;

    ConnectionService service;

    private String deviceAddress;
    private ListView msgView;
    private ListView boardsView;
    private ListView binaryView;

    private ArrayAdapter<String> msgLog;
    private ArrayAdapter<String> boardFiles;
    private ArrayAdapter<String> binFiles;

    private void handleServerMessage(List<Byte> bytes) throws JSONException {
        if(bytes == null)
            return;

        Log.d(TAG, "handleServerMessage: " + bytes.size());
        Log.d(TAG, "handleServerMessage: " + bytes);
        byte[] arr = new byte[bytes.size()-5];
        int j = 0;
        for(byte b: bytes.subList(5, bytes.size())) {
            arr[j]=b;
            j++;
        }

        Log.d(TAG, "handleServerMessage: " + arr[arr.length-1]);

        String msg = new String(arr, StandardCharsets.UTF_8);

        msgLog.add("server:  " + msg);

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        switch(bytes.get(0)){
            case JSON_FILES_DATA:
                JSONObject obj = new JSONObject(msg);
                List<String> boards = JArrayToList(obj.getJSONArray("boards"));
                boardFiles.clear();
                boardFiles.addAll(boards);

                List<String> binary = JArrayToList(obj.getJSONArray("binary"));
                binFiles.clear();
                binFiles.addAll(binary);

                boardFiles.notifyDataSetChanged();
                binFiles.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private List<String> JArrayToList(JSONArray jArray) {
        ArrayList<String> result = new ArrayList<String>();
        if (jArray != null) {
            for (int i=0;i<jArray.length();i++){
                result.add(jArray.optString(i));
            }
        }
        return result;
    }


    private final Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageUtils.MESSAGE_SEND:

                    byte[] writeBuf = ((ByteBuffer) msg.obj).array();
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    msgLog.add("Me:  " + writeMessage);
                    break;
                case MessageUtils.MESSAGE_READ:

//                    byte[] readBuf = (byte[]) msg.obj;
//                    Log.d(TAG, String.valueOf(Integer.valueOf(readBuf[0])));
//                    Log.d(TAG, new String(readBuf));
                    Log.d(TAG, String.valueOf(msg.arg1));
//                    List<Byte> msgBytes = streamParser.parse(toObjects((byte[]) msg.obj));
//                    ;
                    try {
                        handleServerMessage((List<Byte>) msg.obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case MessageUtils.MESSAGE_TOAST:
                    Toast.makeText(activity, msg.getData().getString(MessageUtils.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "creating");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communications);

        msgLog = new ArrayAdapter<>(this, R.layout.msg);
        boardFiles = new ArrayAdapter<>(this, R.layout.msg);
        binFiles = new ArrayAdapter<>(this, R.layout.msg);

        Intent newint = getIntent();
        deviceAddress = newint.getStringExtra(DeviceListActivity.EXTRA_ADDRESS);

        msgEdit = (EditText)findViewById(R.id.editText);

        chosenBoard = (TextView) findViewById(R.id.boardsText);
        chosenBinFile = (TextView) findViewById(R.id.binaryText);


        service = new ConnectionService(handler, deviceAddress);
    }

    @Override
    protected void onStart() {
        super.onStart();
        msgEdit = (EditText)findViewById(R.id.editText);

        submitMsg = (Button)findViewById(R.id.sendButton);

        submitMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = msgEdit.getText().toString().getBytes(Charset.defaultCharset());
                service.write(bytes);

            }
        });

        Button requestFiles = findViewById(R.id.filesButton);
        requestFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = getHeader(GET_FILES_REQUEST, 0);
                Log.d(TAG, Arrays.toString(bytes));
                service.write(bytes);
            }
        });

        Button requestFlash = findViewById(R.id.flashRequest);
        requestFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] data = String.format("{\"target\": \"%s\", \"board\": \"%s\"}",
                                chosenBinFile.getText(), chosenBoard.getText())
                        .getBytes(StandardCharsets.UTF_8);
                byte[] header = getHeader(FLASH_REQUEST, data.length);
                Log.d(TAG, Arrays.toString(header));
                Log.d(TAG, Arrays.toString(data));
                Log.d(TAG, new String(data));
                service.write(header);
                service.write(data);
            }
        });


        binaryView = (ListView) findViewById(R.id.binaryView);
        binaryView.setAdapter(binFiles);
        binaryView.setOnItemClickListener(binariesClickListener);


        boardsView = (ListView) findViewById(R.id.boardsView);
        boardsView.setAdapter(boardFiles);
        boardsView.setOnItemClickListener(boardsClickListener);

        msgView = (ListView) findViewById(R.id.msgView);
        msgView.setAdapter(msgLog);

//        byte[] bytes = getHeader(GET_FILES_REQUEST, 0);
//        Log.d(TAG, Arrays.toString(bytes));
//        service.write(bytes);
    }

    private AdapterView.OnItemClickListener boardsClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            String filename = ((TextView) v).getText().toString();
            chosenBoard.setText(filename);
        }
    };

    private AdapterView.OnItemClickListener binariesClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            String filename = ((TextView) v).getText().toString();
            chosenBinFile.setText(filename);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}



