package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Notice;
import site.yuanshen.data.vo.NoticeVo;

import java.sql.Timestamp;
import java.time.LocalDateTime;


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
    private LocalDateTime updateTime;

    /**
     * 频道
     */
    private String channel;

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
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Timestamp validTimeStart;

    /**
     * 有效期结束时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
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