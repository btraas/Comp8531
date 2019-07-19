package com.devrygreenhouses.comp8031;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.*;

/**
 *
 // /dev/null upload:
 // On my server, go to ~/bcit/8031/LargeFile/upload and run `java Server 4444`

 // Raw text download:
 // On my sever, go to ~/bcit/8031/download and run `java SimpleFileServer 4343 /var/www/bcit/ipad.split.logaa`

 // GZ text download:
 // On my server, go to ~/bcit/8031/download and run `java SimpleFileServer 4344 /var/www/bcit-gzip/ipad.split.logaa.gz`


 */

public class MainActivity extends SensorActivity {
    TextView textView ;
    ImageView imageView;

    File downloadedFile = null;

    Uri currentUri = null;

    boolean automateUploadRunning = false;



    private SensorManager sensorManager;
    private Sensor acceleration;


    private boolean uploadEnabled = true;



    // Mode -> List of pairs (write time, close time)
    private HashMap<Integer,ArrayList<Pair<Long,Long>>> uploadTimes = new HashMap<Integer, ArrayList<Pair<Long,Long>>>(){{
        for(int i=0; i<100; i++) {
            //if(getAlg(i).length() < 3) {
                put(i, new ArrayList<Pair<Long,Long>>());
            //}
        }
    }};
    private HashMap<Integer,Long> compressedSizes = new HashMap<>();

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

        initUI();

    }



    private void checkAndAsyncUpload(final int compressionMode) {
        System.out.println("checkAndAsyncUpload ("+Compressor.getAlg(compressionMode)+") to btraas.ca....");

        if(downloadedFile == null) {
            Toast.makeText(MainActivity.this, "Wait for download to complete before uploading", Toast.LENGTH_LONG).show();
        } else {

            Toast.makeText(MainActivity.this, "Uploading ("+Compressor.getAlg(compressionMode)+") to btraas.ca....", Toast.LENGTH_LONG).show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//
                        upload(new FileInputStream(downloadedFile), compressionMode, 4444);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
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
        System.out.println("upload ("+Compressor.getAlg(compressionMode)+") to btraas.ca....");


        final long start = System.currentTimeMillis();

//        Socket socket = new Socket(host, port); // if you uncomment, make sure to close it!

        File tmpFile = null;
        OutputStream out = null;

        try {
            byte[] bytes = new byte[16 * 1024]; // 16kb
//            OutputStream out = null; // = socket.getOutputStream();

//            FileOutputStream tmpOS = new FileOutputStream(tmpFile);

            System.out.println(" opening "+Compressor.getAlg(compressionMode)+" stream!");

            // First, get an output stream from an upload URL or destination file

            if(uploadEnabled) {
                System.out.println(" streaming to "+host+":"+port+"!");
                Socket socket = new Socket(host, port);
//                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                out = socket.getOutputStream();
            } else {
                tmpFile = File.createTempFile("tmp", ".gz", getCacheDir());
                System.out.println(" saving to "+tmpFile.getName()+"!");

                out = new FileOutputStream(tmpFile);
            }

            // Secondly, wrap the output stream in an output byte counter
            ByteCounterOutputStream _out = new ByteCounterOutputStream(out);
            StreamByteCounter outputByteCounter = _out;
            out = _out;

            // Thirdly, wrap the output stream in the desired compression mode

            out = Compressor.compressOutputStream(compressionMode, out);
            if(out == null) return;

            // Finally, wrap the output stream in the input byte counter
            ByteCounterOutputStream _in = new ByteCounterOutputStream(out);
            StreamByteCounter inputByteCounter = _in;
            out = _in;


            int size = stream.available();
            int sizeKb = Math.round(size / 1024);

            float pct = 0f;
            float lastPrintedPct = -1f;

            Float bytesLoaded = 0f;
            int count;
            long printTime = System.currentTimeMillis();
            while ((count = stream.read(bytes)) > 0) {
//                System.out.println(" read "+count+" bytes from stream!");

                bytesLoaded += (bytes.length); // used for counting stream.read
                out.write(bytes, 0, count);

                pct = Math.round((bytesLoaded / size) * 1000f) / 10f;
//                if(pct > lastPrintedPct) {
//                    int readKb = Math.round(bytesLoaded / 1024);
//                    System.out.println(" "+readKb + "KB /" + sizeKb + " kb: " + pct + "%");
//                    lastPrintedPct = pct;
//                }

                if(System.currentTimeMillis() > printTime + 1000) {
                    int readKb = Math.round(bytesLoaded / 1024);
                    System.out.println(" "+readKb + "KB /" + sizeKb + " kb: " + pct + "%");
                    printTime = System.currentTimeMillis();
                }

            }
            System.out.println(" closing "+Compressor.getAlg(compressionMode)+" stream!");
            final long writeEnd = System.currentTimeMillis();

            out.close(); // some streams only write on close
            out = null;
            System.out.println(Compressor.getAlg(compressionMode) + " stream closed!");

            final long end = System.currentTimeMillis();
//            float kbSaved = Math.round(outputByteCounter.getOutputBytes() / 100) / 10f;

            System.out.println(outputByteCounter.comparisonString(inputByteCounter));

            if(uploadEnabled)
                System.out.println("Uploaded to " + host + ":" + port + " in " + (end - start) + " ms with compression=" + compressionMode + " -> "+Compressor.getAlg(compressionMode));

            compressedSizes.put(compressionMode, new Long(outputByteCounter.getOutputBytes()));
            uploadTimes.get(compressionMode).add(new Pair(writeEnd - start, end - writeEnd));

            final int uploadCount = uploadTimes.get(compressionMode).size();

            Long totalWrite = 0L;
            Long totalClose = 0L;
            for (Pair<Long,Long> times : uploadTimes.get(compressionMode)) {

                totalWrite += (times.first);
                totalClose += (times.second);

            }
            final Long avgWrite = totalWrite / uploadCount;
            final float avgWriteS = Math.round(avgWrite / 10) / 100f;
            final Long avgClose = totalClose / uploadCount;
            final float avgCloseS = Math.round(avgClose / 10) / 100f;

//                log.add(end-start);
//                uploadTimes.put(compressionMode, log)

            final String msg = "Completed "+outputByteCounter.getOutputAuto()+" in " + (end - start) + " ms with compression=" + compressionMode + " -> "
                    +Compressor.getAlg(compressionMode)+" (avg=" + avgWriteS + "s +" + avgCloseS + "s in " + uploadCount + " uploads)";
            System.out.println(msg);

            if(compressionMode == 1) { // first mode, show averages so far
                HashMap<Integer, Pair<Long,Long>> averages = new HashMap<>();
                String str = "Loop " + uploadCount+" averages so far: ";

                for(Integer uploadType: uploadTimes.keySet()) {
                    Long thisTypeTotalWrite = 0L;
                    Long thisTypeTotalClose = 0L;

                    for (Pair<Long,Long> times : uploadTimes.get(uploadType)) {

                        thisTypeTotalWrite += times.first;
                        thisTypeTotalClose += times.second;

                    }
                    final Long thisTypeAvgWrite = thisTypeTotalWrite / uploadCount;
                    final float thisTypeAvgWriteS = Math.round(thisTypeAvgWrite / 10) / 100f;

                    final Long thisTypeAvgClose = thisTypeTotalClose / uploadCount;
                    final float thisTypeAvgCloseS = Math.round(thisTypeAvgClose / 10) / 100f;

                    final Long thisTypeAvg = thisTypeAvgWrite + thisTypeAvgClose;
                    final float thisTypeAvgS = Math.round(thisTypeAvg / 10) / 100f;



                    averages.put(uploadType, new Pair(thisTypeAvgWrite, thisTypeAvgClose));

                    Long bytes2 = compressedSizes.get(uploadType);
                    if(uploadType > 0 && bytes2 != null) {
                        long kb = bytes2 / 1024;
                        double mb = ((double)kb) / 1024;
                        System.out.println(String.format("%1$" + 15 + "s", Compressor.getAlg(uploadType)) + ": "+thisTypeAvgWriteS + "s + "+thisTypeAvgCloseS + "s -> " + bytes2 + " bytes ("+mb+" MB)" );
                        str += Compressor.getAlg(uploadType) + ": " + thisTypeAvgWriteS + "s + "+thisTypeAvgCloseS+"s = "+(thisTypeAvgS)+"s / ";
                    }
                }
                System.out.println(str);
            }



            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                out.close();
//                tmpFile.delete();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            socket.close();
            stream.close();
            if(out != null) out.close();
            if(tmpFile != null) tmpFile.delete();

        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (automateUploadRunning) {
                            checkAndAsyncUpload(Compressor.getNextCompressionMode(compressionMode));
//                            int nextCompressionMode = compressionMode + 1;
//                            if (nextCompressionMode >= uploadTimes.size()) nextCompressionMode = 1;
//                            checkAndAsyncUpload(nextCompressionMode);
                        } else {
                            System.err.println("automateUpload not running! stopping loop");
                        }

                    }
                });

            }
        }).start();


    }

    float startX = -1;
    float startY = -1;





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

    private void initUI() {
        textView = (TextView)findViewById(R.id.textView);
        imageView = (ImageView)findViewById(R.id.imageView);
//
//        DownloadWebPageTask task = new DownloadWebPageTask();
//        task.execute(new String[] { "www.yahoo.com" });

        // File outputDir = getCacheDir(); // context being the Activity pointer


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
        ((Button)findViewById(R.id.uploadCompressZipButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downloadedFile == null) {
                    Toast.makeText(MainActivity.this, "Wait for download to complete before uploading", Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(MainActivity.this, "Uploading (ZIP) to btraas.ca....", Toast.LENGTH_LONG).show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
//
                                upload(new FileInputStream(downloadedFile), 2, 4444);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            }
        });
        ((Button)findViewById(R.id.uploadCompressDeflaterButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndAsyncUpload(3);
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


        ((Button)findViewById(R.id.automateButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                automateUploadRunning = !automateUploadRunning;
                if(automateUploadRunning) {
                    checkAndAsyncUpload(1);
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



}
