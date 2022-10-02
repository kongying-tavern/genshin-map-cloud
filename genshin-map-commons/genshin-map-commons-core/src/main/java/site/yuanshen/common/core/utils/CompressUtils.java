package site.yuanshen.common.core.utils;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.*;

/**
 * TODO
 *
 * @author Moment
 */
public class CompressUtils {
    private static final int BUFFER = 8;

    /**
     * @Description: GZIP 数据压缩
     * @author (ljh) @date 2015-4-13 下午6:00:52
     * @param data
     * @return
     * @throws IOException
     * @return byte[]
     * @throws CompressorException
     */
    public static byte[] compress(byte[] data) throws IOException, CompressorException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compress(bais, baos);// 输入流压缩到输出流
        baos.flush();
        baos.close();
        bais.close();
        return baos.toByteArray();
    }

    /**
     * @Description: GZIP 数据压缩
     * @author (ljh) @date 2015-4-14 上午9:26:30
     * @param is
     * @param os
     * @throws IOException
     * @return void
     * @throws CompressorException
     */
    public static void compress(InputStream is, OutputStream os) throws IOException, CompressorException {
        BZip2CompressorOutputStream bzip2OS = new BZip2CompressorOutputStream(os, 8);int count;
        byte[] data = new byte[BUFFER];
        while ((count = is.read(data, 0, data.length)) != -1) {
            bzip2OS.write(data, 0, count);
        }
        bzip2OS.finish();
        bzip2OS.flush();
        bzip2OS.close();
    }


}
