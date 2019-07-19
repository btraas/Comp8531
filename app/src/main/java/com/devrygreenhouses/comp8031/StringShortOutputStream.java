package com.devrygreenhouses.comp8031;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * insignificant digits trimmed (trim to short)
 */
public class StringShortOutputStream extends FilterOutputStream {

    private StringBuilder line = new StringBuilder();

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param out the underlying output stream to be assigned to
     *            the field <tt>this.out</tt> for later use, or
     *            <code>null</code> if this instance is to be
     *            created without an underlying stream.
     */
    public StringShortOutputStream(OutputStream out) {
        super(out);

    }

    public static short[] getShortsFromLine(String line) {
        String[] sensors = line.trim().split(",");
        short[] shorts = new short[sensors.length];
        int i = 0;
        for(String s : sensors) {
            float f = Float.parseFloat(s);
            shorts[i++] = (short)(f * 1000);
        }
        return shorts;
    }

    public static byte[] shortArrayToByteArray(short[] input) {
        byte[] out = new byte[input.length*2];

        int i = 0;
        for(short s: input) {
            out[i++] = (byte)(s>>>8);
            out[i++] = (byte)(s&0xFF);

        }
        return out;
    }

    public static byte[] shortArraysToByteArray(short[][] input) {

        int count = 0;
        for(short[] arr: input) {
            count += arr.length;
        }



        byte[] out = new byte[count*2];

        int i = 0;
        for(short[] array: input) {
            for(short s: array) {
                out[i++] = (byte)(s>>>8);
                out[i++] = (byte)(s&0xFF);
            }


        }
        return out;
    }

    /**
     * Writes a byte to the compressed output stream. This method will
     * block until the byte can be written.
     * @param b the byte to be written
     * @exception IOException if an I/O error has occurred
     */
    public void write(int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte)(b & 0xff);
        write(buf, 0, 1);
    }

    /**
     * Writes an array of bytes to the compressed output stream. This
     * method will block until all the bytes are written.
     * @param bytes the data to be written
     * @param off the start offset of the data
     * @param len the length of the data
     * @exception IOException if an I/O error has occurred
     */
    public void write(byte[] bytes, int off, int len) throws IOException {
        if(off != 0){
            throw new RuntimeException("Invalid offset: "+off);
        }
        for(byte b : bytes) {
            char c = (char)b;
            line.append(c);
            if(c == '\n') {
              // process line
              short[] shorts = getShortsFromLine(line.toString());
              String outString = shorts[0]+","+shorts[1]+","+shorts[2]+"\n";

              byte[] bytes2 = outString.getBytes(StandardCharsets.UTF_8);
              out.write(bytes2, 0, bytes2.length);
              line.setLength(0);
            }
        }


//
//        if (def.finished()) {
//            throw new IOException("write beyond end of stream");
//        }
//        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
//            throw new IndexOutOfBoundsException();
//        } else if (len == 0) {
//            return;
//        }
//        if (!def.finished()) {
//            def.setInput(b, off, len);
//            while (!def.needsInput()) {
//                deflate();
//            }
//        }
    }


}
