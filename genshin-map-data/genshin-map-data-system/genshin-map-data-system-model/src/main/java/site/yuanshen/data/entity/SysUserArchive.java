package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.handler.MybatisPlusJsonArrayTypeHandler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户存档表
 *
 * @since 2023-04-23 01:08:53
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_archive", autoResultMap = true)
public class SysUserArchive extends BaseEntity {

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
     * 存档名称
     */
    @TableField("name")
    private String name;

    /**
     * 槽位顺序
     */
    @TableField("slot_index")
    private Integer slotIndex;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 存档信息
     */
    @TableField(value = "data", typeHandler = MybatisPlusJsonArrayTypeHandler.class)
    private List<Object> data;

}
