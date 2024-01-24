package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.marker.linkage.PathEdgeVo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


/**
 * 点位关联数据封装
 *
 * @since 2023-10-20 11:07:15
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "MarkerLinkage数据封装", description = "点位关联数据封装")
public class MarkerLinkageDto {

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
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

    /**
     * 组ID
     */
    private String groupId;

    /**
     * 起始点点位ID;会根据是否反向与 to_id 交换
     */
    private Long fromId;

    /**
     * 终止点点位ID;会根据是否反向与 from_id 交换
     */
    private Long toId;

    /**
     * 关联操作类型
     */
    private String linkAction;

    /**
     * 是否反向
     */
    private Boolean linkReverse;

    /**
     * 路线
     */
    private List<PathEdgeVo> path;

    /**
     * 额外数据
     */
    private Map<String, Object> extra;

    public MarkerLinkageDto(MarkerLinkage markerLinkage) {
        BeanUtils.copy(markerLinkage, this);
    }

    public MarkerLinkageDto(MarkerLinkageVo markerLinkageVo) {
        BeanUtils.copy(markerLinkageVo, this);
    }

    @JSONField(serialize = false)
    public MarkerLinkage getEntity() {
        return BeanUtils.copy(this, MarkerLinkage.class);
    }

    @JSONField(serialize = false)
    public MarkerLinkageVo getVo() {
        return BeanUtils.copy(this, MarkerLinkageVo.class);
    }

}
