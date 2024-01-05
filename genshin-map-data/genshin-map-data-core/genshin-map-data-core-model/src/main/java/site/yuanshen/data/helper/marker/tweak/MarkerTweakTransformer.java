package site.yuanshen.data.helper.marker.tweak;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import site.yuanshen.common.core.utils.JsonUtils;
import site.yuanshen.data.vo.MarkerItemLinkVo;
import site.yuanshen.data.vo.adapter.marker.tweak.TweakConfigMetaVo;

import java.util.*;
import java.util.stream.Collectors;

public final class MarkerTweakTransformer {
    public static Object applyUpdate(Object data, TweakConfigMetaVo meta) {
        Object newVal = meta.getValue();
        if(newVal == null) {
            return data;
        } else {
            return newVal;
        }
    }

    public static String applyReplace(String data, TweakConfigMetaVo meta) {
        String testStr = meta.getTest();
        String replaceStr = StrUtil.nullToEmpty(meta.getReplace());
        if(StrUtil.isBlank(testStr)) {
            return data;
        }
        final String dataNew = StrUtil.replace(data, testStr, replaceStr);
        return dataNew;
    }

    public static String applyReplaceRegex(String data, TweakConfigMetaVo meta) {
        String testStr = meta.getTest();
        String replaceStr = StrUtil.nullToEmpty(meta.getReplace());
        if(StrUtil.isBlank(testStr)) {
                return data;
        }
        final String dataNew = ReUtil.replaceAll(data, testStr, replaceStr);
        return dataNew;
    }

    public static String applyPrepend(String data, TweakConfigMetaVo meta) {
        String value = "";
        try {
            value = StrUtil.nullToEmpty((String) meta.getValue());
        } catch (Exception ex) {
            value = "";
        }
        final String dataNew = value + data;
        return dataNew;
    }

    public static String applyAppend(String data, TweakConfigMetaVo meta) {
        String value = "";
        try {
            value = StrUtil.nullToEmpty((String) meta.getValue());
        } catch (Exception e) {
            // skip
        }
        final String dataNew = data + value;
        return dataNew;
    }

    public static String applyTrimLeft(String data, TweakConfigMetaVo meta) {
        final String dataNew = StrUtil.trimStart(data);
        return dataNew;
    }

    public static String applyTrimRight(String data, TweakConfigMetaVo meta) {
        final String dataNew = StrUtil.trimEnd(data);
        return dataNew;
    }

    public static String applyRemoveLeft(String data, TweakConfigMetaVo meta) {
        final String testStr = StrUtil.nullToEmpty(meta.getTest());
        final String dataNew = StrUtil.removePrefix(data, testStr);
        return dataNew;
    }

    public static String applyRemoveRight(String data, TweakConfigMetaVo meta) {
        final String testStr = StrUtil.nullToEmpty(meta.getTest());
        final String dataNew = StrUtil.removeSuffix(data, testStr);
        return dataNew;
    }

    public static Map<String, Object> applyMerge(Map<String, Object> data, TweakConfigMetaVo meta) {
        final Map<String, Object> map = meta.getMap();
        final Map<String, Object> dataNew = JsonUtils.merge(data, map);
        return dataNew;
    }

    public static List<MarkerItemLinkVo> applyUpdateItemList(List<MarkerItemLinkVo> data, TweakConfigMetaVo meta) {
        final List<MarkerItemLinkVo> itemList = meta.getItemList();
        if(itemList == null) {
            return data;
        } else {
            return itemList;
        }
    }

    public static List<MarkerItemLinkVo> applyInsertItemListIfAbsent(List<MarkerItemLinkVo> data, TweakConfigMetaVo meta) {
        final List<MarkerItemLinkVo> itemList = meta.getItemList();
        if(itemList == null) {
            return data;
        } else if(data == null) {
            return null;
        }

        Map<Long, MarkerItemLinkVo> itemMap = new HashMap<>();
        // Add base items
        for(MarkerItemLinkVo item : data) {
            itemMap.putIfAbsent(item.getItemId(), item);
        }
        // Add insertion items
        for(MarkerItemLinkVo item : itemList) {
            itemMap.putIfAbsent(item.getItemId(), item);
        }
        final List<MarkerItemLinkVo> newList = new ArrayList<>(itemMap.values());
        return newList;
    }

    public static List<MarkerItemLinkVo> applyInsertItemListOrUpdate(List<MarkerItemLinkVo> data, TweakConfigMetaVo meta) {
        final List<MarkerItemLinkVo> itemList = meta.getItemList();
        if(itemList == null) {
            return data;
        } else if(data == null) {
            return null;
        }

        Map<Long, MarkerItemLinkVo> itemMap = new HashMap<>();
        // Add base items
        for(MarkerItemLinkVo item : data) {
            itemMap.put(item.getItemId(), item);
        }
        // Add insertion items
        for(MarkerItemLinkVo item : itemList) {
            itemMap.put(item.getItemId(), item);
        }
        final List<MarkerItemLinkVo> newList = new ArrayList<>(itemMap.values());
        return newList;
    }

    public static List<MarkerItemLinkVo> applyRemoveItemListItem(List<MarkerItemLinkVo> data, TweakConfigMetaVo meta) {
        final List<MarkerItemLinkVo> itemList = meta.getItemList();
        if(itemList == null) {
            return data;
        } else if(data == null) {
            return null;
        }

        Set<Long> itemIdSet = new HashSet<>();
        // Generate id set
        for(MarkerItemLinkVo item : itemList) {
            itemIdSet.add(item.getItemId());
        }
        // Delete items
        final List<MarkerItemLinkVo> newList = data.stream()
                .filter(item -> item.getItemId() != null && !itemIdSet.contains(item.getItemId()))
                .collect(Collectors.toList());
        return newList;
    }
}
