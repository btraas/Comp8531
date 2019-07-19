package com.devrygreenhouses.comp8031;

public interface StreamByteCounter extends Comparable<StreamByteCounter> {

    public int getOutputBytes();
    public String getOutputKB();
    public String getOutputMB();
    public String getOutputAuto();

    public String getPercentOf(StreamByteCounter other);

    public String comparisonString(StreamByteCounter o);

    public int compareTo(StreamByteCounter o);
    public boolean equals(Object o);




}
