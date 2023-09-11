package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.TagDto;
import site.yuanshen.data.dto.TagSearchDto;
import site.yuanshen.data.vo.TagSearchVo;
import site.yuanshen.data.vo.TagVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.TagService;
import site.yuanshen.genshin.core.service.UserAppenderService;

/**
 * 图标标签 Controller 层
 *
 * @author Moment
 * @since 2022-06-02
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tag")
@Tag(name = "tag", description = "图标标签API")
public class TagController {

    private final TagService tagService;
    private final CacheService cacheService;

    //////////////START:标签本身的API//////////////

    @Operation(summary = "列出标签", description = "可按照分类进行查询，也可给出需要查询url的tag名称列表，可分页")
    @PostMapping("/get/list")
    public R<PageListVo<TagVo>> listTag(@RequestBody TagSearchVo tagSearchVo) {
        R<PageListVo<TagVo>> result = RUtils.create(
                tagService.listTag(new TagSearchDto(tagSearchVo))
        );
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, TagVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, TagVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "获取单个标签信息", description = "获取单个标签信息")
    @PostMapping("/get/single/{name}")
    public R<TagVo> getTag(@PathVariable("name") String name) {
        R<TagVo> result = RUtils.create(
                tagService.getTag(name)
        );
        UserAppenderService.appendUser(result, result.getData(), false, TagVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData(), false, TagVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "修改标签关联", description = "将标签关联到另一个图标上")
    @PostMapping("/{tagName}/{iconId}")
    public R<Boolean> updateTag(@PathVariable("tagName") String tagName, @PathVariable("iconId") Long iconId) {
        Boolean result = tagService.updateTag(tagName, iconId);
        cacheService.cleanIconTagCache(tagName);
        return RUtils.create(result);
    }

    @Operation(summary = "修改标签的分类信息", description = "本接口仅在后台使用，故分离出来")
    @PostMapping("/updateType")
    public R<Boolean> updateTypeInTag(@RequestBody TagVo tagVo) {
        Boolean result = tagService.updateTypeInTag(tagVo);
        cacheService.cleanIconTagCache(tagVo.getTag());
        return RUtils.create(result);
    }

    @Operation(summary = "创建标签", description = "只创建一个空标签")
    @PutMapping("/{tagName}")
    public R<Boolean> createTag(@PathVariable("tagName") String tagName) {
        Boolean result = tagService.createTag(tagName);
        if (result) {
            cacheService.cleanIconTagCache(tagName);
        }
        return RUtils.create(result);
    }

    @Operation(summary = "删除标签", description = "需要确保已经没有条目在使用这个标签，否则会删除失败")
    @DeleteMapping("/{tagName}")
    public R<Boolean> deleteTag(@PathVariable("tagName") String tagName) {
        Boolean result = tagService.deleteTag(tagName);
        cacheService.cleanIconTagCache(tagName);
        return RUtils.create(result);
    }

    //////////////END:标签本身的API//////////////
}
