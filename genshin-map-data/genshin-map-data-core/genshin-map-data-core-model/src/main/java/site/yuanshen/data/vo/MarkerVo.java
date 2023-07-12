package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
* 点位主表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Marker前端封装", description = "点位主表前端封装")
public class MarkerVo {

    /**
     * 乐观锁
     */
    @Schema(title = "乐观锁")
    private Long version;

    /**
     * ID
     */
    @Schema(title = "ID")
    private Long id;

    /**
     * 创建人
     */
    @Schema(title = "创建人")
    private Long creatorId;

    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 点位签戳（用于兼容旧点位ID）
     */
    @Schema(title = "点位签戳（用于兼容旧点位ID）")
    private String markerStamp;

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
    private List<MarkerItemLinkVo> itemList;

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
     * 点位刷新时间;单位:毫秒
     */
    @Schema(title = "点位刷新时间;单位:毫秒")
    private Long refreshTime;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    private Integer hiddenFlag;

    /**
     * 额外特殊字段
     */
    @Schema(title = "额外特殊字段")
    private Map<String, Object> extra;

}