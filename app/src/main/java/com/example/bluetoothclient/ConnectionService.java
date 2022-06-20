package com.example.bluetoothclient;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// TODO(bgrzesik) migrate this to actual Android service/BroadcastReceiver
public class ConnectionService {

    private static final String TAG = "CoennctionService";
    public static final int PUMP_DELAY = 10;

    private boolean connected = true;
    private ProgressDialog progressDialog;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private ConnectedThread connectedThread;
    private Handler handler;
    private String mDeviceAddress;
    private Deque<ByteBuffer> mPendingWrites = new ArrayDeque<>();

    private ScheduledExecutorService mExecutor = Executors.newScheduledThreadPool(1);
    private Future<?> mPendingPumpWrites = null;


    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ConnectionService(Handler handler, String deviceAddress) {
        this.handler = handler;
        this.mDeviceAddress = deviceAddress;
        connect();
    }

    @SuppressLint("MissingPermission")
    private void connect() {
        try {
            if (bluetoothSocket == null || !connected) {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    //get the mobile bluetooth device
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mDeviceAddress);   //connects to the device's address and checks if it's available
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID); //create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                bluetoothSocket.connect();//start connection
            }
        } catch (IOException e) {
            connected = false;
        }
        if(connected) {
            connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();
        }

        pumpPendingWrites();
    }

    private void schedulePumpPendingWrites(boolean instant) {
        Log.d(TAG, "schedulePumpPendingWrites: instant=" + instant);
        if (mPendingPumpWrites != null && !mPendingPumpWrites.isDone()) {
            return;
        }
        if (instant) {
            mPendingPumpWrites = mExecutor.submit(this::pumpPendingWrites);
        } else {
            mPendingPumpWrites = mExecutor.schedule(this::pumpPendingWrites, PUMP_DELAY, TimeUnit.MILLISECONDS);
        }
    }

    private void pumpPendingWrites() {
        Log.d(TAG, "pumpPendingWrites: connectedThread=" + connectedThread);
        if (connectedThread == null) {
            return;
        }
        while (!mPendingWrites.isEmpty()) {
            ByteBuffer byteBuffer = mPendingWrites.peek();

            Log.d(TAG, "pumpPendingWrites: Writing buffer size=" + byteBuffer.position());
            try {
                if (!connectedThread.write(byteBuffer)) {
                    connectedThread = null;
                    // TODO(bgrzesik): reconnect
                    return;
                }
            } catch (Throwable th) {
                Log.e(TAG, "pumpPendingWrites: Error while writing", th);
                return;
            }

            mPendingWrites.pop();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        Arrays.setAll(bytes, n -> bytesPrim[n]);
        return bytes;
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for receiving bytes
        private StreamParser streamParser = new StreamParser();



        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
//                    Log.d(TAG, String.valueOf(numBytes));
//                    Log.d(TAG, String.valueOf(mmBuffer[0]));
//                    Log.d(TAG, mmBuffer.toString());
                    List<Byte> msgBytes = streamParser.parse(toObjects(mmBuffer), numBytes);
//                    Arrays.fill( mmBuffer, (byte) 0);
                    if(msgBytes != null) {
                        Message readMsg = handler.obtainMessage(
                                MessageUtils.MESSAGE_READ, msgBytes.size(), -1,
                                msgBytes);
                        readMsg.sendToTarget();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public boolean write(ByteBuffer bytes) {
            try {
                mmOutStream.write(bytes.array());
                Log.d(TAG, Arrays.toString(bytes.array()));
                Log.d(TAG, "write: " + new String(bytes.array()));

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageUtils.MESSAGE_SEND, -1, -1, bytes);
                writtenMsg.sendToTarget();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageUtils.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }

            return false;
        }
    }

    public void write(byte[] bytes){
        this.write(ByteBuffer.wrap(bytes));
    }

    public void write(ByteBuffer byteBuffer) {
        mPendingWrites.add(byteBuffer);

        if (connectedThread != null) {
            schedulePumpPendingWrites(false);
        } else {
            connect();
        }
    }

}


