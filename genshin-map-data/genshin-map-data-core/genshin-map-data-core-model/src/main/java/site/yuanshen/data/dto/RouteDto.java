package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Route;
import site.yuanshen.data.vo.RouteVo;


/**
 * 路线数据封装
 *
 * @author Moment
 * @since 2023-01-03 05:13:30
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@With
@EqualsAndHashCode
@Schema(title = "Route数据封装", description = "路线数据封装")
public class RouteDto {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

                /**
     * ID
     */
    @Schema(title = "ID")
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

    public RouteDto(Route route) {
        BeanUtils.copyProperties(route, this);
    }

    public RouteDto(RouteVo routeVo) {
        BeanUtils.copyProperties(routeVo, this);
    }

    @JSONField(serialize = false)
    public Route getEntity() {
        return BeanUtils.copyProperties(this, Route.class);
    }

    @JSONField(serialize = false)
    public RouteVo getVo() {
        return BeanUtils.copyProperties(this, RouteVo.class);
    }


}
