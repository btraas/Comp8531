package com.devrygreenhouses.comp8031;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v7.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class SensorActivity extends AppCompatActivity implements SensorEventListener {

    boolean sensorUploadPaused = true;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    byte b(int i) { return (byte) i; }

    byte[] prev = new byte[] {0,0,0,0, 0,0,0,0, 0,0,0,0};
    float prevX = 0;
    float prevY = 0;
    float prevZ = 0;

    short prevXS = 0;
    short prevYS = 0;
    short prevZS = 0;

    MultiCaster streamCaster = new MultiCaster("224.0.0.0", 4443);



    boolean sending = false;
    int sendCount = 0;
    private void getAccelerometer(SensorEvent event) {
        final float x = event.values[0];
        final float y = event.values[1];
        final float z = event.values[2];

        short sX = (short)(x * 1000);
        short sY = (short)(y * 1000);
        short sZ = (short)(z * 1000);


        getSupportActionBar().setTitle("sx: "+sX);

        if(sending || sensorUploadPaused) {
            return;
        }

        sending = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    try {
                        String MYFILE = "accelerometer.text";
                        String strText = x+","+y+","+z + "\n";

                        // MODE_APPEND, MODE_WORLD_READABLE, MODE_WORLD_WRITEABLE
                        // create new file or rewrite existing
                        // append to file
                        FileOutputStream fos = openFileOutput(MYFILE, MODE_APPEND);

                        fos.write(strText.getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    float deltaX = (x-prevX);
                    if(Math.abs(deltaX) < 0.01) deltaX = 0;

                    float deltaY = (y-prevY);
                    if(Math.abs(deltaY) < 0.01) deltaY = 0;

                    float deltaZ = (z-prevZ);
                    if(Math.abs(deltaZ) < 0.01) deltaZ = 0;


                    System.out.println("Delta floats: "+(deltaX)+", "+(deltaY)+", "+(deltaZ));
                    System.out.println("sending floats: x:" + x + ", y: " + y + ", z: " + z);

                    // Simulate short + delta compression
                    int xBytes = deltaX == 0 ? 0 : (Math.abs(deltaX) < 128 ? 1 : 2);
                    int yBytes = deltaY == 0 ? 0 : (Math.abs(deltaY) < 128 ? 1 : 2);
                    int zBytes = deltaZ == 0 ? 0 : (Math.abs(deltaZ) < 128 ? 1 : 2);

                    float total = 1 + xBytes + yBytes + zBytes;

                    if(total == 1) {
                        // this can be skipped
                        sending = false;
                        return;
                    }

                    int pct = (int)(((12f-total)/12f) * 100f);


                    System.out.println("Simulated delta compression: 1 + " + xBytes + " + " + yBytes + " + " + zBytes + " = "+(total) + " "+pct+"% saved");

                    prevX = x;
                    prevY = y;
                    prevZ = z;

                    // raw, no compression
                    ByteBuffer buf = ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN);
                    buf.putFloat(x);
                    buf.putFloat(4, y);
                    buf.putFloat(8, z);
                    byte[] array0 = buf.array();


                    //boolean[] bitArray = new boolean[] {false,false, false,false, false,false};

                    // false/0 indicates no change, true/1 indicates changed.

                    // bit 0 pertains to float 0 byte 0
                    // bit 1 pertains to float 0 byte 1
                    // bit 2 pertains to float 1 byte 0
                    // bit 3 pertains to float 1 byte 1
                    // ... etc

                    // Bytes 2 and 3 of each float are considered changed, always.

                    int byte0 = 0;

                    int dataBytes = 6;

                    boolean f0b0 = false;
                    boolean f0b1 = false;
                    boolean f1b0 = false;
                    boolean f1b1 = false;
                    boolean f2b0 = false;
                    boolean f2b1 = false;

                    if(array0[0] != prev[0]) {
                        byte0 += 1;
                        dataBytes++;
                        f0b0 = true;
                    }
                    if(array0[1] != prev[1]) {
                        byte0 += 2;
                        dataBytes++;
                        f0b1 = true;
                    }
                    if(array0[4] != prev[4]) {
                        byte0 += 4;
                        dataBytes++;
                        f1b0 = true;
                    }
                    if(array0[5] != prev[5]) {
                        byte0 += 8;
                        dataBytes++;
                        f1b1 = true;
                    }
                    if(array0[8] != prev[8]) {
                        byte0 += 16;
                        dataBytes++;
                        f2b0 = true;
                    }
                    if(array0[9] != prev[9]) {
                        byte0 += 32;
                        dataBytes++;
                        f2b1 = true;
                    }


                    // old: 0,1,0,0, 0,2,0,0, 0,4,0,0
                    // new: 1,2,0,0, 4,8,0,0, 16,32,0,0

                    boolean forceRefresh = false;
                    if(sendCount % 9 == 0) {
                        System.out.println("Forcing a refresh!");
                        forceRefresh = true;
                        byte0 = (1 + 2 + 4 + 8   +  16 + 32); // force refresh
                        dataBytes = 12;
                        sendCount = 1;
                    }


//                    System.out.println("Sending number of bytes: 1 + "+(dataBytes));
                    ByteBuffer sendBuf = ByteBuffer.allocate(dataBytes);
//                    sendBuf.put((byte)byte0);

                    int idx = 0;



                    if(f0b0 || forceRefresh)
                        sendBuf.put(idx++, array0[0]);
                    if(f0b1 || forceRefresh)
                        sendBuf.put(idx++, array0[1]);
                    sendBuf.put(idx++, array0[2]);
                    sendBuf.put(idx++, array0[3]);

                    if(f1b0 || forceRefresh)
                        sendBuf.put(idx++, array0[4]);
                    if(f1b1 || forceRefresh)
                        sendBuf.put(idx++, array0[5]);
                    sendBuf.put(idx++, array0[6]);
                    sendBuf.put(idx++, array0[7]);

                    if(f2b0 || forceRefresh)
                        sendBuf.put(idx++, array0[8]);
                    if(f2b1 || forceRefresh)
                        sendBuf.put(idx++, array0[9]);
                    sendBuf.put(idx++, array0[10]);
                    sendBuf.put(idx, array0[11]);



//
//                    for (int i = 5; i >= 0; i--) {
//                        if (array0[i] != prev[i]) {
//                            //bitArray[i] = true;
//                            byte0 += Math.pow(2, i);
//                        }
//                    }

                    if(sendCount % 8 == 0) {
                        byte0 = (1 + 2 + 4 + 8 + 16 + 32); // force refresh

                    }

//                    System.out.println("\nData: " + Arrays.toString(array0));
//                    System.out.println("Byte0: " + byte0);


                    // mode 0: all new
                    // mode 1: float 1 retain byte 1
                    // mode 2: float 2 retain byte 1
                    // mode 3: float 3 retain byte 1
                    // mode 4: float 4 retain byte 1

                    // mode 5: float 1 retain byte 2


                    // v2
//                    ByteBuffer buf = ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN);
//                    buf.put()
//                    buf.putFloat(x);
//                    buf.putFloat(4, y);
//                    buf.putFloat(8, z);


//                    System.out.println("sending bytes: " + Arrays.toString(sendBuf.array()));


//                    System.out.println("sending bytes: " + Arrays.toString(array));
                    streamCaster.sendBytes(new byte[]{(byte)byte0});
                    streamCaster.sendBytes(sendBuf.array());

                    //streamCaster.sendBytes(array0);
                    prev = array0;

                    Thread.sleep(10);
                    sending = false;

                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendCount++;
                sending = false;
            }

        }).start();



    }
}
