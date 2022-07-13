package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.Tag;
import site.yuanshen.data.mapper.TagMapper;
import site.yuanshen.genshin.core.service.mbp.TagMBPService;

/**
 * 图标标签主表 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class TagMBPServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagMBPService {

}
