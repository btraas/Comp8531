package com.devrygreenhouses.comp8031.huffman;

import edu.fiu.cs.users.BitUtils;

public class DynamicCharCounter {

    private int [ ] theCounts = new int[ BitUtils.DIFF_BYTES + 1 ];


    public int getCount( int ch )
    {
        return theCounts[ ch & 0xff ];
    }

    public void addCount( int ch ) {
        setCount(ch, getCount(ch) + 1);
    }

    private void setCount( int ch, int count )
    {
        theCounts[ ch & 0xff ] = count;
    }

}
