package site.yuanshen.data.dto;

import com.alibaba.fastjson.JSON;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;
import site.yuanshen.data.entity.SysUserArchive;
import site.yuanshen.data.vo.ArchiveHistoryVo;
import site.yuanshen.data.vo.ArchiveVo;

import java.time.LocalDateTime;
import java.util.LinkedList;

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
@EqualsAndHashCode(callSuper = true)
public class SysUserArchiveDto extends BaseEntity {

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
    private Long slotIndex;

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
    private LinkedList<String> archiveHistory;

    public SysUserArchiveDto(SysUserArchive sysUserArchive) {
        BeanUtils.copyProperties(sysUserArchive, this);
        archiveHistory = new LinkedList<>(JSON.parseArray(data).toJavaList(String.class));
    }

    public SysUserArchive getEntity() {
        return BeanUtils.copyProperties(this, SysUserArchive.class)
                .setData(JSON.toJSONString(archiveHistory));
    }

    /**
     * @param index 存档历史下标
     * @return 指定历史下标的存档
     */
    public ArchiveVo getVo(int index) {
        ArchiveVo vo = BeanUtils.copyProperties(this, ArchiveVo.class);
        vo.setArchive(archiveHistory.get(index - 1));
        return vo;
    }

    /**
     * @return 历史存档列表
     */
    public ArchiveHistoryVo getHistoryVo() {
        ArchiveHistoryVo vo = BeanUtils.copyProperties(this, ArchiveHistoryVo.class);
        vo.setArchive(archiveHistory.toArray(new String[0]));
        return vo;
    }

    /**
     * 存入存档
     *
     * @param newArchive 存档JSON字符串
     * @return 存档是否已更改
     */
    public boolean saveArchive(String newArchive) {
        if (archiveHistory.getFirst().equals(newArchive)) return false;
        archiveHistory.add(0, newArchive);
        while (archiveHistory.size() > 5) {
            archiveHistory.removeLast();
        }
        return true;
    }

    /**
     * 恢复上次存档（删除最近一次存档）
     */
    public ArchiveVo restoreHistory() {
        try {
            ArchiveVo vo = BeanUtils.copyProperties(this, ArchiveVo.class);
            archiveHistory.removeFirst();
            vo.setArchive(archiveHistory.getFirst());
            return vo;
        }
        catch (Exception ignore) {
            throw new RuntimeException("存档为空，无历史存档");
        }
    }
}
