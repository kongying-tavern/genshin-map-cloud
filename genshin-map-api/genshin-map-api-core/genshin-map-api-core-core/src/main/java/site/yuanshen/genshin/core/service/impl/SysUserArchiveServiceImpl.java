package site.yuanshen.genshin.core.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dto.SysUserArchiveDto;
import site.yuanshen.data.dto.SysUserArchiveSlotDto;
import site.yuanshen.data.entity.SysUserArchive;
import site.yuanshen.data.mapper.SysUserArchiveMapper;
import site.yuanshen.data.vo.SysArchiveSlotVo;
import site.yuanshen.data.vo.SysArchiveVo;
import site.yuanshen.genshin.core.service.SysUserArchiveService;

import java.util.Collections;
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

    private SysUserArchive getSlotEntity(int slotIndex, Long userId) {
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
    public SysArchiveVo getLastArchive(int slotIndex, Long userId) {
        return new SysUserArchiveSlotDto(getSlotEntity(slotIndex, userId))
                .getArchiveVo(1);
    }

    /**
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @return 指定槽位的所有历史存档
     */
    @Override
    public SysArchiveSlotVo getSlot(int slotIndex, Long userId) {
        return new SysUserArchiveSlotDto(getSlotEntity(slotIndex, userId)).getSlotVo();
    }


    /**
     * @param userId 用户id
     * @return 所有槽位的历史存档
     */
    @Override
    public List<SysArchiveSlotVo> getAllSlot(Long userId) {
        return sysUserArchiveMapper.selectList(Wrappers.<SysUserArchive>lambdaQuery().eq(SysUserArchive::getUserId, userId))
                .parallelStream().map(SysUserArchiveSlotDto::new).sorted(Comparator.comparingLong(SysUserArchiveSlotDto::getSlotIndex))
                .map(SysUserArchiveSlotDto::getSlotVo)
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
    public Boolean createSlotAndSaveArchive(int slotIndex, String archive, Long userId, String name) {
        if (this.getSlotEntity(slotIndex, userId) != null) throw new RuntimeException("槽位下标冲突，请重新选择下标");
        return sysUserArchiveMapper.insert(
                new SysUserArchive()
                        .withSlotIndex(slotIndex)
                        .withUserId(userId)
                        .withName(name)
                        .withData((JSON.toJSONString(Collections.singletonList(new SysUserArchiveDto(archive))))))
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
        SysUserArchiveSlotDto slotDto = new SysUserArchiveSlotDto(getSlotEntity(slotIndex, userId));
        if (slotDto.saveArchive(archive)) {
            sysUserArchiveMapper.updateById(slotDto.getEntity());
            return true;
        }
        return false;
    }

    /**
     * 重命名指定槽位
     *
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @param newName   存档新名称
     * @return 是否成功
     */
    @Override
    public boolean renameSlot(int slotIndex, Long userId, String newName) {
        SysUserArchive archive = getSlotEntity(slotIndex, userId);
        if (archive == null) throw new RuntimeException("槽位不存在");
        return sysUserArchiveMapper.updateById(archive.withName(newName)) == 1;
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
    public SysArchiveVo restoreArchive(int slotIndex, Long userId) {
        SysUserArchiveSlotDto slotDto = new SysUserArchiveSlotDto(getSlotEntity(slotIndex, userId));
        SysArchiveVo sysArchiveVo = slotDto.restoreHistory();
        sysUserArchiveMapper.updateById(slotDto.getEntity());
        return sysArchiveVo;
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
        if (this.getSlotEntity(slotIndex, userId) == null) throw new RuntimeException("槽位不存在");
        return sysUserArchiveMapper.delete(
                Wrappers.<SysUserArchive>lambdaQuery()
                        .eq(SysUserArchive::getUserId, userId)
                        .eq(SysUserArchive::getSlotIndex, slotIndex))
                == 1;
    }
}
