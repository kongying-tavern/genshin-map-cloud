package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.vo.HistoryVo;
import java.time.LocalDateTime;


/**
 * 历史操作表路数据封装
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
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

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