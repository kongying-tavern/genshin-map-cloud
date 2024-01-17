package site.yuanshen.data.helper.notice;

import cn.hutool.core.img.ColorUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.nodes.Attribute;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NoticeDataHelper {
    public static Map<String, String> getStyleAttrs(Attribute styleAttr) {
        Map<String, String> styleAttrs = new HashMap<>();
        if(styleAttr == null) {
            return styleAttrs;
        }

        String styleVal = StrUtil.blankToDefault(styleAttr.getValue(), "");
        return getStyleAttrs(styleVal);
    }

    public static Map<String, String> getStyleAttrs(String styleValue) {
        String styleStr = StrUtil.blankToDefault(styleValue, "");
        List<String> styleLines = StrUtil.split(styleStr, ";");
        Map<String, String> styleAttrs = new HashMap<>();

        for(String styleLine : styleLines) {
            final List<String> styleLineParts = StrUtil.split(styleLine, ':', 2);
            final String styleKey = StrUtil.trim(styleLineParts.size() >= 1 ? styleLineParts.get(0) : "");
            final String styleVal = StrUtil.trim(styleLineParts.size() >= 2 ? styleLineParts.get(1) : "");
            if(StrUtil.isAllNotBlank(styleKey, styleVal)) {
                styleAttrs.put(styleKey, styleVal);
            }
        }
        return styleAttrs;
    }

    public static String colorToHex(String colorStr) {
        colorStr = StrUtil.blankToDefault(colorStr, "");
        colorStr = StrUtil.trim(colorStr);

        Pattern colorPattern = null;
        List<Pair<String, Function<Matcher, String>>> patterns = List.of(
                // #RGB
                ImmutablePair.of("^#([0-9a-f]){3}$", m -> {
                    final int red = HexUtil.hexToInt(m.group(1)) * 0x011;
                    final int green = HexUtil.hexToInt(m.group(2)) * 0x011;
                    final int blue = HexUtil.hexToInt(m.group(3)) * 0x011;
                    return ColorUtil.toHex(red, green, blue);
                }),
                // #RGBA
                ImmutablePair.of("^#([0-9a-f]){4}", m -> {
                    final int red = HexUtil.hexToInt(m.group(1)) * 0x011;
                    final int green = HexUtil.hexToInt(m.group(2)) * 0x011;
                    final int blue = HexUtil.hexToInt(m.group(3)) * 0x011;
                    final int alpha = HexUtil.hexToInt(m.group(4)) * 0x011;
                    return ColorUtil.toHex(red, green, blue) + StrUtil.format("%02X", alpha);
                }),
                // #RRGGBB
                ImmutablePair.of("^#([0-9a-f]{6})$", m -> {
                    final int colorInt = HexUtil.hexToInt(m.group(1));
                    final Color color = ColorUtil.getColor(colorInt);
                    return ColorUtil.toHex(color);
                }),
                // #RRGGBBAA
                ImmutablePair.of("^#([0-9a-f]{6})([0-9a-f]{2})$", m -> {
                    final int colorInt = HexUtil.hexToInt(m.group(1));
                    final int alpha = HexUtil.hexToInt(m.group(2));
                    final Color color = ColorUtil.getColor(colorInt);
                    return ColorUtil.toHex(color) + StrUtil.format("%02X", alpha);
                }),
                // rgb(R, G, B)
                ImmutablePair.of("^rgb\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$", m -> {
                    try {
                        int red = NumberUtil.parseInt(m.group(1));
                        int green = NumberUtil.parseInt(m.group(2));
                        int blue = NumberUtil.parseInt(m.group(3));
                        return ColorUtil.toHex(red, green, blue);
                    } catch(Exception ex) {
                        return "";
                    }
                }),
                // rgba(R, G, B, A)
                ImmutablePair.of("^rgba\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*([01]|0?\\.\\d+)\\s*\\)$", m -> {
                    try {
                        int red = NumberUtil.parseInt(m.group(1));
                        int green = NumberUtil.parseInt(m.group(2));
                        int blue = NumberUtil.parseInt(m.group(3));
                        int alpha = ((int) (NumberUtil.parseDouble(m.group(4)) * 255)) & 0x0FF;
                        return ColorUtil.toHex(red, green, blue) + StrUtil.format("%02X", alpha);
                    } catch(Exception ex) {
                        return "";
                    }
                })
        );
        for(Pair<String, Function<Matcher, String>> patternConf : patterns) {
            final String patternStr = patternConf.getLeft();
            final Function<Matcher, String> patternDealer = patternConf.getRight();
            if(StrUtil.isBlank(patternStr) || patternDealer == null) {
                continue;
            }

            final Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            final Matcher match = pattern.matcher(colorStr);
            if(match.matches()) {
                return patternDealer.apply(match);
            }
        }
        return "";
    }

    public static String sizeToNumber(String sizeStr) {
        if(StrUtil.isBlank(sizeStr)) {
            return "";
        }
        try {
            final int sizeInt = NumberUtil.parseInt(sizeStr);
            final String sizeNum = String.valueOf(sizeInt);
            return sizeNum;
        } catch (Exception e) {
            return "";
        }
    }
}
