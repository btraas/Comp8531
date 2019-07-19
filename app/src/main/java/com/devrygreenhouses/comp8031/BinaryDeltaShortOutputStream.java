package com.devrygreenhouses.comp8031;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static com.devrygreenhouses.comp8031.StringShortOutputStream.getShortsFromLine;

/**
 * Delta encoding, and insignificant digits trimmed (trim to short)
 */
public class BinaryDeltaShortOutputStream extends FilterOutputStream {

    private static final byte BYTE_0_FULL_MESSAGE_INDICATOR = 127;

    private String previousLine = "";
    private short[] previousShorts = new short[3];
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
    public BinaryDeltaShortOutputStream(OutputStream out) {
        super(out);
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
              String lineString = line.toString();
              // process line
              short[] shorts = getShortsFromLine(lineString);

              if(previousLine.isEmpty()) { // don't do delta
//                  String outString = shorts[0]+","+shorts[1]+","+shorts[2]+"\n";


//                  byte[] bytes2 = outString.getBytes(StandardCharsets.UTF_8);
                  byte[] bytes2 = StringShortOutputStream.shortArrayToByteArray(shorts);

                  byte[] bytes3 = new byte[bytes2.length+1];
                  int i2 = 0;
                  bytes3[i2] = BYTE_0_FULL_MESSAGE_INDICATOR;

                  for(byte b2: bytes2) {
                      bytes3[i2++] = b2;
                  }

                  out.write(bytes3, 0, bytes3.length);
                  previousShorts = shorts;
              } else {
                  short[] prevShorts = previousShorts; //getShortsFromLine(previousLine);
                  int[] deltas = new int[3];
                  int[] lengths = new int[3];

                  byte byte0 = 0;
                  int i = 0;
                  for(short s: shorts) {
                      short prev = prevShorts[i++];

                      int delta = s - prev;
                      deltas[i-1] = delta;
                      int abs = Math.abs(delta);

                      if(abs > 128) {
                          int pow = i + (i-1);
                          byte0 += (Math.pow(2, pow));
                          lengths[i-1] = 2;
                      } else if(delta != 0) {
                          int pow = i + (i-1) - 1;
                          byte0 += (Math.pow(2, pow));
                          lengths[i-1] = 1;
                      } else { // s == prev
                          byte0 += 0;
                          lengths[i-1] = 0;
                      }
                  }
                  out.write(new byte[]{byte0}, 0, 1);

                  int j = 0;
                  for(int length: lengths) {
                      short delta = (short)deltas[j++];
                      if(length == 2) {
                          out.write(new byte[]{(byte)(delta>>>8),(byte)(delta&0xFF)}, 0, 2);
                      } else if(length == 1) {
                          out.write(new byte[]{(byte)delta}, 0, 1);
                      }
                  }



              }


              previousLine = lineString;
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
