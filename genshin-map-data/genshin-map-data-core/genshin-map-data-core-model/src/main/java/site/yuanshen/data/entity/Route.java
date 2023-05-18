package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.handler.MybatisPlusJsonArrayTypeHandler;
import site.yuanshen.handler.MybatisPlusJsonObjectTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 路线
 *
 * @since 2023-04-23 01:08:53
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "route", autoResultMap = true)
public class Route extends BaseEntity {

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
     * 路线名称
     */
    @TableField("name")
    private String name;

    /**
     * 路线描述
     */
    @TableField("content")
    private String content;

    /**
     * 点位顺序数组
     */
    @TableField(value = "marker_list", typeHandler = MybatisPlusJsonArrayTypeHandler.class)
    private List<Object> markerList;

    /**
     * 显隐等级
     */
    @TableField("hidden_flag")
    private Integer hiddenFlag;

    /**
     * 视频地址
     */
    @TableField("video")
    private String video;

    /**
     * 额外信息
     */
    @TableField(value = "extra", typeHandler = MybatisPlusJsonObjectTypeHandler.class)
    private Map<String, Object> extra;

    /**
     * 创建人昵称
     */
    @TableField("creator_nickname")
    private String creatorNickname;

}
