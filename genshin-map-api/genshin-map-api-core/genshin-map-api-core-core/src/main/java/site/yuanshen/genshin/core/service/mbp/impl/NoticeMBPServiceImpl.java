package site.yuanshen.genshin.core.service.mbp.impl;

import site.yuanshen.data.entity.Notice;
import site.yuanshen.data.mapper.NoticeMapper;
import site.yuanshen.genshin.core.service.mbp.NoticeMBPService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 消息通知 Mybatis Plus CRUD服务实现类
 *
 * @since 2023-05-31 03:12:05
 */
@Service
public class NoticeMBPServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeMBPService {

}
