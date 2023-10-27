package site.yuanshen.data.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ByteUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.vo.MarkerLinkageVo;

import java.nio.ByteOrder;
import java.util.*;
import java.util.stream.Collectors;

public class MarkerLinkageDataHelper {
    public static void reverseLinkageIds(List<MarkerLinkageVo> linkageVos) {
        for(MarkerLinkageVo linkageVo : linkageVos) {
            final Long fromId = ObjectUtil.defaultIfNull(linkageVo.getFromId(), 0L);
            final Long toId = ObjectUtil.defaultIfNull(linkageVo.getToId(), 0L);
            final Boolean linkReverse = ObjectUtil.defaultIfNull(linkageVo.getLinkReverse(), false);

            if(linkReverse) {
                linkageVo.setFromId(toId);
                linkageVo.setToId(fromId);
                linkageVo.setLinkReverse(false);
            }
        }
    }


    public static List<Long> getLinkIdList(List<MarkerLinkageVo> linkageVos) {
        // 生成用到的 ID
        final Set<Long> idSet = new HashSet<>();
        for(MarkerLinkageVo linkage : linkageVos) {
            idSet.add(linkage.getFromId());
            idSet.add(linkage.getToId());
        }
        return idSet.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static Map<String, MarkerLinkage> getLinkSearchMap(List<MarkerLinkage> linkageList) {
        final Map<String, MarkerLinkage> searchMap = new HashMap<>();
        for(MarkerLinkage linkageEntity : linkageList) {
            final String idHash = MarkerLinkageDataHelper.getIdHash(Arrays.asList(linkageEntity.getFromId(), linkageEntity.getToId()));
            searchMap.put(idHash, linkageEntity);
        }
        return searchMap;
    }

    public static Map<String, MarkerLinkage> patchLinkSearchMap(Map<String, MarkerLinkage> linkageMap, List<MarkerLinkageVo> linkageVos, String groupId) {
        // 先设置所有的关联为删除，后续对新增关联开启，以便复用现有关联
        linkageMap.replaceAll((hash, v) -> {
            v.setDelFlag(true);
            return v;
        });

        for(MarkerLinkageVo linkageVo : linkageVos) {
            Long fromId = linkageVo.getFromId();
            Long toId = linkageVo.getToId();
            if(fromId == null || toId == null) {
                continue;
            }
            boolean dirReverse = false;
            if(fromId.compareTo(toId) > 0) {
                fromId = fromId ^ toId;
                toId = fromId ^ toId;
                fromId = fromId ^ toId;
                dirReverse = true;
            }
            final String idHash = MarkerLinkageDataHelper.getIdHash(Arrays.asList(linkageVo.getFromId(), linkageVo.getToId()));
            final MarkerLinkage linkageItem = linkageMap.getOrDefault(idHash, new MarkerLinkage());
            linkageItem.setGroupId(groupId);
            linkageItem.setFromId(fromId);
            linkageItem.setToId(toId);
            linkageItem.setLinkAction(StrUtil.blankToDefault(linkageVo.getLinkAction(), ""));
            linkageItem.setLinkReverse(dirReverse);
            linkageItem.setPath(CollUtil.defaultIfEmpty(linkageVo.getPath(), List.of()));
            linkageItem.setDelFlag(false);

            linkageMap.put(idHash, linkageItem);
        }

        return linkageMap;
    }

    public static String getIdHash(List<Long> idList) {
        idList = idList.stream().map(id -> ObjectUtil.defaultIfNull(id, 0L)).sorted().collect(Collectors.toList());
        int byteSize = Long.BYTES * idList.size();
        byte[] bytes = new byte[byteSize];

        for(int i = 0; i < idList.size(); i++) {
            Long id = idList.get(i);
            byte[] idBytes = ByteUtil.longToBytes(id, ByteOrder.BIG_ENDIAN);
            System.arraycopy(idBytes, 0, bytes, i * Long.BYTES, Long.BYTES);
        }
        final String idHash = SecureUtil.md5(Arrays.toString(bytes));
        return idHash;
    }

}
