package site.yuanshen.genshin.core.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import site.yuanshen.genshin.core.service.CacheService;

/**
 * 监听应用启动完成后，写入缓存
 *
 * @author Moment
 */
@Component
@RequiredArgsConstructor
public class CacheInitListener implements ApplicationListener<ApplicationReadyEvent> {

    private final CacheService cacheService;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        cacheService.cleanMarkerCache();
    }
}
