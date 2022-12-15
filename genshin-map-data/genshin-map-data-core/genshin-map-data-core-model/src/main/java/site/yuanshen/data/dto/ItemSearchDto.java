package site.yuanshen.data.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.vo.ItemSearchVo;

import java.util.List;

/**
 * 物品查询前端封装
 *
 * @author Moment
 * @since 2022-06-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "物品查询前端封装", description = "物品查询前端封装")
public class ItemSearchDto {

    /**
     * 末端物品类型ID列表
     */
    @Schema(title = "末端物品类型ID列表")
    private List<Long> typeIdList;

    /**
     * 末端地区ID列表
     */
    @Schema(title = "末端地区ID列表")
    private List<Long> areaIdList;

    /**
     * 当前页，从0开始
     */
    @Schema(title = "当前页，从0开始")
    private Long current = 0L;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size = 10L;


    /**
     * 是否为测试打点员
     */
    @Schema(title = "是否为测试打点员")
    private Boolean isTestUser = false;

    public ItemSearchDto(ItemSearchVo itemSearchVo) {
        BeanUtils.copyNotNull(itemSearchVo, this);
    }
    @JSONField(serialize = false)
    public Page<Item> getPageEntity() {
        return new Page<>(current, size);
    }

}
