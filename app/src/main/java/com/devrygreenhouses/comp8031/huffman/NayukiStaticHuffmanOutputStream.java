package com.devrygreenhouses.comp8031.huffman;

import io.nayuki.BitOutputStream;
import io.nayuki.FrequencyTable;
import io.nayuki.HuffmanEncoder;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class NayukiStaticHuffmanOutputStream extends OutputStream {

    private ByteArrayOutputStream byteOut = new ByteArrayOutputStream( );


    private BitOutputStream bitStream;

    private int[] initFreqs = new int[257];

    private FrequencyTable freqs;
    private HuffmanEncoder enc;

    private int count = 0;

    public NayukiStaticHuffmanOutputStream(OutputStream os) {
        bitStream = new BitOutputStream(new BufferedOutputStream(os));

        Arrays.fill(initFreqs, 1);

        freqs = new FrequencyTable(initFreqs);
        enc = new HuffmanEncoder(bitStream);

        enc.codeTree = freqs.buildCodeTree();

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

        for( int i = 0; i < bytes.length; i++ ) {
           byteOut.write(bytes[i]);
           count++;

           freqs.increment(bytes[i]);
            if (count < 262144 && isPowerOf2(count) || count % 262144 == 0)  // Update code tree
                enc.codeTree = freqs.buildCodeTree();
            if (count % 262144 == 0)  // Reset frequency table
                freqs = new FrequencyTable(initFreqs);
        }


    }

    public void close() throws IOException
    {

        byte [ ] theInput = byteOut.toByteArray( );

        for (byte b : theInput) {
            enc.write(b);
        }

        enc.write(256); // EOF

        bitStream.close();
    }

    private static boolean isPowerOf2(int x) {
        return x > 0 && Integer.bitCount(x) == 1;
    }

}
