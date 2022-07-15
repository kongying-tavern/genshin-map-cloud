package site.yuanshen.data.dto;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.CachedBeanCopier;;
import site.yuanshen.data.entity.MarkerExtraPunctuate;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.entity.MarkerPunctuate;
import site.yuanshen.data.vo.MarkerPunctuateVo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 打点完整信息的数据封装
 *
 * @author Moment
 * @since 2022-06-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "MarkerPunctuate完整信息的数据封装", description = "打点完整信息的数据封装")
public class MarkerPunctuateDto {

    /**
     * 打点ID
     */
    @Schema(title = "打点ID")
    private Long punctuateId;

    /**
     * 原有点位id
     */
    @Schema(title = "原有点位id")
    private Long originalMarkerId;

    /**
     * 点位名称
     */
    @Schema(title = "点位名称")
    private String markerTitle;

    /**
     * 点位坐标
     */
    @Schema(title = "点位坐标")
    private String position;

    /**
     * 点位物品列表
     */
    @Schema(title = "点位物品列表")
    private List<MarkerItemLinkDto> itemList;

    /**
     * 点位说明
     */
    @Schema(title = "点位说明")
    private String content;

    /**
     * 点位图片
     */
    @Schema(title = "点位图片")
    private String picture;

    /**
     * 点位初始标记者
     */
    @Schema(title = "点位初始标记者")
    private Long markerCreatorId;

    /**
     * 点位图片上传者
     */
    @Schema(title = "点位图片上传者")
    private Long pictureCreatorId;

    /**
     * 点位视频
     */
    @Schema(title = "点位视频")
    private String videoPath;

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

    /**
     * 刷新时间
     */
    @Schema(title = "刷新时间")
    private Long refreshTime;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    private Integer hiddenFlag;


    public MarkerPunctuateDto(MarkerPunctuateVo punctuateVo) {
        CachedBeanCopier.copyProperties(punctuateVo, this);
    }

    public MarkerPunctuateDto(MarkerPunctuate punctuate, MarkerExtraPunctuate extraPunctuate) {
        CachedBeanCopier.copyProperties(punctuate, this);
        if (extraPunctuate != null) {
            CachedBeanCopier.copyProperties(extraPunctuate, this);
        }
        this.itemList = JSONArray.parseArray(punctuate.getItemList(), MarkerItemLinkDto.class);
    }

    public MarkerPunctuate getEntity() {
        return CachedBeanCopier.copyProperties(this, MarkerPunctuate.class);
    }

    public MarkerExtraPunctuate getMarkerExtraEntity() {
        if (markerExtraContent == null || markerExtraContent.equals("")) markerExtraContent = "{}";
        return CachedBeanCopier.copyProperties(this, MarkerExtraPunctuate.class).setIsRelated(isRelated != null && isRelated.equals(1));
    }

    public List<MarkerItemLink> getLinkEntity() {
        return this.itemList.stream().map(MarkerItemLinkDto::getEntity).collect(Collectors.toList());
    }

    public MarkerPunctuateVo getVo() {
        MarkerPunctuateVo punctuateVo = CachedBeanCopier.copyProperties(this, MarkerPunctuateVo.class);
        punctuateVo.setItemList(this.itemList.stream().map(MarkerItemLinkDto::getVo).collect(Collectors.toList()));
        return punctuateVo;
    }

}
