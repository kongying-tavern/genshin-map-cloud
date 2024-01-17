package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * 创建人
     */
    @Schema(title = "创建人")
    private Long creatorId;

    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    private Timestamp createTime;

    /**
     * 更新人
     */
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    private Timestamp updateTime;

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
    private List<Object> markerList;

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
    private Map<String, Object> extra;

    /**
     * 创建人昵称
     */
    @Schema(title = "创建人昵称")
    private String creatorNickname;

}
