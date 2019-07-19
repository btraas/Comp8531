package com.devrygreenhouses.comp8031;

import com.devrygreenhouses.comp8031.huffman.DynamicHuffmanOutputStream;
import com.devrygreenhouses.comp8031.huffman.NayukiAdaptiveHuffmanOutputStream;
import com.devrygreenhouses.comp8031.huffman.NayukiStaticHuffmanOutputStream;
import edu.fiu.cs.users.HZIPOutputStream;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public abstract class Compressor {

    public static String getAlg(int compressionMode) {
        String alg;
        switch(compressionMode) {
            case 0: alg = "raw"; break;

            case 1: alg = "gzip (16k)"; break;

            case 2: alg = "Adaptive Huffman (Nayuki)"; break;

            case 3: alg = "Huffman (Nayuki)"; break;

            // FIU.edu huffman: 15 kbps...
            // Adding a buffer wrapper: 600 kbps;
//            case 4: alg = "Buffered Adaptive Huffman (FIU)"; break;
//
//            case 5: alg = "Buffered Huffman (FIU)"; break;


            case 4: alg = "raw"; break;


//            case 2: alg = "dynamic huffman"; break;

            /*

            case 1: alg = "XZ"; break;

//            case 1: alg = "zip (STORED)"; break;

            case 4: alg = "gzip short (binary)"; break;
            case 5: alg = "short (string)"; break;
            case 6: alg = "gzip short (string)"; break;
//            case 6: alg = "delta (trim+delta)"; break;
            case 7: alg = "short (binary)"; break;

            case 8: alg = "delta (binary)"; break;
            case 9: alg = "gzip delta (binary)"; break;



            case 11: alg = "zip (DEFLATED)"; break;
//
//            case 12: alg = "deflate (1)"; break;
//            case 13: alg = "deflate (3)"; break;
//            case 14: alg = "deflate (5)"; break;
//            case 15: alg = "deflate (7)"; break;
            case 16: alg = "deflate (9)"; break;

//            case 17: alg = "gzip (2k)"; break;
//            case 18: alg = "gzip (4k)"; break;
//            case 19: alg = "gzip (8k)"; break;
//            case 20: alg = "gzip (12k)"; break;
            case 21: alg = "gzip (16k)"; break;
//            case 22: alg = "gzip (24k)"; break;
//            case 23: alg = "gzip (32k)"; break;
//            case 24: alg = "gzip (64k)"; break;

//            case 30: alg = "bzip (100k)"; break;
////            case 31: alg = "bzip (300k)"; break;
////            case 32: alg = "bzip (500k)"; break;
////            case 33: alg = "bzip (700k)"; break;
            case 34: alg = "bzip (900k)"; break;
//            case 35: alg = "bzip (32k)"; break;

//            case 40: alg = "snappy (1k)"; break;
//            case 41: alg = "snappy (2k)"; break;
//            case 42: alg = "snappy (4k)"; break;
//            case 43: alg = "snappy (8k)"; break;
//            case 49: alg = "snappy (12k)"; break;
            case 44: alg = "snappy (16k)"; break;
//            case 45: alg = "snappy (32k)"; break;
//            case 46: alg = "snappy (128k)"; break;
//            case 48: alg = "snappy (512k)"; break;
//            case 2: alg = "snappy (2048k)"; break;

//            case 50: alg = "LZMA"; break;

*/



            default: alg = ""+compressionMode;
        }
        return alg;
    }

    public static OutputStream compressOutputStream(int compressionMode, OutputStream out) {


        try {
            switch (compressionMode) {

                case 1: out = new GZIPOutputStream(out, 16384); break;

                case 2: out = new NayukiAdaptiveHuffmanOutputStream(out); break;

//                case 2: out = new DynamicHuffmanOutputStream(out); break;

                // HZIP (FIU.edu): 16kbps
//                case 3: out = new HZIPOutputStream(out); break;
                case 3: out = new NayukiStaticHuffmanOutputStream(out); break;

//                case 4: out = new DynamicHuffmanOutputStream(new BufferedOutputStream(out)); break;
//
//                case 5: out = new HZIPOutputStream(new BufferedOutputStream(out)); break;


                case 4: out = out; break;

//                case 2: out = new DynamicHuffmanOutputStream(out); break;


//                case 1: out = new XZCompressorOutputStream(out); break;
//
//                case 4: out = new BinaryShortOuputStream(new GZIPOutputStream(out)); break;
//                case 5: out = new StringShortOutputStream(out); break;
//                case 6: out = new StringShortOutputStream(new GZIPOutputStream(out)); break;
////                case 6: out = new DeltaShortOutputStream(out); break;
//                case 7: out = new BinaryShortOuputStream(out); break;
//                case 8: out = new BinaryDeltaShortOutputStream(out); break;
//                case 9: out = new BinaryDeltaShortOutputStream(new GZIPOutputStream(out)); break;
//
//                case 11:
//                    ZipEntry entry2 = new ZipEntry("tmp");
//                    entry2.setMethod(ZipEntry.DEFLATED);
//                    out = new ZipOutputStream(out);
//                    try {
//                        ((ZipOutputStream) out).putNextEntry(entry2);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//
////                case 12: out = new DeflaterOutputStream(out, new Deflater(1)); break;
////                case 13: out = new DeflaterOutputStream(out, new Deflater(3)); break;
////                case 14: out = new DeflaterOutputStream(out); break;
////                case 15: out = new DeflaterOutputStream(out, new Deflater(7)); break;
//                case 16: out = new DeflaterOutputStream(out, new Deflater(9)); break;
//
////              case 6: out = new GZIPOutputStream(out, 512); break;
////              case 8: out = new GZIPOutputStream(out, 1024); break;
////                case 17: out = new GZIPOutputStream(out, 2048); break;
////              case 9: out = new GZIPOutputStream(out, 2816); break;
////              case 10: out = new GZIPOutputStream(out, 3072); break;
////                case 18: out = new GZIPOutputStream(out, 4096); break;
////                case 19: out = new GZIPOutputStream(out, 8192); break;
////                case 20: out = new GZIPOutputStream(out, 8192 + 4096); break;   // 12k
//                case 21: out = new GZIPOutputStream(out, 16384); break;         // 16k
////                case 22: out = new GZIPOutputStream(out, 16384 + 8192); break;  // 24k
////                case 23: out = new GZIPOutputStream(out, 32768); break;         // 32k
////                case 24: out = new GZIPOutputStream(out, 65536); break;         // 64k
//
////                case 30: out = new BZip2CompressorOutputStream(out, 1); break;
////                case 31: out = new BZip2CompressorOutputStream(out, 3); break;
////                case 32: out = new BZip2CompressorOutputStream(out, 5); break;
////                case 33: out = new BZip2CompressorOutputStream(out, 7); break;
//                case 34: out = new BZip2CompressorOutputStream(out, 9); break;
////                case 35: out = new BZip2CompressorOutputStream(out, 32768); break;
//
////                case 40: out = new SnappyCompressorOutputStream(out, downloadedFile.length(), 1024); break;
////                case 41: out = new SnappyCompressorOutputStream(out, downloadedFile.length(), 2048); break;
////                case 42: out = new SnappyCompressorOutputStream(out, downloadedFile.length(), 4096); break;
////                case 43: out = new SnappyCompressorOutputStream(out, downloadedFile.length(), 8192); break;
//                case 44: out = new SnappyCompressorOutputStream(out, downloadedFile.length(), 16384); break;
////                case 45: out = new SnappyCompressorOutputStream(out, downloadedFile.length(), 32768); break;
////                case 46: out = new SnappyCompressorOutputStream(out, downloadedFile.length(), 32768 * 4); break; // = "snappy (128k)"; break;
////                case 48: out = new SnappyCompressorOutputStream(out, downloadedFile.length(), 32768 * 16); break; // = "snappy (512k)"; break;
////                case 49: out = new SnappyCompressorOutputStream(out, downloadedFile.length(), 8192 + 4096); break; // = "snappy (12k)"; break;
//
////                case 2: out = new SnappyCompressorOutputStream(out, downloadedFile.length(), 32768 * 64); break; // = "snappy (2048k)"; break; // not sure if this will work!?
//
////                case 50: out = new LZMACompressorOutputStream(out); break;

                default: return null;

            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return out;
    }

    public static int getNextCompressionMode(int currentMode) {
        int m = currentMode+1;
        while(getAlg(m).length() < 3) {
            m++;
            if(m > 99) {
                m = 1;
                break;
            }
        }
        return m;

    }

}
