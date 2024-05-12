package site.yuanshen.common.core.utils;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;
import java.util.List;

/**
 * 客户端辅助方法
 *
 * @author Alex Fang
 */
@Slf4j
public class ClientUtils {
    static class Ip2RegionHolder {
        public static Searcher searcher;

        static {
            String dbPath = "/ip2region/ip2region.xdb";
            byte[] dbBuff;
            try {
                InputStream ris = ClientUtils.class.getResourceAsStream(dbPath);
                dbBuff = FileCopyUtils.copyToByteArray(ris);
                searcher = Searcher.newWithBuffer(dbBuff);
                log.info("加载ip2region数据成功");
            } catch (Exception e) {
                throw new RuntimeException("IP地区数据库加载失败");
            }
        }
    }

    @Data
    @NoArgsConstructor
    public static class Region {
        private static String UNKNOWN_REGION = "未知";
        private static String DEFAULT_REGION_TEXT = "0";

        @Schema(title = "是否是未知地区")
        @JsonProperty("isUnknown")
        private boolean unknown = false;

        @JsonIgnore
        private String hash = "";

        @Schema(title = "国家")
        private String country = "";

        @Schema(title = "地区")
        private String region = "";

        @Schema(title = "省/州")
        private String province = "";

        @Schema(title = "城市")
        private String city = "";

        @Schema(title = "网络运营商")
        private String isp = "";

        public void setFullRegion(String fullRegion) {
            if(fullRegion == null) {
                this.setUnknown(true);
            } else {
                List<String> ipRegionParts = StrUtil.split(fullRegion, "|");
                int ipRegionLevelCount = ipRegionParts.size();
                this.setCountry(ipRegionLevelCount > 0 ? ipRegionParts.get(0) : "");
                this.setRegion(ipRegionLevelCount > 1 ? ipRegionParts.get(1) : "");
                this.setProvince(ipRegionLevelCount > 2 ? ipRegionParts.get(2) : "");
                this.setCity(ipRegionLevelCount > 3 ? ipRegionParts.get(3) : "");
                this.setIsp(ipRegionLevelCount > 4 ? ipRegionParts.get(4) : "");
            }
        }

        public void updateHash() {
            final String fullKey = this.country + "|" + this.region + "|" + this.province + "|" + this.city + "|" + this.isp;
            this.hash = DigestUtils.md5DigestAsHex(fullKey.getBytes());
        }

        public void setUnknown(boolean isUnknown) {
            this.unknown = isUnknown;
            if(isUnknown) {
                this.country = UNKNOWN_REGION;
                this.region = "";
                this.province = "";
                this.city = "";
                this.isp = "";
            }
            this.updateHash();
        }

        private String defaultToEmpty(String text) {
            return DEFAULT_REGION_TEXT.equals(text) ? "" : text;
        }

        public void setCountry(String country) {
            this.country = defaultToEmpty(country);
            this.updateHash();
        }

        public void setRegion(String region) {
            this.region = defaultToEmpty(region);
            this.updateHash();
        }

        public void setProvince(String province) {
            this.province = defaultToEmpty(province);
            this.updateHash();
        }

        public void setCity(String city) {
            this.city = defaultToEmpty(city);
            this.updateHash();
        }

        public void setIsp(String isp) {
            this.isp = defaultToEmpty(isp);
            this.updateHash();
        }
    }

    /**
     * 获取客户端IP区域
     */
    public static Region getClientIpRegion(String ip) {
        Region region = new Region();
        String ipRegion = null;
        try {
            ipRegion = Ip2RegionHolder.searcher.search(ip);
        } catch (Exception e) {}
        region.setFullRegion(ipRegion);
        return region;
    }
}
