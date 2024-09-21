package site.yuanshen.data.helper.marker.linkage;

import site.yuanshen.data.dto.adapter.marker.linkage.graph.AccumulatorCache;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.dto.adapter.marker.linkage.graph.LinkRefDto;

import java.util.List;

public final class MarkerLinkageGraphAccumulator {
    public static void withTrigger(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        accumulateOneToOneTrigger(caches, linkageVo);
    }

    public static void withTriggerAll(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        accumulateAnyToAnyTrigger(caches, linkageVo);
    }

    public static void withTriggerAny(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        accumulateAnyToAnyTrigger(caches, linkageVo);
    }

    public static void withRelated(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        accumulateGroup(caches, linkageVo);
    }

    public static void withDirected(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        accumulateGroup(caches, linkageVo);
    }

    public static void withPathUniDir(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        accumulateGroup(caches, linkageVo);
    }

    public static void withPathBiDir(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        accumulateGroup(caches, linkageVo);
    }

    public static void withEquivalent(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        final LinkRefDto linkRef = MarkerLinkageDataHelper.getLinkRef(linkageVo);
        AccumulatorCache ac = null;

        // Try to find existing matched accumulator cache
        for (AccumulatorCache cache : caches) {
            if (cache.inLinkage(linkRef, true, true)) {
                ac = cache;
                break;
            }
        }

        // No accumulator cache matched, add a new accumulator cache
        if (ac == null) {
            ac = new AccumulatorCache();
            caches.add(ac);
        }

        // Add data
        ac.addLinkage(linkRef);
        ac.addPath(linkageVo);
    }

    /**
     * --------------------------------------------------
     * Helper Functions
     * --------------------------------------------------
     */
    private static void accumulateOneToOneTrigger(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        final LinkRefDto linkRef = MarkerLinkageDataHelper.getLinkRef(linkageVo);
        final AccumulatorCache ac = new AccumulatorCache();
        ac.addLinkage(linkRef);
        ac.addPath(linkageVo);
        caches.add(ac);
    }

    private static void accumulateAnyToAnyTrigger(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        final LinkRefDto linkRef = MarkerLinkageDataHelper.getLinkRef(linkageVo);
        AccumulatorCache ac = null;

        // Try to find existing matched accumulator cache
        for (AccumulatorCache cache : caches) {
            final Long toId = linkageVo.getToId();
            if (cache.inLinkage(null, toId, false, true)) {
                ac = cache;
                break;
            }
        }

        // No accumulator cache matched, add a new accumulator cache
        if (ac == null) {
            ac = new AccumulatorCache();
            caches.add(ac);
        }

        // Add data
        ac.addLinkage(linkRef);
        ac.addPath(linkageVo);
    }

    private static void accumulateGroup(List<AccumulatorCache> caches, MarkerLinkageVo linkageVo) {
        final LinkRefDto linkRef = MarkerLinkageDataHelper.getLinkRef(linkageVo);
        AccumulatorCache ac = null;

        // Try to find existing matched accumulator cache
        for (AccumulatorCache cache : caches) {
            if (cache.inLinkage(linkRef, true, true)) {
                ac = cache;
                break;
            }
        }

        // No accumulator cache matched, add a new accumulator cache
        if (ac == null) {
            ac = new AccumulatorCache();
            caches.add(ac);
        }

        // Add data
        ac.addLinkage(linkRef);
        ac.addPath(linkageVo);
    }
}
