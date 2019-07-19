package edu.fiu.cs.users;


import com.devrygreenhouses.comp8031.huffman.BTreeNode;
import org.jetbrains.annotations.NotNull;

// Basic node in a Huffman coding tree.
public class HuffNode implements Comparable<HuffNode>, BTreeNode<HuffNode>
{
    public int value;
    public int weight;

    public int compareTo( HuffNode rhs )
    {
        return weight - rhs.weight;
    }

    public HuffNode left;
    public HuffNode right;
    public HuffNode parent;

    private int[] code = null;

    public HuffNode( int v, int w, HuffNode lt, HuffNode rt, HuffNode pt )
    {
        value = v; weight = w; left = lt; right = rt; parent = pt;
    }

    public HuffNode getLeft() {
        return left;
    }
    public HuffNode getRight() {
        return right;
    }
    public HuffNode getParent() {
        return parent;
    }

    /**
     * Recursively increment to the root node
     *
     * Not thread safe!
     */
    public void incrementUpTree() {

        if(parent != null) {
            if(weight >= getSibling().weight) {
                this.code = null;
//                System.err.println("Clearing code for node " + this.value + " (weight="+weight+")");
                parent.swapChildren();
            }
        }

        this.weight++;
        if(parent != null) {
            parent.incrementUpTree();
        }
    }

    public boolean isLeft() {
        if(parent == null) return false;
        return parent.left == this;
    }

    public HuffNode getSibling() {
        if(parent == null) return null;

        if(parent.left == this) return parent.right;
        else return parent.left;
    }

    public void swapChildren() {
        HuffNode tmp = left;
        left = right;
        right = tmp;
    }

    public int[] getCode() {
        if(this.code == null) {
            this.calculateCode();
        }
        return this.code;
    }

    private void calculateCode() {
        String v = "";

        HuffNode node = this;
        HuffNode _parent;
        while((_parent = node.getParent()) != null) {
            if(node.isLeft()) {
                v = "0" + v;
            } else {
                v = "1" + v;
            }
            node = _parent;
        }

        int [ ] result = new int[ v.length( ) ];
        for( int i = 0; i < result.length; i++ )
            result[ i ] = v.charAt( i ) == '0' ? 0 : 1;


        this.code = result;
//        System.out.println("HuffNode " + (char)value + " calculated code: " + v);
    }

    @NotNull
    public String toString() {
        return value + " (x" + weight + ")";
    }
}