package site.yuanshen.common.core.utils;

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
        DeflaterOutputStream gzipOS = new DeflaterOutputStream(os, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        gzipOS.write(inputData);
        gzipOS.finish();
        gzipOS.close();
        return os.toByteArray();
    }

}
