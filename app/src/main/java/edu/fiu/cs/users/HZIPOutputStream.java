package edu.fiu.cs.users;

import java.io.IOException;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Writes to HZIPOutputStream are compressed and
 * sent to the output stream being wrapped.
 * No writing is actually done until close.
 */
public class HZIPOutputStream extends OutputStream
{

    private ByteArrayOutputStream byteOut = new ByteArrayOutputStream( );
    private DataOutputStream dout;

    int bitCount = 0;

    public HZIPOutputStream( OutputStream out ) throws IOException
    {
        dout = new DataOutputStream( out );
    }

    public void write( int ch ) throws IOException
    {
        byteOut.write( ch );
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
        for(byte b: bytes) {
            byteOut.write(b);
        }
    }

    public void close( ) throws IOException
    {
        byte [ ] theInput = byteOut.toByteArray( );
        ByteArrayInputStream byteIn = new ByteArrayInputStream( theInput );

        CharCounter countObj = new CharCounter( byteIn );
        byteIn.close( );

        HuffmanTree codeTree = new HuffmanTree( countObj );

        // 1. Write tree to output stream
        codeTree.writeEncodingTable( dout );

        BitOutputStream bout = new BitOutputStream( dout );

        for( int i = 0; i < theInput.length; i++ ) {

            // 2. Read code from tree
            int[] code = codeTree.getCode( theInput[ i ] & (0xff) );

            bout.writeBits( code ); // write code

            bitCount += code.length;
        }

        bout.writeBits( codeTree.getCode( BitUtils.EOF ) );

        bitCount += codeTree.getCode( BitUtils.EOF ).length;

        bout.close( );
        byteOut.close( );
    }

}