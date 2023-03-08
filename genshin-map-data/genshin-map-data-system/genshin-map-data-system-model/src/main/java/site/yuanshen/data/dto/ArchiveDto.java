package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.*;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.CachedBeanCopier;
import site.yuanshen.data.vo.ArchiveVo;

import java.time.LocalDateTime;

/**
 * 存档Dto
 *
 * @author Moment
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ArchiveDto {

    /**
     * 存档时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /**
     * 存档
     */
    private String archive;

    public ArchiveDto(String archive) {
        this.archive = archive;
        time = LocalDateTime.now();
    }

    public ArchiveDto(ArchiveVo vo) {
        CachedBeanCopier.copyProperties(vo, this);
    }

    public ArchiveVo getVo(int historyIndex) {
        ArchiveVo vo = CachedBeanCopier.copyProperties(this, ArchiveVo.class);
        vo.setHistoryIndex(historyIndex);
        return vo;
    }

}