package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.genshin.core.service.CacheService;

import java.util.List;

/**
 * 地区 Controller 层
 *
 * @author Sunosay
 * @since 2022-06-11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cache")
@Tag(name = "cache", description = "缓存API")
public class CacheController {

    private final CacheService cacheService;

    @Operation(summary = "删除标签缓存",description = "list为空则删除所有标签缓存")
    @DeleteMapping("/iconTag")
    public R<Boolean> cleanIconTagCache(@RequestBody List<String> nameList){
        if (nameList.isEmpty()){
            cacheService.cleanIconTagCache();
        }else{
            nameList.forEach(cacheService::cleanIconTagCache);
        }
        return RUtils.create(true);
    }

    @Operation(summary = "删除地区缓存",description = "删除地区缓存")
    @DeleteMapping("/area")
    public R<Boolean> cleanAreaCache(){
        cacheService.cleanAreaCache();
        return RUtils.create(true);
    }

    @Operation(summary = "删除全部物品缓存",description = "删除物品缓存")
    @DeleteMapping("/item")
    public R<Boolean> cleanItemCache(){
        cacheService.cleanItemCache();
        return RUtils.create(true);
    }

    @Operation(summary = "删除全部公用物品缓存",description = "删除公用物品缓存")
    @DeleteMapping("/commonItem")
    public R<Boolean> cleanCommonItemCache(){
        cacheService.cleanCommonItemCache();
        return RUtils.create(true);
    }

    @Operation(summary = "删除全部点位缓存",description = "删除点位缓存")
    @DeleteMapping("/marker")
    public R<Boolean> cleanMarkerCache(){
        cacheService.cleanMarkerCache();
        return RUtils.create(true);
    }
}
