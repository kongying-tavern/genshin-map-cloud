package site.yuanshen.data.dto;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.entity.SysUserArchive;
import site.yuanshen.data.vo.SysArchiveSlotVo;
import site.yuanshen.data.vo.SysArchiveVo;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 系统用户存档数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysUserArchive数据封装", description = "系统用户存档表数据封装")
public class SysUserArchiveSlotDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createTime;

    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

    /**
     * 存档名称
     */
    private String name;

    /**
     * 槽位顺序
     */
    private Integer slotIndex;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 存档历史
     */
    private LinkedList<SysUserArchiveDto> archiveHistory;

    public SysUserArchiveSlotDto(SysUserArchive sysUserArchive) {
        BeanUtils.copy(sysUserArchive, this);
        archiveHistory = sysUserArchive.getData().stream()
                .map(archiveHistory -> {
                    Map<String, Object> archive = (Map<String, Object>) archiveHistory;
                    Timestamp ts = null;
                    try {
                        Object time = archive.get("time");
                        if(time instanceof String) {
                            ts = TimeUtils.parseTime((String) time);
                        } else if(time instanceof Long) {
                            ts = new Timestamp((Long) time);
                        }
                    } catch(Exception ex) {
                        // nothing to do;
                    }
                    return (new SysUserArchiveDto())
                            .withArchive((String) archive.get("archive"))
                            .withTime(ts);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public SysUserArchive getEntity() {
        return BeanUtils.copy(this, SysUserArchive.class)
                .withData(JSONArray.from(archiveHistory));
    }

    /**
     * @param index 存档历史下标
     * @return 指定历史下标的存档
     */
    public SysArchiveVo getArchiveVo(int index) {;
        return archiveHistory.get(index - 1).getVo(index);
    }

    /**
     * @return 历史存档列表
     */
    public SysArchiveSlotVo getSlotVo() {
        AtomicInteger index = new AtomicInteger(1);
        return BeanUtils.copy(this, SysArchiveSlotVo.class)
                .withArchive(archiveHistory.stream()
                        .map(dto -> dto.getVo(index.getAndAdd(1)))
                        .collect(Collectors.toList()));
    }

    /**
     * 存入存档
     *
     * @param newArchive 存档JSON字符串
     * @return 存档是否已更改
     */
    public boolean saveArchive(String newArchive) {
        if (!archiveHistory.isEmpty() && StrUtil.equals(archiveHistory.getFirst().getArchive(), newArchive))
            return false;
        archiveHistory.add(0, new SysUserArchiveDto(newArchive));
        while (archiveHistory.size() > 5) {
            archiveHistory.removeLast();
        }
        return true;
    }

    /**
     * 恢复上次存档（删除最近一次存档）
     */
    public SysArchiveVo restoreHistory() {
        if (archiveHistory.isEmpty()) throw new GenshinApiException("存档为空，无历史存档");
        return archiveHistory.removeFirst().getVo(1);
    }
}
