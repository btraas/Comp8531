package com.devrygreenhouses.comp8031.huffman;


import com.devrygreenhouses.comp8031.StringShortOutputStream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.devrygreenhouses.comp8031.StringShortOutputStream.getShortsFromLine;

/**
 * insignificant digits trimmed (trim to short)
 */
public class HuffmanOutputStream extends FilterOutputStream {

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
    public HuffmanOutputStream(OutputStream out) {
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
                // process line
                short[] shorts = getShortsFromLine(line.toString());

                byte[] bytes2 = StringShortOutputStream.shortArrayToByteArray(shorts); //StringShortOutputStream.shortArraysToByteArray(new short[][]{shorts});
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
