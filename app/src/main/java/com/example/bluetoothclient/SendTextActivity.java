package com.example.bluetoothclient;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.nio.charset.Charset;

public class SendTextActivity extends CommunicationsActivity {

    private String mMessageFromServer = "";

    private TextView mResponseTextView;
    private SeekBar mSpeedSeekBar;
    EditText msgEdit;
    Button submitMsg;

    Thread listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mResponseTextView = (TextView)findViewById(R.id.serverReplyText);

        msgEdit = (EditText)findViewById(R.id.editText);

        submitMsg = (Button)findViewById(R.id.sendButton);

        submitMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = msgEdit.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);

                int i;
                byte[] buffer = new byte[1024];
                i = mBluetoothConnection.read(buffer);
                mMessageFromServer = new String(buffer, 0, i);
                mResponseTextView.setText(mMessageFromServer);
            }
        });



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

