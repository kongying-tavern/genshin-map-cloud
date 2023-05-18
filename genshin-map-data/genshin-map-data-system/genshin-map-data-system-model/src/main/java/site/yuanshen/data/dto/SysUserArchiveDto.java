package site.yuanshen.data.dto;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.vo.SysArchiveVo;

import java.time.LocalDateTime;

/**
 * 存档Dto</p>
 * 此为单个存档数据封装
 *
 * @see SysUserArchiveSlotDto
 * @author Moment
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class SysUserArchiveDto {

    /**
     * 存档时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /**
     * 存档
     */
    private String archive;

    public SysUserArchiveDto(String archive) {
        this.archive = StrUtil.cleanBlank(archive);
        time = LocalDateTime.now();
    }

    public SysUserArchiveDto(SysArchiveVo vo) {
        BeanUtils.copy(vo, this);
    }

    public SysArchiveVo getVo(int historyIndex) {
        return BeanUtils.copy(this, SysArchiveVo.class)
                .withHistoryIndex(historyIndex);
    }

}