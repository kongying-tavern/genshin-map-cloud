package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.data.base.BaseEntity;

/**
 * 点位主表
 *
 * @author Moment
 * @since 2022-07-04 11:36:24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("marker")
@Schema(title = "Marker对象", description = "点位主表")
public class Marker extends BaseEntity {

    /**
     * id
     */
    @Schema(title = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 旧版本点位id
     */
    @Schema(title = "旧版本点位id")
    @TableField("marker_stamp")
    private String markerStamp;

    /**
     * 点位名称
     */
    @Schema(title = "点位名称")
    @TableField("marker_title")
    private String markerTitle;

    /**
     * 点位坐标
     */
    @Schema(title = "点位坐标")
    @TableField("position")
    private String position;

    /**
     * 点位说明
     */
    @Schema(title = "点位说明")
    @TableField("content")
    private String content;

    /**
     * 点位图片
     */
    @Schema(title = "点位图片")
    @TableField("picture")
    private String picture;

    /**
     * 点位初始标记者
     */
    @Schema(title = "点位初始标记者")
    @TableField("marker_creator_id")
    private Long markerCreatorId;

    /**
     * 点位图片上传者
     */
    @Schema(title = "点位图片上传者")
    @TableField("picture_creator_id")
    private Long pictureCreatorId;

    /**
     * 点位视频
     */
    @Schema(title = "点位视频")
    @TableField("video_path")
    private String videoPath;

    /**
     * 刷新时间
     */
    @Schema(title = "刷新时间")
    @TableField("refresh_time")
    private Long refreshTime;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    @TableField("hidden_flag")
    private Integer hiddenFlag;

    /**
     * 额外特殊字段
     */
    @Schema(title = "额外特殊字段")
    @TableField("extra")
    private String extra;
}
