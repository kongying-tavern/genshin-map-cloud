package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
* 路线前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Route前端封装", description = "路线前端封装")
public class RouteVo {

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
     * 路线名称
     */
    @Schema(title = "路线名称")
    private String name;

    /**
     * 路线描述
     */
    @Schema(title = "路线描述")
    private String content;

    /**
     * 点位顺序数组
     */
    @Schema(title = "点位顺序数组")
    private String markerList;

    /**
     * 显隐等级
     */
    @Schema(title = "显隐等级")
    private Integer hiddenFlag;

    /**
     * 视频地址
     */
    @Schema(title = "视频地址")
    private String video;

    /**
     * 额外信息
     */
    @Schema(title = "额外信息")
    private String extra;

    /**
     * 创建人昵称
     */
    @Schema(title = "创建人昵称")
    private String creatorNickname;

}