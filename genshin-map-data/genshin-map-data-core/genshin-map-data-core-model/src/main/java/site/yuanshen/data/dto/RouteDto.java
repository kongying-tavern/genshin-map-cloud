package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Route;
import site.yuanshen.data.vo.RouteVo;
import java.time.LocalDateTime;


/**
 * 路线路数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Route数据封装", description = "路线数据封装")
public class RouteDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 路线名称
     */
    private String name;

    /**
     * 路线描述
     */
    private String content;

    /**
     * 点位顺序数组
     */
    private String markerList;

    /**
     * 显隐等级
     */
    private Integer hiddenFlag;

    /**
     * 视频地址
     */
    private String video;

    /**
     * 额外信息
     */
    private String extra;

    /**
     * 创建人昵称
     */
    private String creatorNickname;

    public RouteDto(Route route) {
        BeanUtils.copy(route, this);
    }

    public RouteDto(RouteVo routeVo) {
        BeanUtils.copy(routeVo, this);
    }

    @JSONField(serialize = false)
    public Route getEntity() {
        return BeanUtils.copy(this, Route.class);
    }

    @JSONField(serialize = false)
    public RouteVo getVo() {
        return BeanUtils.copy(this, RouteVo.class);
    }

}