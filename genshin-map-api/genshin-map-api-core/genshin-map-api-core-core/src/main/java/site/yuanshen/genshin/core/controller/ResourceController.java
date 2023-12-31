package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.ResourceUploadDto;
import site.yuanshen.data.vo.ResourceUploadVo;
import site.yuanshen.genshin.core.service.ResourceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/res")
@Tag(name = "resource", description = "资源API")
public class ResourceController {

    private final ResourceService resourceService;

    @PutMapping("/upload/image")
    @Operation(summary = "上传图片", description = "上传图片至图床并返回访问地址")
    public R<ResourceUploadVo> uploadImage(@RequestParam(value = "file", required = false) MultipartFile file, @ModelAttribute ResourceUploadVo uploadVo) {
        R<ResourceUploadVo> result = RUtils.create(
                resourceService.uploadImage(
                        (new ResourceUploadDto(uploadVo)).withFile(file)
                )
        );
        return result;
    }
}
