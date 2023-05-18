package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.handler.MybatisPlusJsonObjectTypeHandler;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 点位主表
 *
 * @since 2023-04-23 01:08:53
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "marker", autoResultMap = true)
public class Marker extends BaseEntity {

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
     * 点位签戳（用于兼容旧点位ID）
     */
    @TableField("marker_stamp")
    private String markerStamp;

    /**
     * 点位名称
     */
    @TableField("marker_title")
    private String markerTitle;

    /**
     * 点位坐标
     */
    @TableField("position")
    private String position;

    /**
     * 点位说明
     */
    @TableField("content")
    private String content;

    /**
     * 点位图片
     */
    @TableField("picture")
    private String picture;

    /**
     * 点位初始标记者
     */
    @TableField("marker_creator_id")
    private Long markerCreatorId;

    /**
     * 点位图片上传者
     */
    @TableField("picture_creator_id")
    private Long pictureCreatorId;

    /**
     * 点位视频
     */
    @TableField("video_path")
    private String videoPath;

    /**
     * 点位刷新时间;单位:毫秒
     */
    @TableField("refresh_time")
    private Long refreshTime;

    /**
     * 隐藏标志
     */
    @TableField("hidden_flag")
    private Integer hiddenFlag;

    /**
     * 额外特殊字段
     */
    @TableField(value = "extra", typeHandler = MybatisPlusJsonObjectTypeHandler.class)
    private Map<String, Object> extra;

}
