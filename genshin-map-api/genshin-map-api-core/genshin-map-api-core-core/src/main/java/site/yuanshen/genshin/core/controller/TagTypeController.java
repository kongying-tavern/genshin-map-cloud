package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.TagTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeSearchDto;
import site.yuanshen.data.vo.TagTypeVo;
import site.yuanshen.data.vo.helper.PageAndTypeSearchVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.TagTypeService;

/**
 * 图标标签分类 Controller 层
 *
 * @author Alex Fang
 * @since 2022-10-15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tag_type")
@Tag(name = "tag_type", description = "图标标签分类API")
public class TagTypeController {

    private final TagTypeService tagTypeService;
    private final CacheService cacheService;

    //////////////START:标签分类的API//////////////

    @Operation(summary = "列出分类", description = "列出标签的分类，parentID为-1的时候为列出所有的根分类，isTraverse为1时遍历所有子分类，默认为1，可分页")
    @PostMapping("/get/list")
    public R<PageListVo<TagTypeVo>> listTagType(@RequestBody PageAndTypeSearchVo searchVo) {
        return RUtils.create(
                tagTypeService.listTagType(new PageAndTypeSearchDto(searchVo))
        );
    }

    @Operation(summary = "新增分类", description = "类型id在创建后返回")
    @PutMapping("/add")
    public R<Long> addTagType(@RequestBody TagTypeVo tagTypeVo) {
        cacheService.cleanIconTagCache();
        Long newTagId = tagTypeService.addTagType(new TagTypeDto(tagTypeVo));
        return RUtils.create(newTagId);
    }

    @Operation(summary = "修改分类", description = "由类型ID来定位修改一个分类")
    @PostMapping("/update")
    public R<Boolean> updateTagType(@RequestBody TagTypeVo tagTypeVo) {
        Boolean result = tagTypeService.updateTagType(new TagTypeDto(tagTypeVo));
        if (result) cacheService.cleanIconTagCache();
        return RUtils.create(result);
    }

    @Operation(summary = "删除分类", description = "这个操作会递归删除，请在前端做二次确认")
    @DeleteMapping("/delete/{typeId}")
    public R<Boolean> deleteTagType(@PathVariable("typeId") Long typeId) {
        Boolean result = tagTypeService.deleteTagType(typeId);
        if (result) cacheService.cleanIconTagCache();
        return RUtils.create(result);
    }

    //////////////END:标签分类的API//////////////
}
