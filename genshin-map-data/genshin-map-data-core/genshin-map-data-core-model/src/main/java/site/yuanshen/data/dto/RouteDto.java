package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Route;
import site.yuanshen.data.vo.RouteVo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


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
     * 创建人
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createTime;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

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
    private List<Object> markerList;

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
    private Map<String, Object> extra;

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
