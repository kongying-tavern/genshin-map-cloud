package site.yuanshen.data.dto;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.vo.HistoryVo;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "History数据对象", description = "History数据对象")
public class HistoryDto {

    @Schema(title = "id")
    private Long id;

    /**
     * 内容
     */
    @Schema(title = "内容")
    private Object content;

    /**
     * md5
     */
    @Schema(title = "md5")
    private String md5;

    /**
     * 类型id
     */
    @Schema(title = "类型id")
    private Long tId;

    /**
     * 记录类型
     */
    @Schema(title = "记录类型")
    private Integer type;

    /**
     * ipv4
     */
    @Schema(title = "ipv4")
    private String ipv4;

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

    public HistoryDto(History history) {
        BeanUtils.copyProperties(history, this).setContent(JSON.parseObject(history.getContent()));
    }

    @JSONField(serialize = false)
    public History getEntity() {
        return BeanUtils.copyProperties(this, History.class);
    }

    public HistoryDto(HistoryVo historyVo) {
        BeanUtils.copyProperties(historyVo, this);
    }

    @JSONField(serialize = false)
    public HistoryVo getVo() {
        return BeanUtils.copyProperties(this, HistoryVo.class);
    }
}
