package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.vo.IconVo;
import site.yuanshen.data.vo.SysUserSmallVo;
import site.yuanshen.data.vo.SysUserVo;

import java.sql.Timestamp;
import java.time.LocalDateTime;


/**
 * 图标主数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Icon数据封装", description = "图标主表数据封装")
public class IconDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 创建人
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

    /**
     * 图标名称
     */
    private String name;

    /**
     * 图标url
     */
    private String url;

    public IconDto(Icon icon) {
        BeanUtils.copy(icon, this);
    }

    public IconDto(IconVo iconVo) {
        BeanUtils.copy(iconVo, this);
    }

    @JSONField(serialize = false)
    public Icon getEntity() {
        return BeanUtils.copy(this, Icon.class);
    }

    @JSONField(serialize = false)
    public IconVo getVo() {
        return BeanUtils.copy(this, IconVo.class);
    }

}
