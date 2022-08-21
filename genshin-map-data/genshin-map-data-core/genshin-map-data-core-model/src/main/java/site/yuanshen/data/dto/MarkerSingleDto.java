package site.yuanshen.data.dto;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.vo.MarkerSingleVo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 点位无Extra的数据封装
 *
 * @author Moment
 * @since 2022-06-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "Marker数据封装", description = "点位无Extra的数据封装")
public class MarkerSingleDto {

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
     * 刷新时间
     */
    @Schema(title = "刷新时间")
    private Long refreshTime;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    private Integer hiddenFlag;

    public MarkerSingleDto(MarkerSingleVo markerSingleVo) {
        BeanUtils.copyProperties(markerSingleVo, this);
        if(markerSingleVo.getItemList() != null) {
            this.itemList = markerSingleVo.getItemList().stream().map(MarkerItemLinkDto::new).collect(Collectors.toList());
        }
    }

    public MarkerSingleDto(Marker marker, List<MarkerItemLink> markerItemLinks) {
        BeanUtils.copyProperties(marker, this);
        if(markerItemLinks != null) {
            this.itemList = markerItemLinks.stream().map(MarkerItemLinkDto::new).collect(Collectors.toList());
        }
    }


    @JSONField(serialize = false)
    public Marker getEntity() {
        return BeanUtils.copyProperties(this, Marker.class);
    }


    @JSONField(serialize = false)
    public MarkerSingleVo getVo() {
        MarkerSingleVo MarkerSingleVo = BeanUtils.copyProperties(this, MarkerSingleVo.class);
        MarkerSingleVo.setItemList(this.itemList.stream().map(MarkerItemLinkDto::getVo).collect(Collectors.toList()));
        return BeanUtils.copyProperties(this, MarkerSingleVo.class);
    }

}
