package site.yuanshen.genshin.core.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import site.yuanshen.genshin.core.dao.IconTagDao;
import site.yuanshen.genshin.core.service.ItemDocService;
import site.yuanshen.genshin.core.service.MarkerDocService;
import site.yuanshen.genshin.core.service.MarkerLinkageDocService;

/**
 * 监听应用启动完成后，写入缓存
 *
 * @author Moment
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheInitListener implements ApplicationListener<ApplicationReadyEvent> {

    private final MarkerDocService markerDocService;
    private final MarkerLinkageDocService markerLinkageDocService;
    private final ItemDocService itemDocService;
    private final IconTagDao iconTagDao;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        long startTime = System.currentTimeMillis();
        markerDocService.refreshMarkerBz2MD5();
        markerDocService.listMarkerBz2MD5();
        markerLinkageDocService.refreshMarkerLinkageListBz2MD5();
        markerLinkageDocService.listMarkerLinkageBz2MD5();
        itemDocService.refreshItemBz2MD5();
        itemDocService.listItemBz2MD5();
        iconTagDao.listAllTagBz2Md5();
        log.info("完成缓存初始化，花费{}", System.currentTimeMillis() - startTime);
    }
}
