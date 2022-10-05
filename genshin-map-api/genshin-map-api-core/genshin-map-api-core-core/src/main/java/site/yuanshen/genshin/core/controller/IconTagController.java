package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.TagDto;
import site.yuanshen.data.dto.TagSearchDto;
import site.yuanshen.data.dto.TagTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.vo.TagSearchVo;
import site.yuanshen.data.vo.TagTypeVo;
import site.yuanshen.data.vo.TagVo;
import site.yuanshen.data.vo.helper.PageAndTypeListVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.dao.IconTagDao;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.IconTagService;

/**
 * 图标标签 Controller 层
 *
 * @author Moment
 * @since 2022-06-02
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/tag")
@Tag(name = "tag", description = "图标标签API")
public class IconTagController {

    private final IconTagService iconTagService;
    private final IconTagDao iconTagDao;
    private final CacheService cacheService;

    //////////////START:标签本身的API//////////////

    @Operation(summary = "列出标签", description = "可按照分类进行查询，也可给出需要查询url的tag名称列表，可分页")
    @PostMapping("/get/list")
    public R<PageListVo<TagVo>> listTag(@RequestBody TagSearchVo tagSearchVo) {
        return RUtils.create(
                iconTagService.listTag(new TagSearchDto(tagSearchVo))
        );
    }

    @Operation(summary = "获取所有标签信息的bz2压缩", description = "查询所有标签信息，返回bz2压缩格式的byte数组")
    @GetMapping("/get/all_bz2")
    public byte[] listAllTagBz2() {
        return iconTagDao.listAllTagBz2();
    }

    @Operation(summary = "返回所有标签信息bz2的md5", description = "返回所有标签信息bz2的md5")
    @GetMapping("/get/all_bz2_md5")
    public R<String> listAllTagBz2Md5() {
        return RUtils.create(
                iconTagDao.listAllTagBz2Md5()
        );
    }

    @Operation(summary = "获取单个标签信息", description = "获取单个标签信息")
    @PostMapping("/get/single/{name}")
    public R<TagVo> getTag(@PathVariable("name") String name) {
        return RUtils.create(
                iconTagService.getTag(name).getVo()
        );
    }

    @Operation(summary = "修改标签关联", description = "将标签关联到另一个图标上")
    @PostMapping("/{tagName}/{iconId}")
    public R<Boolean> updateTag(@PathVariable("tagName") String tagName, @PathVariable("iconId") Long iconId) {
        Boolean result = iconTagService.updateTag(tagName, iconId);
        cacheService.cleanIconTagCache(tagName);
        return RUtils.create(result);
    }

    @Operation(summary = "修改标签的分类信息", description = "本接口仅在后台使用，故分离出来")
    @PostMapping("/updateType")
    public R<Boolean> updateTypeInTag(@RequestBody TagVo tagVo) {
        Boolean result = iconTagService.updateTypeInTag(new TagDto(tagVo));
        cacheService.cleanIconTagCache(tagVo.getTag());
        return RUtils.create(result);
    }

    @Operation(summary = "创建标签", description = "只创建一个空标签")
    @PutMapping("/{tagName}")
    public R<Boolean> createTag(@PathVariable("tagName") String tagName) {
        Boolean result = iconTagService.createTag(tagName);
        if (result) {
            cacheService.cleanIconTagCache(tagName);
        }
        return RUtils.create(result);
    }

    @Operation(summary = "删除标签", description = "需要确保已经没有条目在使用这个标签，否则会删除失败")
    @DeleteMapping("/{tagName}")
    public R<Boolean> deleteTag(@PathVariable("tagName") String tagName) {
        Boolean result = iconTagService.deleteTag(tagName);
        cacheService.cleanIconTagCache(tagName);
        return RUtils.create(result);
    }

    //////////////END:标签本身的API//////////////

    //////////////START:标签分类的API//////////////

    @Operation(summary = "列出分类", description = "列出标签的分类，parentID为-1的时候为列出所有的根分类，isTraverse为1时遍历所有子分类，默认为1，可分页")
    @PostMapping("/get/type/list")
    public R<PageListVo<TagTypeVo>> listTagType(@RequestBody PageAndTypeListVo searchVo) {
        return RUtils.create(
                iconTagService.listTagType(new PageAndTypeListDto(searchVo))
        );
    }

    @Operation(summary = "新增分类", description = "类型id在创建后返回")
    @PutMapping("/type")
    public R<Long> addTagType(@RequestBody TagTypeVo tagTypeVo) {
        cacheService.cleanIconTagCache();
        Long newTagId = iconTagService.addTagType(new TagTypeDto(tagTypeVo));
        return RUtils.create(newTagId);
    }

    @Operation(summary = "修改分类", description = "由类型ID来定位修改一个分类")
    @PostMapping("/type")
    public R<Boolean> updateTagType(@RequestBody TagTypeVo tagTypeVo) {
        Boolean result = iconTagService.updateTagType(new TagTypeDto(tagTypeVo));
        if (result) cacheService.cleanIconTagCache();
        return RUtils.create(result);
    }

    @Operation(summary = "删除分类", description = "这个操作会递归删除，请在前端做二次确认")
    @DeleteMapping("/type/{typeId}")
    public R<Boolean> deleteTagType(@PathVariable("typeId") Long typeId) {
        Boolean result = iconTagService.deleteTagType(typeId);
        if (result) cacheService.cleanIconTagCache();
        return RUtils.create(result);
    }

    //////////////END:标签分类的API//////////////
}
