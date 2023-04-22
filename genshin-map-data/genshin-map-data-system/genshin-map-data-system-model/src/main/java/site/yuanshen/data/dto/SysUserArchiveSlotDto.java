package site.yuanshen.data.dto;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang.StringUtils;
import site.yuanshen.common.core.utils.BeanUtils;
import lombok.*;
import lombok.experimental.Accessors;
import site.yuanshen.data.entity.SysUserArchive;
import site.yuanshen.data.vo.ArchiveSlotVo;
import site.yuanshen.data.vo.ArchiveVo;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 系统用户存档Dto
 *
 * @author Moment
 * @since 2022-12-02 06:27:13
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class SysUserArchiveSlotDto {

    /**
     * 乐观锁：修改次数
     */
    private Long version;

    /**
     * 存档ID
     */
    private Long id;

    /**
     * 存档名称
     */
    private String name;

    /**
     * 槽位顺序
     */
    private Integer slotIndex;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 存档信息
     */
    private String data;

    /**
     * 存档历史
     */
    private LinkedList<ArchiveDto> archiveHistory;

    public SysUserArchiveSlotDto(SysUserArchive sysUserArchive) {
        BeanUtils.copy(sysUserArchive, this);
        archiveHistory = new LinkedList<>(JSON.parseArray(sysUserArchive.getData()).toJavaList(ArchiveDto.class));
    }

    public SysUserArchive getEntity() {
        return BeanUtils.copy(this, SysUserArchive.class)
                .withData(JSON.toJSONString(archiveHistory));
    }

    /**
     * @param index 存档历史下标
     * @return 指定历史下标的存档
     */
    public ArchiveVo getArchiveVo(int index) {;
        return archiveHistory.get(index - 1).getVo(index);
    }

    /**
     * @return 历史存档列表
     */
    public ArchiveSlotVo getSlotVo() {
        AtomicInteger index = new AtomicInteger(1);
        return BeanUtils.copy(this, ArchiveSlotVo.class)
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
        if (StringUtils.equals(archiveHistory.getFirst().getArchive(), newArchive)) return false;
        archiveHistory.add(0, new ArchiveDto(newArchive));
        while (archiveHistory.size() > 5) {
            archiveHistory.removeLast();
        }
        return true;
    }

    /**
     * 恢复上次存档（删除最近一次存档）
     */
    public ArchiveVo restoreHistory() {
        if (archiveHistory.isEmpty()) throw new RuntimeException("存档为空，无历史存档");
        return archiveHistory.removeFirst().getVo(1);
    }
}
