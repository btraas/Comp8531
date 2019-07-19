package com.devrygreenhouses.comp8031.huffman;



import edu.fiu.cs.users.BitOutputStream;
import edu.fiu.cs.users.BitUtils;

import java.io.IOException;
import java.io.OutputStream;


public class DynamicHuffmanOutputStream extends OutputStream {

    private BitOutputStream bitStream;

    private DynamicHuffmanTree dynamicTree = new DynamicHuffmanTree();

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param out the underlying output stream to be assigned to
     *            the field <tt>this.out</tt> for later use, or
     *            <code>null</code> if this instance is to be
     *            created without an underlying stream.
     */
    public DynamicHuffmanOutputStream(OutputStream out) {
        bitStream = new BitOutputStream( out );
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

            //
            int[] code = dynamicTree.getCode( bytes[ i ] & (0xff) );

            bitStream.writeBits( code ); // write code
        }




    }

    public void close() throws IOException
    {
        bitStream.writeBits( dynamicTree.getCode( BitUtils.EOF ) );

        bitStream.close();
    }




}
