package com.devrygreenhouses.comp8031.huffman;

import com.devrygreenhouses.comp8031.huffman.BTreePrinter;
import edu.fiu.cs.users.BitUtils;
import edu.fiu.cs.users.HuffNode;
import edu.fiu.cs.users.HuffmanTree;
import org.w3c.dom.Node;
import weiss.util.PriorityQueue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DynamicHuffmanTree {

    // Adaptive/Dynamic tree rules:

    // 1. Each node must either have 2 or 0 children except the root. (Sibling rule)
    // 2. Parent values must be >= child values
    // 3. Values must be >= 0
    // 4. Left < right

    private DynamicCharCounter theCounts;

    protected ArrayList<HuffNode> theNodes = new ArrayList<HuffNode>(BitUtils.DIFF_BYTES + 1);
    protected HuffNode root;

    private HuffNode NYT;

    public HuffNode[] characterMap = new HuffNode[BitUtils.DIFF_BYTES + 1];


    public DynamicHuffmanTree() {
        theCounts = new DynamicCharCounter();

        NYT = new HuffNode(0, 0, null, null, null);
        root = NYT;

        characterMap[0] = NYT;

//        print();
    }

    /*



                          +------------+
                          |            |
                          |     START  +<----------------------------------------------------
                          |            |                                                     |
                          +------+-----+                                                     |
                                 |                                                           |
                                 |                                                           |
                                 |       START                                               |
                                 v                                                           |
                           +-----+-------+                                                   |
+-----------------+        |             |                                                   |
|NYT gives birth  |        |  Seen this  |                                                   |
|to new NYT and   |  NO    |  Symbol     |                                                   |
|external nodes   +<-------+  Before?    |                                                   |
|                 |        |             |                                                   |
+-------+---------+        |             |                                                   |
        |                  +------+------+                                                   |
        |                         |                                                          |
        |                         |                                                          |
        |                         |                                                          |
        |                         |  YES                                                     |
+-------+--------+                |                                                          |
| Increment new  |                v                                                          |
| leaf weight and|       +--------+----------+                                               |
| old NYT nodes  |       |                   |                                               |
+-------+--------+       |    GO to leaf     |                                               |
        |                |    whose value    |                                               |
        |                |    matches this   |                                               |
        |                |    symbol         |                                               |
        v                |                   |                                               |
 +------+-----------+    |                   |                                               |
 | Go to old NYT    |    +--------+----------+                                               |
 |                  |             |                                                          |
 |                  |             v                                                          |
 +--------+---------+    +--------+----------+            +-------------------+              |
          |              |                   |            |                   |              |
          |              |   Is this the max |    NO      |                   |              |
          |              |   ordered node in |            |      Swap with    |              |
          |              |   its weight      +------------+      Highest      |              |
          |              |   class?          |            |      ordered node |              |
          |              |                   |            |      in weight    |              |
          |              +---------+---------+            |      class        |              |
          |                        |                      |                   |              |
          |                        | YES                  +---------+---------+              |
          |                        |                                |                        |
          |              +---------v---------+                      |                        |
          |              |                   |                      |                        |
          +------------->+    Increment weight<---------------------+                        |
                         |                   |                                               |
                         +---------+---------+                                               |
                                   |                                                         |
                                   |                                                         |
                         +---------v---------+                                               |
                         |                   |    NO                                         |
                         | is this root node?+-----------------------------------------------+
                         |                   |
                         +---------+---------+
                                   |
                                   |  YES
                                   v
                           +-------+---------+
                           |     END         |
                           |                 |
                           |                 |
                           +-----------------+



     */

    public int[] getCode(int ch) {

        // 1.

        HuffNode node = characterMap[ch];
        int[] code = null;


        if(node == null) {
            // This is a new symbol

//            System.out.println(" new symbol: "+(char)ch+"!");


            // NYT gives birth to new NYT and symbol node

            node = new HuffNode(ch, 0, null, null, NYT);

            HuffNode newNYT = new HuffNode(0, 0, null, null, NYT);

            node.weight++;
            NYT.weight++;
            NYT.left = newNYT;
            NYT.right = node;

            if(NYT.parent != null)
                NYT.parent.incrementUpTree();

            NYT = newNYT;
            characterMap[ch] = node;

            return new int[] {0,ch}; // 0 = NYT, indicates raw value incoming


        } else {
            // we've seen this symbol before.

            // Go to leaf who's value is this symbol

            // Is this the max ordered node in it's weight class?


            code = node.getCode();
            node.incrementUpTree();


        }

//        print();

        // add to tree, then



        return code;

//        return _getCode(ch);
    }


    public void print() {
        BTreePrinter.printNode(root);
    }



    public static final int ERROR = -3;
    public static final int INCOMPLETE_CODE = -2;
    public static final int END = BitUtils.DIFF_BYTES;



    /**
     * Get the character corresponding to code.
     */
    public int getChar( String code )
    {
        HuffNode p = root;
        for( int i = 0; p != null && i < code.length( ); i++ )
            if( code.charAt( i ) == '0' )
                p = p.getLeft();
            else
                p = p.getRight();

        if( p == null )
            return ERROR;

        return p.value;
    }


    /**
     * Construct the Huffman coding tree.
     *
     * This algorithm is useful only in static Huffman coding.
     */
    private void createTree( )
    {
        PriorityQueue<HuffNode> pq = new PriorityQueue<HuffNode>( );

        for( int i = 0; i < BitUtils.DIFF_BYTES; i++ )
            if ( theCounts.getCount( i ) > 0 )
            {
                HuffNode newNode = new HuffNode( i, theCounts.getCount( i ), null, null, null );
                theNodes.add(i, newNode);
                pq.add( newNode );
            }

        theNodes.add(END, new HuffNode( END, 1, null, null, null ));
        pq.add( theNodes.get(END) );

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


