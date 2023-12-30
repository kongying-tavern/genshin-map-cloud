package site.yuanshen.genshin.core.sao;

import io.minio.MinioClient;
import org.springframework.web.multipart.MultipartFile;
import site.yuanshen.common.core.exception.minio.BucketNotFoundException;
import site.yuanshen.common.core.exception.minio.ObjectPutException;

public interface MinioSao {

    /**
     * 创建 MinIO 客户端连接
     */
    MinioClient createClient(String endpoint, String key, String secret);

    /**
     * 检查存储桶是否存在
     */
    boolean bucketExist(MinioClient client, String bucket) throws BucketNotFoundException;

    /**
     * 检查对象是否存在
     */
    boolean objectExists(MinioClient client, String bucket, String object);

    String putObject(MinioClient client, String bucket, String object, MultipartFile file) throws ObjectPutException;
}
