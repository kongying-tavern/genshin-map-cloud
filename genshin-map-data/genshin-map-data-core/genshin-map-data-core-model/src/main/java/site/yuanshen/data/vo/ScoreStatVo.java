package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.LocalDateTime;
import java.util.Map;

/**
* 评分统计前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "ScoreStat前端封装", description = "评分统计前端封装")
public class ScoreStatVo {

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
     * 创建人信息
     */
    @Schema(title = "创建人信息")
    private SysUserVo creator;

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
     * 更新人信息
     */
    @Schema(title = "更新人信息")
    private SysUserVo updater;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 统计范围
     */
    @Schema(title = "统计范围")
    private String scope;

    /**
     * 统计颗粒度
     */
    @Schema(title = "统计颗粒度")
    private String span;

    /**
     * 统计起始时间
     */
    @Schema(title = "统计起始时间")
    private LocalDateTime spanStartTime;

    /**
     * 统计终止时间
     */
    @Schema(title = "统计终止时间")
    private LocalDateTime spanEndTime;

    /**
     * 用户ID
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * 修改的字段JSON
     */
    @Schema(title = "修改的字段JSON")
    private Map<String, Object> content;

}