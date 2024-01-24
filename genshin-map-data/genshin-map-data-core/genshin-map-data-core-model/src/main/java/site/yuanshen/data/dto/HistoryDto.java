package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryEditType;
import site.yuanshen.data.vo.HistoryVo;

import java.sql.Timestamp;


/**
 * 历史操作数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "History数据封装", description = "历史操作表数据封装")
public class HistoryDto {

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
     * 内容
     */
    private String content;

    /**
     * MD5
     */
    private String md5;

    /**
     * 原ID
     */
    private Long tId;

    /**
     * 操作数据类型;1地区; 2图标; 3物品; 4点位; 5标签
     */
    private Integer type;

    /**
     * IPv4
     */
    private String ipv4;

    /**
     * 修改类型
     */
    private HistoryEditType editType;

    public HistoryDto(History history) {
        BeanUtils.copy(history, this);
    }

    public HistoryDto(HistoryVo historyVo) {
        BeanUtils.copy(historyVo, this);
    }

    @JSONField(serialize = false)
    public History getEntity() {
        return BeanUtils.copy(this, History.class);
    }

    @JSONField(serialize = false)
    public HistoryVo getVo() {
        return BeanUtils.copy(this, HistoryVo.class);
    }

}
