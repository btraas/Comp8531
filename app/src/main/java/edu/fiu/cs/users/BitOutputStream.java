package edu.fiu.cs.users;

// BitOutputStream class: Bit-output stream wrapper class.
//
// CONSTRUCTION: with an open OutputStream.
//
// ******************PUBLIC OPERATIONS***********************
// void writeBit( val )        --> Write one bit (0 or 1)
// void writeBits( vald )      --> Write array of bits
// void flush( )               --> Flush buffered bits
// void close( )               --> Close underlying stream

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream
{
    public BitOutputStream( OutputStream os )
    {
        bufferPos = 0;
        buffer = 0;
        out = os;
    }

    public void writeBit( int val ) throws IOException
    {
        buffer = setBit( buffer, bufferPos++, val );
        if( bufferPos == BitUtils.BITS_PER_BYTES )
            flush( );
    }

    public void writeBits( int [ ] val ) throws IOException
    {
        for( int v : val )
            writeBit( v );
    }

    public void flush( ) throws IOException
    {
        if( bufferPos == 0 )
            return;

        out.write( buffer );
        bufferPos = 0;
        buffer = 0;
    }

    public void close( ) throws IOException
    {
        flush( );
        out.close( );
    }

    private int setBit( int pack, int pos, int val )
    {
        if( val == 1 )
            pack |= ( val << pos );
        return pack;
    }

    private OutputStream out;
    private int buffer;
    private int bufferPos;
}