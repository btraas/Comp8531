package com.devrygreenhouses.comp8031;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 // /dev/null upload:
 // On my server, go to ~/bcit/8031/LargeFile/upload and run `java Server 4444`

 // Raw text download:
 // On my sever, go to ~/bcit/8031/download and run `java SimpleFileServer 4343 /var/www/bcit/ipad.split.logaa`

 // GZ text download:
 // On my server, go to ~/bcit/8031/download and run `java SimpleFileServer 4344 /var/www/bcit-gzip/ipad.split.logaa.gz`


 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView textView ;
    ImageView imageView;

    File downloadedFile = null;

    Uri currentUri = null;

    private SensorManager sensorManager;
    private Sensor acceleration;

//    public byte[] getBytes(InputStream inputStream) throws IOException {
//        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
//        int bufferSize = 1024;
//        byte[] buffer = new byte[bufferSize];
//
//        int len = 0;
//        while ((len = inputStream.read(buffer)) != -1) {
//            byteBuffer.write(buffer, 0, len);
//        }
//        return byteBuffer.toByteArray();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL);

        Toast.makeText(this, "Downloading large image from bcit.btraas.ca ...", Toast.LENGTH_LONG).show();
        textView = (TextView)findViewById(R.id.textView);
        imageView = (ImageView)findViewById(R.id.imageView);
//
//        DownloadWebPageTask task = new DownloadWebPageTask();
//        task.execute(new String[] { "www.yahoo.com" });

//        DownloadImageTask task2 = new DownloadImageTask();
//        task2.execute(new String[] { "bcit.btraas.ca" }  );

        ((Button)findViewById(R.id.selectButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

//        <Button android:id="@+id/uploadButton"
//        android:layout_height="wrap_content"
//        android:layout_width="wrap_content"
//        android:text="Upload Text"
//        android:layout_marginTop="50dp"
//
//
//                />
//
//    <Button android:id="@+id/uploadCompressButton"
//        android:layout_height="wrap_content"
//        android:layout_width="wrap_content"
//        android:text="Compress and upload text"
//
//                />

        ((Button)findViewById(R.id.downloadTextButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadTextTask2(4343, 0).execute("btraas.ca");
            }
        });
        ((Button)findViewById(R.id.downloadCompressButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadTextTask2(4344, 1).execute("btraas.ca");

//                new DownloadTextTask().execute("gzip.bcit.btraas.ca");
            }
        });


        ((Button)findViewById(R.id.uploadButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downloadedFile == null) {
                    Toast.makeText(MainActivity.this, "wait for download to complete before uploading", Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(MainActivity.this, "Uploading to btraas.ca....", Toast.LENGTH_LONG).show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
//                                if(currentUri != null) {
//                                    upload(getContentResolver().openInputStream(currentUri), 0);
//                                } else {
                                    System.out.println("file size: " + downloadedFile.length() + " bytes!");
                                    upload(new FileInputStream(downloadedFile), 0, 4444);
                               // }
//

//                                final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bcit.btraas.ca/uploads/"));
//                                MainActivity.this.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        MainActivity.this.startActivity(browserIntent);
//                                    }
//                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            }
        });

        ((Button)findViewById(R.id.uploadCompressButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downloadedFile == null) {
                    Toast.makeText(MainActivity.this, "Wait for download to complete before uploading", Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(MainActivity.this, "Uploading (GZIP) to btraas.ca....", Toast.LENGTH_LONG).show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
//                                upload(getContentResolver().openInputStream(currentUri), 1);
                               // if(currentUri != null) {
                                //    upload(getContentResolver().openInputStream(currentUri), 1);
                                //} else {
                                    upload(new FileInputStream(downloadedFile), 1, 4444);
                                //}
//                                final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bcit.btraas.ca/uploads/"));
//                                MainActivity.this.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        MainActivity.this.startActivity(browserIntent);
//                                    }
//                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            }
        });

        ((Button)findViewById(R.id.multicastButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUri == null) {
                    Toast.makeText(MainActivity.this, "Load local image before multicasting", Toast.LENGTH_LONG).show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                MultiCaster caster = new MultiCaster("224.0.0.0", 4445);

                                File outputFile = File.createTempFile("tmp", ".jpg", getCacheDir());
                                OutputStream out = new FileOutputStream(outputFile);

                                InputStream input = new BufferedInputStream(getContentResolver().openInputStream(currentUri));
                                for (int c; (c = input.read()) != -1; ) {
                                    out.write(c);
                                }

                                out.close();

                                caster.send(outputFile);
                            } catch (IOException | URISyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });

        ((Button)findViewById(R.id.uploadSensorButton)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sensorUploadPaused = !sensorUploadPaused;
            }
        });
    }

    /**
     * Get path from Uri
     * @param uri
     * @return
     */
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 1){
                if(data == null){
                    //no data present
                    return;
                }

                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ((ImageView)findViewById(R.id.imageView)).setImageBitmap(selectedImage);
                    imageStream.close();



                    currentUri = imageUri;

//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                upload(getContentResolver().openInputStream(imageUri));
//                                final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bcit.btraas.ca/uploads/"));
//                                MainActivity.this.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        MainActivity.this.startActivity(browserIntent);
//                                    }
//                                });
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void upload(InputStream stream, final int compressionMode, int port) throws IOException {
        String host = "btraas.ca";



        final long start = System.currentTimeMillis();

        Socket socket = new Socket(host, port);

        // Get the size of the file
        byte[] bytes = new byte[16 * 1024];
        OutputStream out = socket.getOutputStream();

        switch(compressionMode) {
            case 1 : out = new GZIPOutputStream(out, 4096);
        }


        int count;
        while ((count = stream.read(bytes)) > 0) {
            out.write(bytes, 0, count);
        }

        out.close();
        socket.close();

        stream.close();

        final long end = System.currentTimeMillis();
        System.out.println("Uploaded to "+host+":"+port+" in " + (end - start) + " ms with compression="+compressionMode);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Uploaded in " + (end - start) + " ms with compression="+compressionMode, Toast.LENGTH_LONG).show();
            }
        });

    }

    float startX = -1;
    float startY = -1;

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


    MultiCaster streamCaster = new MultiCaster("224.0.0.0", 4443);



    boolean sending = false;
    int sendCount = 0;
    private void getAccelerometer(SensorEvent event) {
        final float x = event.values[0];
        final float y = event.values[1];
        final float z = event.values[2];

       getSupportActionBar().setTitle("x: "+x);

       if(sending || sensorUploadPaused) {
           return;
       }

       sending = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    System.out.println("-");
                    System.out.println("sending floats: x:" + x + ", y: " + y + ", z: " + z);

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

                    Thread.sleep(100);
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


    private class DownloadImageTask extends AsyncTask<String, Void, File> {
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected File doInBackground(String... urls) {
            System.out.println("Starting doInBackground()!");
            final long start = System.currentTimeMillis();
            ArrayList<Byte> response = new ArrayList<Byte>();
            //for (String url : urls) {

            //String host = "www.google.com";
            PrintWriter pw = null;
            BufferedReader br = null;



            try {
                Socket s = new Socket();
                s.connect(new InetSocketAddress(urls[0], 80));


                pw = new PrintWriter( s.getOutputStream(), true);
                //reader for socket
                //br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                InputStream input = new BufferedInputStream(s.getInputStream());

                pw.print( "GET / HTTP/1.1\r\nHost: bcit.btraas.ca\r\nAccept: image/webp,image/apng,*/*;q=0.8\r\n\r\n" );
                pw.flush();

                File outputDir = getCacheDir(); // context being the Activity pointer
                File outputFile = File.createTempFile("tmp", ".jpg", outputDir);

                OutputStream out = new FileOutputStream(outputFile);

                boolean foundDoubleNewLine = false;

                // this is for trimming the HTTP headers!
                ArrayList<Character> expected = new ArrayList<>();
                expected.add('\r');
                expected.add('\n');
                expected.add('\r');
                expected.add('\n');
                ArrayList<Character> tmp = new ArrayList<>();
                for (int c; (c = input.read()) != -1; ) {

                    if(tmp.size() == 4 && tmp.equals(expected)) {
                        foundDoubleNewLine = true;
                    }
                    if(foundDoubleNewLine)
                        out.write(c);
                    else {
                        if(c != '\r' && c != '\n') {
                            tmp.clear();
                        } else {
                            tmp.add((char)c);
                        }
                    }
                }

                    // response  = s_in.readLine();
                pw.close();
                input.close();
                out.close();
                s.close();

                final long end = System.currentTimeMillis();
                System.out.println("Downloaded in " + (end - start)  + " ms!");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Downloaded in " + (end - start)  + " ms", Toast.LENGTH_LONG).show();
                    }
                });
                return outputFile;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //}
            return null;
        }

        @Override
        protected void onPostExecute(File result) {
            if(result == null) return;

            MainActivity.this.downloadedFile = result;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(result), null, options);
                imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    private class DownloadTextTask2 extends AsyncTask<String, Void, File> {

        int port = 0;
        int compressionMode = 0;
        public DownloadTextTask2(int port, int compressionMode) {
            this.port = port;
            this.compressionMode = compressionMode;
        }

        @Override
        protected void onPreExecute() {
//            MainActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
            Toast.makeText(MainActivity.this, "Downloading text", Toast.LENGTH_LONG).show();

//            });
        }

        // url must be a host only
        @Override
        protected File doInBackground(String... server) {
            System.out.println("Starting doInBackground()!");

            String response = "";
            //for (String url : urls) {

            //String host = "www.google.com";
            PrintWriter pw = null;
            BufferedReader br = null;

            final long startTime = System.currentTimeMillis();
            System.out.println("Downloading from " + server[0]+":"+port);


            try {
                File outputDir = getCacheDir(); // context being the Activity pointer
                File outputFile = File.createTempFile("tmp", ".txt", outputDir);

                OutputStream out = new FileOutputStream(outputFile);


                Socket s = new Socket(server[0], port);
                InputStream input = new BufferedInputStream(s.getInputStream());

                switch(compressionMode) {
                    case 1 : input = new GZIPInputStream(input, 4096);
                }

                byte[] buffer = new byte[4096];
                int len = 0;

                while ((len = input.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }

                out.close();
                input.close();

                s.close();

                BufferedReader brTest = new BufferedReader(new FileReader(outputFile));
                String text = brTest.readLine();

                System.out.println("Downloaded in " + (System.currentTimeMillis() - startTime) + " ms");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Downloaded in " + (System.currentTimeMillis() - startTime)  + " ms", Toast.LENGTH_LONG).show();
                    }
                });
                System.out.println("first line: "+text);
                brTest.close();

                return outputFile;

            } catch (Exception e) {
                e.printStackTrace();
            }
            //}
            return null;
        }

        @Override
        protected void onPostExecute(File result) {
            if(result == null) return;

            MainActivity.this.downloadedFile = result;

//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            try {
//                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(result), null, options);
//                imageView.setImageBitmap(bitmap);
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
        }
    }

    class BackgroundThread extends Thread  {
        @Override
        public void run() {
            try{
                Socket s = new Socket();
                PrintWriter pw = null;
                BufferedReader br = null;
                s.connect(new InetSocketAddress("www.google.com" , 80));
                pw = new PrintWriter( s.getOutputStream(), true);
                br = new BufferedReader(new
                        InputStreamReader(s.getInputStream()));
                pw.println( "GET / HTTP/1.1\r\n\r\n" );
                String response;
                while ((response = br.readLine()) != null) {
                    textView.setText(response);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

}
