package site.yuanshen.data.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerExtra;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.vo.MarkerVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 点位完整信息的数据封装
 *
 * @author Moment
 * @since 2022-06-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "Marker完整信息的数据封装", description = "点位完整信息的数据封装")
public class MarkerDto {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

    /**
     * 点位ID
     */
    @Schema(title = "点位ID")
    private Long id;

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
     * 刷新时间
     */
    @Schema(title = "刷新时间")
    private Long refreshTime;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    private Integer hiddenFlag;

    public MarkerDto(MarkerVo markerVo) {
        BeanUtils.copyProperties(markerVo, this);
    }


    /**
     * 构建点位返回的方法
     * 2022.09.12 增加传参,带入TagIcon
     * @param marker
     * @param markerExtra
     * @param markerItemLinks
     */
    public MarkerDto(Marker marker, MarkerExtra markerExtra, List<MarkerItemLink> markerItemLinks, Map<Long, Item> itemMap) {
        BeanUtils.copyProperties(marker, this);
        this.id = marker.getId();
        BeanUtils.copyNotNull(Optional.ofNullable(markerExtra).orElse(new MarkerExtra()).setId(null),
                this);
        markerItemLinks = Optional.ofNullable(markerItemLinks).orElse(new ArrayList<>());
        this.itemList = markerItemLinks.stream().map(
                markerItemLink -> new MarkerItemLinkDto(markerItemLink).setIconTag(itemMap.get(markerItemLink.getItemId()).getIconTag())
        ).collect(Collectors.toList());
    }


    public MarkerDto(Marker marker, MarkerExtra markerExtra, List<MarkerItemLink> markerItemLinks) {
        BeanUtils.copyProperties(marker, this);
        this.id = marker.getId();
        BeanUtils.copyNotNull(Optional.ofNullable(markerExtra).orElse(new MarkerExtra()).setId(null),
                this);
        markerItemLinks = Optional.ofNullable(markerItemLinks).orElse(new ArrayList<>());
        this.itemList = markerItemLinks.stream().map(MarkerItemLinkDto::new).collect(Collectors.toList());
    }

    @JSONField(serialize = false)
    public Marker getEntity() {
        return BeanUtils.copyProperties(this, Marker.class).setId(this.id);
    }

    @JSONField(serialize = false)
    public MarkerExtra getMarkerExtraEntity() {
        if (markerExtraContent == null || markerExtraContent.equals("")) markerExtraContent = "{}";
        return BeanUtils.copyProperties(this, MarkerExtra.class).setIsRelated(isRelated != null && isRelated.equals(1));
    }

    @JSONField(serialize = false)
    public List<MarkerItemLink> getLinkEntity() {
        return this.itemList.stream().map(MarkerItemLinkDto::getEntity).collect(Collectors.toList());
    }

    @JSONField(serialize = false)
    public MarkerVo getVo() {
        MarkerVo markerVo = BeanUtils.copyProperties(this, MarkerVo.class);
        markerVo.setItemList(this.itemList.stream().map(MarkerItemLinkDto::getVo).collect(Collectors.toList()));
        return markerVo;
    }

}
