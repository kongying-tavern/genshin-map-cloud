package site.yuanshen.data.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.MarkerExtraPunctuate;
import site.yuanshen.data.vo.MarkerExtraPunctuateVo;

/**
 * 打点的Extra数据封装
 *
 * @author Moment
 * @since 2022-06-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "MarkerPunctuate的Extra数据封装", description = "打点的Extra数据封装")
public class MarkerExtraPunctuateDto {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

    /**
     * 打点ID
     */
    @Schema(title = "打点ID")
    private Long punctuateId;

    /**
     * 额外特殊字段具体内容
     */
    @Schema(title = "额外特殊字段具体内容")
    private String markerExtraContent;

    /**
     * 父点位ID
     */
    @Schema(title = "父点位ID")
    private Long parentId;

    /**
     * 关联其他点位Flag
     */
    @Schema(title = "关联其他点位Flag")
    private Integer isRelated;


    /**
     * 点位提交者id
     */
    @Schema(title = "点位提交者id")
    private Long author;

    /**
     * 状态;0:暂存 1:审核中 2:不通过
     */
    @Schema(title = "状态;0:暂存 1:审核中 2:不通过")
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
    private Integer methodType;

    public MarkerExtraPunctuateDto(MarkerExtraPunctuateVo extraPunctuateVo) {
        BeanUtils.copyProperties(extraPunctuateVo, this);
    }

    public MarkerExtraPunctuateDto(MarkerExtraPunctuate extraPunctuate) {
        BeanUtils.copyProperties(extraPunctuate, this);
    }

    @JSONField(serialize = false)
    public MarkerExtraPunctuate getMarkerExtraEntity() {
        if (markerExtraContent == null || markerExtraContent.equals("")) markerExtraContent = "{}";
        return BeanUtils.copyProperties(this, MarkerExtraPunctuate.class).setIsRelated(isRelated != null && isRelated.equals(1));
    }

    @JSONField(serialize = false)
    public MarkerExtraPunctuateVo getVo() {
        MarkerExtraPunctuateVo extraPunctuateVo = BeanUtils.copyProperties(this, MarkerExtraPunctuateVo.class);
        return extraPunctuateVo;
    }

}
