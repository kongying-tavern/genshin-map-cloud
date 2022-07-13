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
 * 点位额外字段提交表
 *
 * @author Moment
 * @since 2022-06-26 02:23:28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("marker_extra_punctuate")
@Schema(title = "MarkerExtraPunctuate对象", description = "点位额外字段提交表")
public class MarkerExtraPunctuate extends BaseEntity {

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
     * 额外特殊字段具体内容
     */
    @Schema(title = "额外特殊字段具体内容")
    @TableField("marker_extra_content")
    private String markerExtraContent;

    /**
     * 父点位id
     */
    @Schema(title = "父点位id")
    @TableField("parent_id")
    private Long parentId;

    /**
     * 关联其他点位flag
     */
    @Schema(title = "关联其他点位flag")
    @TableField("is_related")
    private Boolean isRelated;

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


}
