package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dto.SysUserArchiveDto;
import site.yuanshen.data.entity.SysUserArchive;
import site.yuanshen.data.mapper.SysUserArchiveMapper;
import site.yuanshen.data.vo.ArchiveHistoryVo;
import site.yuanshen.data.vo.ArchiveVo;
import site.yuanshen.genshin.core.service.SysUserArchiveService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户存档服务实现
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class SysUserArchiveServiceImpl implements SysUserArchiveService {

    private final SysUserArchiveMapper sysUserArchiveMapper;

    private SysUserArchive getArchive(int slotIndex, Long userId) {
        return sysUserArchiveMapper.selectOne(
                Wrappers.<SysUserArchive>lambdaQuery()
                        .eq(SysUserArchive::getSlotIndex, slotIndex)
                        .eq(SysUserArchive::getUserId, userId));
    }

    /**
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @return 指定存档槽位的当前存档
     */
    @Override
    public ArchiveVo getLastArchive(int slotIndex, Long userId) {
        return new SysUserArchiveDto(getArchive(slotIndex, userId))
                .getVo(1);
    }

    /**
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @return 指定槽位的所有历史存档
     */
    @Override
    public ArchiveHistoryVo getHistoryArchive(int slotIndex, Long userId) {
        return new SysUserArchiveDto(getArchive(slotIndex, userId)).getHistoryVo();
    }

    /**
     * @param userId 用户id
     * @return 所有槽位的最新存档
     */
    @Override
    public List<ArchiveVo> getAllArchive(Long userId) {
        return sysUserArchiveMapper.selectList(Wrappers.<SysUserArchive>lambdaQuery().eq(SysUserArchive::getUserId, userId))
                .stream().sorted(Comparator.comparingLong(SysUserArchive::getSlotIndex))
                .map(archive -> new SysUserArchiveDto(archive).getVo(1))
                .collect(Collectors.toList());
    }

    /**
     * @param userId 用户id
     * @return 所有槽位的历史存档
     */
    @Override
    public List<ArchiveHistoryVo> getAllHistoryArchive(Long userId) {
        return sysUserArchiveMapper.selectList(Wrappers.<SysUserArchive>lambdaQuery().eq(SysUserArchive::getUserId, userId))
                .stream().sorted(Comparator.comparingLong(SysUserArchive::getSlotIndex))
                .map(archive -> new SysUserArchiveDto(archive).getHistoryVo())
                .collect(Collectors.toList());
    }

    /**
     * 新建存档槽位并将存档存入<br>
     * 新建存档并存入，注意槽位下标不能冲突
     *
     * @param slotIndex 槽位下标
     * @param archive   存档
     * @param userId    用户id
     * @return 是否成功
     */
    @Override
    public Boolean createArchive(int slotIndex, String archive, Long userId, String name) {
        if (this.removeArchive(slotIndex, userId) != null) throw new RuntimeException("槽位下标冲突，请重新选择下标");
        return sysUserArchiveMapper.insert(
                new SysUserArchive()
                        .setSlotIndex(slotIndex)
                        .setUserId(userId)
                        .setName(name)
                        .setData("[" + archive + "]"))
                == 1;
    }

    /**
     * 存档入指定槽位<br>
     * 指定槽位下标，将存档存入该槽位。如果存档与最后一次一致，则不存入，并返回false；如果槽位已满，则挤掉最后一次备份。
     *
     * @param slotIndex 槽位下标
     * @param archive   存档
     * @param userId    用户id
     * @return 是否成功
     */
    @Override
    public Boolean saveArchive(int slotIndex, String archive, Long userId) {
        SysUserArchiveDto archiveDto = new SysUserArchiveDto(getArchive(slotIndex, userId));
        if (archiveDto.saveArchive(archive)) {
            sysUserArchiveMapper.insert(archiveDto.getEntity());
            return true;
        }
        return false;
    }

    /**
     * 删除最近一次存档（恢复为上次存档）<br>
     * 删除最近一次存档，也意味着恢复为上次存档。会返回上一次存档。如果存档为空，则返回400，并附带报错信息
     *
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @return 新的当前存档
     */
    @Override
    public ArchiveVo restoreArchive(int slotIndex, Long userId) {
        SysUserArchiveDto archiveDto = new SysUserArchiveDto(getArchive(slotIndex, userId));
        ArchiveVo archiveVo = archiveDto.restoreHistory();
        sysUserArchiveMapper.updateById(archiveDto.getEntity());
        return archiveVo;
    }

    /**
     * 删除存档槽位
     *
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @return 是否成功
     */
    @Override
    public Boolean removeArchive(int slotIndex, Long userId) {
        return sysUserArchiveMapper.delete(
                Wrappers.<SysUserArchive>lambdaQuery()
                        .eq(SysUserArchive::getUserId, userId)
                        .eq(SysUserArchive::getSlotIndex, slotIndex))
                == 1;
    }
}
