package site.yuanshen.genshin.core.service;

import site.yuanshen.data.vo.SysArchiveSlotVo;
import site.yuanshen.data.vo.SysArchiveVo;

import java.util.List;

/**
 * 用户存档服务接口
 *
 * @author Moment
 */
public interface SysUserArchiveService {

    /**
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @return 指定存档槽位的当前存档
     */
    SysArchiveVo getLastArchive(int slotIndex, Long userId);

    /**
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @return 指定槽位的所有历史存档
     */
    SysArchiveSlotVo getSlot(int slotIndex, Long userId);

    /**
     * @param userId 用户id
     * @return 所有槽位的历史存档
     */
    List<SysArchiveSlotVo> getAllSlot(Long userId);

    /**
     * 新建存档槽位并将存档存入<br>
     * 新建存档并存入，注意槽位下标不能冲突
     *
     * @param slotIndex 槽位下标
     * @param archive   存档
     * @param userId    用户id
     * @param name      存档名称
     * @return 是否成功
     */
    Boolean createSlotAndSaveArchive(int slotIndex, String archive, Long userId, String name);

    /**
     * 存档入指定槽位<br>
     * 指定槽位下标，将存档存入该槽位。如果存档与最后一次一致，则不存入，并返回false；如果槽位已满，则挤掉最后一次备份。
     *
     * @param slotIndex 槽位下标
     * @param archive   存档
     * @param userId    用户id
     * @return 是否成功
     */
    Boolean saveArchive(int slotIndex, String archive, Long userId);

    /**
     * 重命名指定槽位
     *
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @param newName   存档新名称
     * @return 是否成功
     */
    boolean renameSlot(int slotIndex, Long userId, String newName);

    /**
     * 删除最近一次存档（恢复为上次存档）<br>
     * 删除最近一次存档，也意味着恢复为上次存档。会返回上一次存档。如果存档为空，则返回400，并附带报错信息
     *
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @return 新的当前存档
     */
    SysArchiveVo restoreArchive(int slotIndex, Long userId);

    /**
     * 删除存档槽位
     *
     * @param slotIndex 槽位下标
     * @param userId    用户id
     * @return 是否成功
     */
    Boolean removeArchive(int slotIndex, Long userId);

}
