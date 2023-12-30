package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "资源上传前端封装", description = "资源上传前端封装")
public class ResourceUploadVo {

    @Schema(title = "文件路径")
    private String filePath;

    @Schema(title = "文件地址")
    private String fileUrl;
}
