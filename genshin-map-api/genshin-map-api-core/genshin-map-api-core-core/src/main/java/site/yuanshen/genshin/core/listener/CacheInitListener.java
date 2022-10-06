package site.yuanshen.genshin.core.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import site.yuanshen.genshin.core.dao.IconTagDao;
import site.yuanshen.genshin.core.dao.ItemDao;
import site.yuanshen.genshin.core.dao.MarkerDao;

/**
 * 监听应用启动完成后，写入缓存
 *
 * @author Moment
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheInitListener implements ApplicationListener<ApplicationReadyEvent> {

    private final MarkerDao markerDao;
    private final ItemDao itemDao;
    private final IconTagDao iconTagDao;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        long startTime = System.currentTimeMillis();
        markerDao.listMarkerBz2MD5(false);
        itemDao.listAllItemBz2Md5();
        iconTagDao.listAllTagBz2Md5();
        log.info("完成缓存初始化，花费{}", System.currentTimeMillis() - startTime);
    }
}
