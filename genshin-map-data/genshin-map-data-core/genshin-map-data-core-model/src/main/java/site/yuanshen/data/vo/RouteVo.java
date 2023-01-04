package site.yuanshen.data.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 路线前端封装
 *
 * @author Moment
 * @since 2023-01-03 05:10:07
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(title = "Route前端封装", description = "路线前端封装")
public class RouteVo {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

    /**
     * 路线ID
     */
    @Schema(title = "路线ID")
    private Long id;

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
     * 创建人
     */
    @Schema(title = "创建人")
    private Long creatorId;

    /**
     * 创建人昵称
     */
    @Schema(title = "创建人昵称")
    private String creatorNickname;


}
