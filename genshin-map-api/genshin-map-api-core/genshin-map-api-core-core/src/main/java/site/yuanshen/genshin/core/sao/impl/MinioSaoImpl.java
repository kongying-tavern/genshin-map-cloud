package site.yuanshen.genshin.core.sao.impl;

import io.minio.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.yuanshen.common.core.exception.minio.BucketNotFoundException;
import site.yuanshen.common.core.exception.minio.ObjectPutException;
import site.yuanshen.genshin.core.sao.MinioSao;

@Service
public class MinioSaoImpl implements MinioSao {

    @Override
    public MinioClient createClient(String endpoint, String key, String secret) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(key, secret)
                .credentialsProvider(null)
                .build();
        return minioClient;
    }

    @Override
    public boolean bucketExist(MinioClient client, String bucket) throws BucketNotFoundException {
        try {
            boolean found = client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );
            return found;
        } catch (Exception e) {
            throw new BucketNotFoundException();
        }
    }

    @Override
    public boolean objectExists(MinioClient client, String bucket, String object) {
        try {
            StatObjectResponse objectStat = client.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .build()
            );
            if(objectStat == null) return false;
            return objectStat.size() >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String putObject(MinioClient client, String bucket, String object, MultipartFile file) throws ObjectPutException {
        if(file == null) {
            throw new ObjectPutException("上传文件不能为空");
        }

        try {
            ObjectWriteResponse objectRes = client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .stream(file.getInputStream(), -1, 10485760)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectRes.object();
        } catch(Exception e) {
            throw new ObjectPutException();
        }
    }
}
