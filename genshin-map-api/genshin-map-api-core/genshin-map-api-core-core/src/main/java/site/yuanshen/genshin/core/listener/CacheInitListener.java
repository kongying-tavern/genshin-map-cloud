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
        markerDocService.refreshMarkerBinaryMD5();
        markerDocService.listAllMarkerBinaryMD5();
        markerLinkageDocService.refreshMarkerLinkageListBinaryMD5();
        markerLinkageDocService.listMarkerLinkageBinaryMD5();
        markerLinkageDocService.refreshMarkerLinkageGraphBinaryMD5();
        markerLinkageDocService.graphMarkerLinkageBinaryMD5();
        itemDocService.refreshItemBinaryMD5();
        itemDocService.listItemBinaryMD5();
        iconTagDao.listAllTagBinaryMd5();
        log.info("完成缓存初始化，花费{}", System.currentTimeMillis() - startTime);
    }
}
