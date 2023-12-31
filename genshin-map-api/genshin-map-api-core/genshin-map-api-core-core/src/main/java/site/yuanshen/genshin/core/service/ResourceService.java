package site.yuanshen.genshin.core.service;

import cn.hutool.core.util.StrUtil;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.exception.minio.BucketNotFoundException;
import site.yuanshen.common.core.exception.minio.ObjectPutException;
import site.yuanshen.common.core.utils.TemplateUtils;
import site.yuanshen.data.dto.ResourceUploadDto;
import site.yuanshen.data.vo.ResourceUploadVo;
import site.yuanshen.genshin.core.sao.MinioSao;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResourceService {

    @Value("${image.minio.key}")
    private String minioKey = "";
    @Value("${image.minio.secret}")
    private String minioSecret = "";
    @Value("${image.minio.endpoint}")
    private String minioEndpoint = "";
    @Value("${image.minio.bucket}")
    private String minioBucket = "";
    @Value("${image.minio.static-url-template}")
    private String minioUrlTemplate = "";

    private final MinioSao minioSao;

    public ResourceUploadVo uploadImage(ResourceUploadDto uploadDto) {
        final ResourceUploadVo fileInfo = new ResourceUploadVo();

        final MultipartFile file = uploadDto.getFile();
        if(file == null) {
            throw new GenshinApiException("请上传有效图片文件");
        }
        if(!StrUtil.isAllNotBlank(minioEndpoint, minioKey, minioSecret, minioBucket)) {
            throw new GenshinApiException("服务器参数错误，无法上传图片");
        }
        final String filePath = StrUtil.blankToDefault(uploadDto.getFilePath(), "");
        if(StrUtil.isBlank(filePath)) {
            throw new GenshinApiException("文件路径不能为空");
        }

        try {
            MinioClient minioClient = minioSao.createClient(minioEndpoint, minioKey, minioSecret);
            boolean bucketFound = minioSao.bucketExist(minioClient, minioBucket);
            if (!bucketFound) {
                throw new BucketNotFoundException();
            }

            boolean objectFound = minioSao.objectExists(minioClient, minioBucket, filePath);
            if (objectFound) {
                throw new RuntimeException("当前文件已存在，请更换文件路径");
            }

            String objectPutPath = minioSao.putObject(minioClient, minioBucket, filePath, file);
            objectPutPath = StrUtil.blankToDefault(objectPutPath, "");
            if(StrUtil.isBlank(objectPutPath)) {
                throw new ObjectPutException();
            }

            // 处理返回数据
            final String outputEndpointParam = StrUtil.removeSuffix(minioEndpoint, "/");
            final String outputPathParam = StrUtil.removePrefix(objectPutPath, "/");
            final String outputPathFullParam = minioBucket + "/" + outputPathParam;
            String outputUrl = "";
            Map<String, Object> outputParams = new HashMap<>(){{
                put("entrypoint", outputEndpointParam);
                put("bucket", minioBucket);
                put("path", outputPathParam);
                put("fullPath", outputPathFullParam);
            }};
            String outputUrlTpl = "";
            if(StrUtil.isBlank(minioUrlTemplate)) {
                outputUrlTpl = "[[entrypoint]]/[[bucket]]/[[path]]";
            } else {
                outputUrlTpl = minioUrlTemplate;
            }
            outputUrlTpl = outputUrlTpl.replace("[[", "${").replace("]]", "}");
            outputUrl = TemplateUtils.execTemplate(outputUrlTpl, outputParams);

            fileInfo.setFilePath(outputPathParam);
            fileInfo.setFileUrl(outputUrl);
        } catch (BucketNotFoundException e) {
            throw new GenshinApiException("存储空间不存在，无法上传文件");
        } catch (ObjectPutException e) {
            throw new GenshinApiException(StrUtil.blankToDefault(e.getMessage(), "文件上传失败"));
        } catch(RuntimeException e) {
            throw new GenshinApiException(e.getMessage());
        }

        return fileInfo;
    }
}
