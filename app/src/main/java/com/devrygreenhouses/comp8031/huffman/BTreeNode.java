package com.devrygreenhouses.comp8031.huffman;

public interface BTreeNode<T extends BTreeNode> extends Comparable<T> {
    public T getLeft();
    public T getRight();
    public T getParent();
}
