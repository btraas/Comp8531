package edu.fiu.cs.users;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

import weiss.util.PriorityQueue;






// Huffman tree class interface: manipulate huffman coding tree.
//
// CONSTRUCTION: with no parameters or a CharCounter object.
//
// ******************PUBLIC OPERATIONS***********************
// int [ ] getCode( ch )        --> Return code given character
// int getChar( code )          --> Return character given code
// void writeEncodingTable( out ) --> Write coding table to out
// void readEncodingTable( in ) --> Read encoding table from in
// ******************ERRORS**********************************
// Error check for illegal code.

public class HuffmanTree
{
    protected CharCounter theCounts;
    protected HuffNode [ ] theNodes = new HuffNode[ BitUtils.DIFF_BYTES + 1 ];
    protected HuffNode root;

    public HuffmanTree( )
    {
        theCounts = new CharCounter( );
        root = null;
    }

    public HuffmanTree( CharCounter cc )
    {
        theCounts = cc;
        root = null;
        createTree( );
    }

    public static final int ERROR = -3;
    public static final int INCOMPLETE_CODE = -2;
    public static final int END = BitUtils.DIFF_BYTES;

    /**
     * Return the code corresponding to character ch.
     * (The parameter is an int to accomodate EOF).
     * If code is not found, return an array of length 0.
     */
    public int [ ] getCode( int ch )
    {
        HuffNode current = theNodes[ ch ];
        if( current == null )
            return null;

        String v = "";
        HuffNode par = current.parent;

        while ( par != null )
        {
            if( par.left == current )
                v = "0" + v;
            else
                v = "1" + v;
            current = current.parent;
            par = current.parent;
        }

        int [ ] result = new int[ v.length( ) ];
        for( int i = 0; i < result.length; i++ )
            result[ i ] = v.charAt( i ) == '0' ? 0 : 1;

        return result;
    }

    /**
     * Get the character corresponding to code.
     */
    public int getChar( String code )
    {
        HuffNode p = root;
        for( int i = 0; p != null && i < code.length( ); i++ )
            if( code.charAt( i ) == '0' )
                p = p.left;
            else
                p = p.right;

        if( p == null )
            return ERROR;

        return p.value;
    }

    // Write the encoding table using character counts
    /**
     * Writes an encoding table to an output stream.
     * Format is character, count (as bytes).
     * A zero count terminates the encoding table.
     */
    public void writeEncodingTable( DataOutputStream out ) throws IOException
    {
        for( int i = 0; i < BitUtils.DIFF_BYTES; i++ )
        {
            if( theCounts.getCount( i ) > 0 ) // get number of i's. This ends up looking like A4B3
            {
                out.writeByte( i );
                out.writeInt( theCounts.getCount( i ) );
            }
        }
        out.writeByte( 0 );
        out.writeInt( 0 );
    }

    /**
     * Read the encoding table from an input stream in format
     * given above and then construct the Huffman tree.
     * Stream will then be positioned to read compressed data.
     */
    public void readEncodingTable( DataInputStream in ) throws IOException
    {
        for( int i = 0; i < BitUtils.DIFF_BYTES; i++ )
            theCounts.setCount( i, 0 );

        int ch;
        int num;

        for( ; ; )
        {
            ch = in.readByte( );
            num = in.readInt( );
            if( num == 0 )
                break;
            theCounts.setCount( ch, num );
        }

        createTree( );
    }

    /**
     * Construct the Huffman coding tree.
     */
    private void createTree( )
    {
        PriorityQueue<HuffNode> pq = new PriorityQueue<HuffNode>( );

        for( int i = 0; i < BitUtils.DIFF_BYTES; i++ )
            if ( theCounts.getCount( i ) > 0 )
            {
                HuffNode newNode = new HuffNode( i,
                        theCounts.getCount( i ), null, null, null );
                theNodes[ i ] =  newNode;
                pq.add( newNode );
            }

        theNodes[ END ] = new HuffNode( END, 1, null, null, null );
        pq.add( theNodes[ END ] );

        while( pq.size( ) > 1 )
        {
            HuffNode n1 = pq.remove( );
            HuffNode n2 = pq.remove( );
            HuffNode result = new HuffNode( INCOMPLETE_CODE,
                    n1.weight + n2.weight, n1, n2, null );
            n1.parent = n2.parent = result;
            pq.add( result );
        }

        root = pq.element( );
    }


}


