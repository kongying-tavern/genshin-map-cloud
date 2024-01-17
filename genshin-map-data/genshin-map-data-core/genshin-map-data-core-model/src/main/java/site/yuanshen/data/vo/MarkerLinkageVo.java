package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.data.vo.adapter.marker.linkage.PathEdgeVo;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
* 点位关联前端封装
*
* @since 2023-10-20 11:07:15
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "MarkerLinkage前端封装", description = "点位关联前端封装")
public class MarkerLinkageVo {

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
    private Timestamp updateTime;

    /**
     * 组ID
     */
    @Schema(title = "组ID")
    private String groupId;

    /**
     * 起始点点位ID;会根据是否反向与 to_id 交换
     */
    @Schema(title = "起始点点位ID;会根据是否反向与 to_id 交换")
    private Long fromId;

    /**
     * 终止点点位ID;会根据是否反向与 from_id 交换
     */
    @Schema(title = "终止点点位ID;会根据是否反向与 from_id 交换")
    private Long toId;

    /**
     * 关联操作类型
     */
    @Schema(title = "关联操作类型")
    private String linkAction;

    /**
     * 是否反向
     */
    @Schema(title = "是否反向")
    private Boolean linkReverse;

    /**
     * 路线
     */
    @Schema(title = "路线")
    private List<PathEdgeVo> path;

    /**
     * 额外数据
     */
    @Schema(title = "额外数据")
    private Map<String, Object> extra;

}
