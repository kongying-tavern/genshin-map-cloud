package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.data.enums.HistoryEditType;

import java.time.LocalDateTime;

/**
 * 历史操作表
 *
 * @since 2023-04-23 01:08:53
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("history")
public class History extends BaseEntity {

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
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * MD5
     */
    @TableField("md5")
    private String md5;

    /**
     * 原ID
     */
    @TableField("t_id")
    private Long tId;

    /**
     * 操作数据类型;1地区; 2图标; 3物品; 4点位; 5标签
     */
    @TableField("type")
    private Integer type;

    /**
     * IPv4
     */
    @TableField("ipv4")
    private String ipv4;

    /**
     * 修改类型
     */
    @TableField(value = "edit_type", typeHandler = MybatisEnumTypeHandler.class)
    private HistoryEditType editType;

}
