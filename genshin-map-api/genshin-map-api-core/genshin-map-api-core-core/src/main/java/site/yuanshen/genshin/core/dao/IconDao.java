package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.vo.BinaryMD5Vo;
import site.yuanshen.data.vo.IconVo;

import java.util.List;

/**
 * 图标的数据查询层
 *
 * @author Moment
 */
public interface IconDao {

    /**
     * @return 所有的图标信息
     */
    List<IconVo> listAllIcon();

    /**
     * @return 所有的图标信息的压缩
     */
    byte[] listAllIconBinary();

    /**
     * @return 所有的图标信息的压缩的md5
     */
    BinaryMD5Vo listAllIconBinaryMd5();
}
