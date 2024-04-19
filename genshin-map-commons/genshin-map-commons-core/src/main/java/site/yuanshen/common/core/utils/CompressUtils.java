package site.yuanshen.common.core.utils;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * 压缩工具类
 *
 * @author Moment
 */
public class CompressUtils {

    public static byte[] compress(byte[] inputData) throws IOException {
        return gzipCompress(inputData);
    }

    public static byte[] gzipCompress(byte[] inputData) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GzipCompressorOutputStream gzipOS = new GzipCompressorOutputStream(os);
        gzipOS.write(inputData);
        gzipOS.finish();
        gzipOS.close();
        return os.toByteArray();
    }

}
