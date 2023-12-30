package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.web.multipart.MultipartFile;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.vo.ResourceUploadVo;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "资源上传数据封装", description = "资源上传数据封装")
public class ResourceUploadDto {
    /**
     * 文件
     */
    private MultipartFile file;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件地址
     */
    private String fileUrl;

    public ResourceUploadDto(ResourceUploadVo routeVo) {
        BeanUtils.copy(routeVo, this);
    }

    @JSONField(serialize = false)
    public ResourceUploadVo getVo() {
        return BeanUtils.copy(this, ResourceUploadVo.class);
    }
}
