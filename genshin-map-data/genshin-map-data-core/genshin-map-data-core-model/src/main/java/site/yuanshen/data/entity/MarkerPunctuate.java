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
 * 点位提交表
 *
 * @author Moment
 * @since 2022-07-04 11:36:24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("marker_punctuate")
@Schema(title = "MarkerPunctuate对象", description = "点位提交表")
public class MarkerPunctuate extends BaseEntity {

    /**
     * id
     */
    @Schema(title = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 点位提交id
     */
    @Schema(title = "点位提交id")
    @TableField("punctuate_id")
    private Long punctuateId;

    /**
     * 原有点位id
     */
    @Schema(title = "原有点位id")
    @TableField("original_marker_id")
    private Long originalMarkerId;

    /**
     * 点位名称
     */
    @Schema(title = "点位名称")
    @TableField("marker_title")
    private String markerTitle;

    /**
     * 点位物品列表
     */
    @Schema(title = "点位物品列表")
    @TableField("item_list")
    private String itemList;

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
     * 点位提交者id
     */
    @Schema(title = "点位提交者id")
    @TableField("author")
    private Long author;

    /**
     * 状态;0:暂存 1:审核中 2:不通过
     */
    @Schema(title = "状态;0:暂存 1:审核中 2:不通过")
    @TableField("status")
    private Integer status;

    /**
     * 审核备注
     */
    @Schema(title = "审核备注")
    @TableField("audit_remark")
    private String auditRemark;

    /**
     * 操作类型;1: 新增 2: 修改 3: 删除
     */
    @Schema(title = "操作类型;1: 新增 2: 修改 3: 删除")
    @TableField("method_type")
    private Integer methodType;

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

}
