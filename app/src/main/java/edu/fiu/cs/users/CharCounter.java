package edu.fiu.cs.users;



// CharCounter class: A character counting class.
//
// CONSTRUCTION: with no parameters or an open InputStream.
//
// ******************PUBLIC OPERATIONS***********************
// int getCount( ch )           --> Return # occurrences of ch
// void setCount( ch, count )   --> Set # occurrences of ch
// ******************ERRORS**********************************
// No error checks.

import java.io.IOException;
import java.io.InputStream;

public class CharCounter
{
    public CharCounter( )
    {
    }

    public CharCounter( InputStream input ) throws IOException
    {
        int ch;
        while( ( ch = input.read( ) ) != -1 )
            theCounts[ ch ]++;
    }

    public int getCount( int ch )
    {
        return theCounts[ ch & 0xff ];
    }

    public void setCount( int ch, int count )
    {
        theCounts[ ch & 0xff ] = count;
    }

    private int [ ] theCounts = new int[ BitUtils.DIFF_BYTES + 1 ];
}