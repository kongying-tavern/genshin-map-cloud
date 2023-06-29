package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.vo.MarkerItemLinkVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.SysUserVo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 点位主数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Marker数据封装", description = "点位主表数据封装")
public class MarkerDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 创建人
     */
    private Long creatorId;

    /**
     * 创建人人信息
     */
    private SysUserVo creator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新人信息
     */
    private SysUserVo updater;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 点位签戳（用于兼容旧点位ID）
     */
    private String markerStamp;

    /**
     * 点位名称
     */
    private String markerTitle;

    /**
     * 点位坐标
     */
    private String position;

    /**
     * 点位物品列表
     */
    private List<MarkerItemLinkVo> itemList;

    /**
     * 点位说明
     */
    private String content;

    /**
     * 点位图片
     */
    private String picture;

    /**
     * 点位初始标记者
     */
    private Long markerCreatorId;

    /**
     * 点位图片上传者
     */
    private Long pictureCreatorId;

    /**
     * 点位视频
     */
    private String videoPath;

    /**
     * 点位刷新时间;单位:毫秒
     */
    private Long refreshTime;

    /**
     * 隐藏标志
     */
    private Integer hiddenFlag;

    /**
     * 额外特殊字段
     */
    private Map<String, Object> extra;

    public MarkerDto(Marker marker) {
        BeanUtils.copy(marker, this);
    }

    public MarkerDto(MarkerVo markerVo) {
        BeanUtils.copy(markerVo, this);
    }

    @JSONField(serialize = false)
    public Marker getEntity() {
        return BeanUtils.copy(this, Marker.class);
    }

    @JSONField(serialize = false)
    public MarkerVo getVo() {
        return BeanUtils.copy(this, MarkerVo.class);
    }

}