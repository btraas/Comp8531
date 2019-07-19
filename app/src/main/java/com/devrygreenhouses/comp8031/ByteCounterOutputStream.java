package com.devrygreenhouses.comp8031;

import java.io.IOException;
import java.io.OutputStream;

public class ByteCounterOutputStream extends OutputStream implements StreamByteCounter {

    private OutputStream os;

    public ByteCounterOutputStream(OutputStream os) {
        this.os = os;
    }


    long publishTime = 0;

    private int byteCount = 0;


    @Override
    public void write(int b) throws IOException {
        os.write(b);
        byteCount++;

        publish();

    }

    @Override
    public void write(byte[] b) throws IOException {
        os.write(b);
        byteCount += b.length;

        publish();

    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        os.write(b, off, len);
        byteCount += b.length;

        publish();
    }

    public void publish() {
        if(System.currentTimeMillis() > (publishTime + 1000)) {
            publishTime = System.currentTimeMillis();
            System.out.println(getOutputAuto() + " written");
        }
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        os.close();
    }


    @Override
    public int getOutputBytes() {
        return byteCount;
    }

    @Override
    public String getOutputKB() {
        double kb = ((long)Math.round(byteCount / 100f)) / 10.24;
        return String.format("%.2f KB", kb);

    }

    @Override
    public String getOutputMB() {
        double mb = ((long)Math.round(byteCount / 100000f)) / 10.24;
        return String.format("%.2f MB", mb);
    }

    @Override
    public String getOutputAuto() {
        if(getOutputBytes() < 2000) return getOutputBytes() + " bytes";
        if(getOutputBytes() < 2000000) return getOutputKB();
        else return getOutputMB();
    }


    @Override
    public String getPercentOf(StreamByteCounter other) {
        double pct = ((long)Math.round(((float) this.getOutputBytes() / other.getOutputBytes()) * 1000f)) / 10f;
        String pctString = String.format("%.1f", pct);
        return pctString;
    }

    @Override
    public String comparisonString(StreamByteCounter previous) {

        String pct = getPercentOf(previous);

        return "Compressed "+previous.getOutputAuto()+" into " + getOutputAuto() + " ("+pct+"% of original)";
    }

    @Override
    public int compareTo(StreamByteCounter o) {
        if(o == null) return 0;
        if(!(o instanceof  ByteCounterOutputStream)) return 0;

        if(((ByteCounterOutputStream) o).byteCount == this.byteCount) return 0;
        return ((ByteCounterOutputStream) o).byteCount > this.byteCount ? 1 : -1;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) return true;
        if(o == null) return false;
        if(!(o instanceof  ByteCounterOutputStream)) return false;

        return ((ByteCounterOutputStream) o).byteCount == this.byteCount;
    }

    @Override
    public int hashCode() {
        return byteCount + 253;
    }

}
