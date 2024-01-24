package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Notice;
import site.yuanshen.data.vo.NoticeVo;

import java.sql.Timestamp;
import java.util.List;


/**
 * 消息通知路数据封装
 *
 * @since 2023-05-31 03:12:05
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Notice数据封装", description = "消息通知数据封装")
public class NoticeDto {

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
     * 频道
     */
    private List<String> channel;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 有效期开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp validTimeStart;

    /**
     * 有效期结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp validTimeEnd;

    public NoticeDto(Notice notice) {
        BeanUtils.copy(notice, this);
    }

    public NoticeDto(NoticeVo noticeVo) {
        BeanUtils.copy(noticeVo, this);
    }

    @JSONField(serialize = false)
    public Notice getEntity() {
        return BeanUtils.copy(this, Notice.class);
    }

    @JSONField(serialize = false)
    public NoticeVo getVo() {
        return BeanUtils.copy(this, NoticeVo.class);
    }

}
