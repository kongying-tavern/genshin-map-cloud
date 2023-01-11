package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.vo.TagVo;

import java.util.List;

/**
 * 图标标签的数据查询层
 *
 * @author Moment
 */
public interface IconTagDao {

    /**
     * @return 所有的标签信息
     */
    List<TagVo> listAllTag();

    /**
     * @return 所有的标签信息的Bz2压缩
     */
    byte[] listAllTagBz2();

    /**
     * @return 所有的标签信息的Bz2压缩的md5
     */
    String listAllTagBz2Md5();
}
