package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.handler.MybatisPlusJsonArrayTypeHandler;
import site.yuanshen.handler.MybatisPlusJsonObjectTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 点位关联
 *
 * @since 2023-10-20 11:07:15
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("marker_linkage")
public class MarkerLinkage extends BaseEntity {

    /**
     * 乐观锁
     */
    @TableField(value = "version", fill = FieldFill.INSERT_UPDATE)
    @Version
    private Long version;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 更新人
     */
    @TableField(value = "updater_id", fill = FieldFill.INSERT_UPDATE)
    private Long updaterId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 组ID
     */
    @TableField("group_id")
    private String groupId;

    /**
     * 起始点点位ID;会根据是否反向与 to_id 交换
     */
    @TableField("from_id")
    private Long fromId;

    /**
     * 终止点点位ID;会根据是否反向与 from_id 交换
     */
    @TableField("to_id")
    private Long toId;

    /**
     * 关联操作类型
     */
    @TableField("link_action")
    private String linkAction;

    /**
     * 是否反向
     */
    @TableField("link_reverse")
    private Boolean linkReverse;

    /**
     * 路线
     */
    @TableField(value = "path", typeHandler = MybatisPlusJsonArrayTypeHandler.class)
    private List<Object> path;

    /**
     * 额外数据
     */
    @TableField(value = "extra", typeHandler = MybatisPlusJsonObjectTypeHandler.class)
    private Map<String, Object> extra;

}
