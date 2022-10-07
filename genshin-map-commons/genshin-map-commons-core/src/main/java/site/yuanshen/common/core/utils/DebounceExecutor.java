package site.yuanshen.common.core.utils;

import java.util.concurrent.*;

/**
 * @author Catch
 * @since 2021-10-29
 */
public class DebounceExecutor {

    private static final ScheduledExecutorService SCHEDULE = Executors.newSingleThreadScheduledExecutor();

    // 使用 ConcurrentHashMap 来存储 Future
    private static final ConcurrentHashMap<Object, Future<?>> DELAYED_MAP = new ConcurrentHashMap<>();

    /**
     * 抖动函数
     */
    public static void debounce(final Object key, final Runnable runnable, long delay, TimeUnit unit) {
        final Future<?> prev = DELAYED_MAP.put(key, SCHEDULE.schedule(() -> {
            try {
                runnable.run();
            } finally {
                // 如果任务运行完,则从 map 中移除
                DELAYED_MAP.remove(key);
            }
        }, delay, unit));
        // 如果任务还没运行,则取消任务
        if (prev != null) {
            prev.cancel(true);
        }
    }

    /**
     * 停止运行
     */
    public static void shutdown() {
        SCHEDULE.shutdownNow();
    }

}
