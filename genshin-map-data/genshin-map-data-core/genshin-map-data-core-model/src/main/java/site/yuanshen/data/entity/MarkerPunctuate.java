package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDateTime;
import site.yuanshen.data.base.BaseEntity;
import lombok.*;
import site.yuanshen.data.enums.PunctuateMethodEnum;
import site.yuanshen.data.enums.PunctuateStatusEnum;
import site.yuanshen.handler.MybatisPlusJsonObjectTypeHandler;

/**
 * 点位提交表
 *
 * @since 2023-04-23 01:08:53
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("marker_punctuate")
public class MarkerPunctuate extends BaseEntity {

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
     * 点位提交ID
     */
    @TableField("punctuate_id")
    private Long punctuateId;

    /**
     * 原有点位ID
     */
    @TableField("original_marker_id")
    private Long originalMarkerId;

    /**
     * 点位名称
     */
    @TableField("marker_title")
    private String markerTitle;

    /**
     * 点位物品列表
     */
    @TableField("item_list")
    private String itemList;

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
     * 额外特殊字段
     */
    @TableField(value = "extra", typeHandler = MybatisPlusJsonObjectTypeHandler.class)
    private String extra;

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
     * 隐藏标志
     */
    @TableField("hidden_flag")
    private Integer hiddenFlag;

    /**
     * 点位提交者ID
     */
    @TableField("author")
    private Long author;

    /**
     * 状态;0:暂存 1:审核中 2:不通过
     */
    @TableField("status")
    private PunctuateStatusEnum status;

    /**
     * 审核备注
     */
    @TableField("audit_remark")
    private String auditRemark;

    /**
     * 操作类型;1: 新增 2: 修改 3: 删除
     */
    @TableField("method_type")
    private PunctuateMethodEnum methodType;

    /**
     * 点位刷新时间
     */
    @TableField("refresh_time")
    private Long refreshTime;

}
