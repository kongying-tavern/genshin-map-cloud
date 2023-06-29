package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Area;
import site.yuanshen.data.vo.AreaVo;
import site.yuanshen.data.vo.SysUserVo;

import java.time.LocalDateTime;


/**
 * 地区主数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Area数据封装", description = "地区主表数据封装")
public class AreaDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新人信息
     */
    private SysUserVo updater;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 地区代码
     */
    private String code;

    /**
     * 地区说明
     */
    private String content;

    /**
     * 图标标签
     */
    private String iconTag;

    /**
     * 父级地区ID（无父级则为-1）
     */
    private Long parentId;

    /**
     * 是否为末端地区
     */
    private Boolean isFinal;

    /**
     * 权限屏蔽标记
     */
    private Integer hiddenFlag;

    /**
     * 额外标记;低位第一位：前台是否显示
     */
    private Integer specialFlag;

    /**
     * 排序
     */
    private Integer sortIndex;

    public AreaDto(Area area) {
        BeanUtils.copy(area, this);
    }

    public AreaDto(AreaVo areaVo) {
        BeanUtils.copy(areaVo, this);
    }

    @JSONField(serialize = false)
    public Area getEntity() {
        return BeanUtils.copy(this, Area.class);
    }

    @JSONField(serialize = false)
    public AreaVo getVo() {
        return BeanUtils.copy(this, AreaVo.class);
    }

}