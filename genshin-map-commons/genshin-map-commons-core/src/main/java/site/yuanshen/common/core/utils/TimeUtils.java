package site.yuanshen.common.core.utils;

import cn.hutool.core.util.StrUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
    public static final int ONE_DAY_SECOND = 86400;
    public static final int ONE_DAY_MILLISECOND = ONE_DAY_SECOND * 1000;
    public static final String DEFAULT_ZONE_OFFSET = "+08:00";

    /**
     * 解析时间
     *
     * @param time 时间文本
     * @return 时间
     */
    public static Timestamp parseTime(String time) {
        LocalDateTime ldt = null;
        // yyyy-MM-dd HH:mm:ss.SSS
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            ldt = LocalDateTime.parse(time, df);
        } catch (Exception e) {
        }
        if (ldt != null) return new Timestamp(ldt.toEpochSecond(ZoneOffset.of(DEFAULT_ZONE_OFFSET)) * 1000L);

        // yyyy-MM-dd HH:mm:ss
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ldt = LocalDateTime.parse(time, df);
        } catch (Exception e) {
        }
        if (ldt != null) return new Timestamp(ldt.toEpochSecond(ZoneOffset.of(DEFAULT_ZONE_OFFSET)) * 1000L);

        // yyyy-MM-dd HH:mm:ss +0800
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss +0800");
            ldt = LocalDateTime.parse(time, df);
        } catch (Exception e) {
        }
        if (ldt != null) return new Timestamp(ldt.toEpochSecond(ZoneOffset.of(DEFAULT_ZONE_OFFSET)) * 1000L);

        // yyyy-MM-dd'T'HH:mm:ss.SSS
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            ldt = LocalDateTime.parse(time, df);
        } catch (Exception e) {
        }
        if (ldt != null) return new Timestamp(ldt.toEpochSecond(ZoneOffset.of(DEFAULT_ZONE_OFFSET)) * 1000L);

        // yyyy-MM-dd'T'HH:mm:ss
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            ldt = LocalDateTime.parse(time, df);
        } catch (Exception e) {
        }
        if (ldt != null) return new Timestamp(ldt.toEpochSecond(ZoneOffset.of(DEFAULT_ZONE_OFFSET)) * 1000L);

        return null;
    }

    /**
     * 获取当前时间戳
     *
     * @return 时间戳
     */
    public static Timestamp getCurrentTimestamp() {
        final Date date = new Date();
        final Timestamp now = new Timestamp(date.getTime());
        return now;
    }

    /**
     * 转换为时间戳
     *
     * @param dt 日期
     * @return 时间戳
     */
    public static Timestamp toTimestamp(Date dt) {
        final Timestamp ts = dt == null ? null : new Timestamp(dt.getTime());
        return ts;
    }

    /**
     * 转换为时间戳
     *
     * @param dt 本地时间对象
     * @param tz 时区标识
     * @return 时间戳
     */
    public static Timestamp toTimestamp(LocalDateTime dt, String tz) {
        final String tzKey = StrUtil.emptyToDefault(tz, "");
        final ZoneId tzId = ZoneId.of(tzKey);
        return toTimestamp(dt, tzId);
    }

    /**
     * 时间戳
     *
     * @param dt   本地时间对象
     * @param tzId 时区标识
     * @return 时间戳
     */
    public static Timestamp toTimestamp(LocalDateTime dt, ZoneId tzId) {
        if (dt == null) {
            return null;
        }
        final long tsVal = dt.atZone(tzId).toInstant().toEpochMilli();
        final Timestamp ts = new Timestamp(tsVal);
        return ts;
    }

    /**
     * 格式化时间
     *
     * @param ts     时间戳
     * @param format 格式
     * @return 格式化的时间
     */
    public static String formatTime(Timestamp ts, String format) {
        if (format == null || "".equals(format)) {
            return "";
        } else {
            try {
                final SimpleDateFormat formatter = new SimpleDateFormat(format);
                final String formatted = formatter.format(ts);
                return formatted;
            } catch (Exception e) {
                return "";
            }
        }
    }

    /**
     * 格式化时间
     *
     * @param dt     日期
     * @param format 格式
     * @return 格式化的时间
     */
    public static String formatTime(Date dt, String format) {
        final Timestamp ts = toTimestamp(dt);
        return TimeUtils.formatTime(ts, format);
    }

    /**
     * 调整到对应的毫秒位置
     *
     * @param ts          时间戳
     * @param hour        小时数，为 null 则不修改
     * @param minute      分钟数，为 null 则不修改
     * @param second      秒数，为 null 则不修改
     * @param millisecond 毫秒数，为 null 则不修改
     * @return 调整后的时间戳
     */
    public static Timestamp toMillisecond(Timestamp ts, Integer hour, Integer minute, Integer second, Integer millisecond) {
        if (ts == null) {
            return null;
        }

        final Date dt = new Date(ts.getTime());
        final Calendar c = Calendar.getInstance();
        c.setTime(dt);
        if (hour != null) {
            c.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute != null) {
            c.set(Calendar.MINUTE, minute);
        }
        if (second != null) {
            c.set(Calendar.SECOND, second);
        }
        if (millisecond != null) {
            c.set(Calendar.MILLISECOND, millisecond);
        }
        final Date dtParsed = c.getTime();
        final Timestamp tsParsed = new Timestamp(dtParsed.getTime());

        return tsParsed;
    }

    /**
     * 获取当天最后一毫秒
     *
     * @param ts 时间戳
     * @return 当天最后一毫秒的时间戳
     */
    public static Timestamp toLastMillisecondOfDay(Timestamp ts) {
        return toMillisecond(ts, 23, 59, 59, 999);
    }

    /**
     * 获取当天第一秒
     *
     * @param ts 时间戳
     * @return 当天第一秒的时间戳
     */
    public static Timestamp toFirstSecondOfDay(Timestamp ts) {
        return toMillisecond(ts, 0, 0, 0, 0);
    }

    /**
     * 获取当天最后一秒
     *
     * @param ts 时间戳
     * @return 当天第一毫秒的时间戳
     */
    public static Timestamp toLastSecondOfDay(Timestamp ts) {
        return toMillisecond(ts, 23, 59, 59, 0);
    }

    /**
     * 进行时间偏移量计算
     *
     * @param ts     时间戳
     * @param offset 偏移量
     * @param radix  与毫秒的缩放比率
     * @return 偏移后的时间戳
     */
    private static Timestamp toTimeOffset(Timestamp ts, Long offset, int radix) {
        if (ts == null) {
            return null;
        } else if (offset == null) {
            return ts;
        }
        final Date dt = new Date(ts.getTime());
        final Date dtParsed = new Date(dt.getTime() + offset * radix);
        final Timestamp tsParsed = new Timestamp(dtParsed.getTime());
        return tsParsed;
    }

    /**
     * 进行时间毫秒偏移量计算
     *
     * @param ts     时间戳
     * @param offset 偏移量（毫秒）
     * @return 偏移后的时间
     */
    public static Timestamp toTimeOffsetInMillisecond(Timestamp ts, Long offset) {
        return toTimeOffset(ts, offset, 1);
    }

    /**
     * 进行时间秒级偏移量计算
     *
     * @param ts
     * @param offset
     * @return
     */
    public static Timestamp toTimeOffsetInSecond(Timestamp ts, Long offset) {
        return toTimeOffset(ts, offset, 1000);
    }

    /**
     * 判断时间区间是否存在重合
     *
     * @param firstRangeStart          时间区域1起始时间
     * @param firstRangeEnd            时间区域1终止时间
     * @param firstRangeStartIncluded  是否包含时间区域1起始时间
     * @param firstRangeEndIncluded    是否包含时间区域1终止时间
     * @param secondRangeStart         时间区域2起始时间
     * @param secondRangeEnd           时间区域2终止时间
     * @param secondRangeStartIncluded 是否包含时间区域2起始时间
     * @param secondRangeEndIncluded   是否包含时间区域2终止时间
     * @return 判断结果
     */
    public static boolean timeRangeIntersected(
        Timestamp firstRangeStart,
        Timestamp firstRangeEnd,
        boolean firstRangeStartIncluded,
        boolean firstRangeEndIncluded,
        Timestamp secondRangeStart,
        Timestamp secondRangeEnd,
        boolean secondRangeStartIncluded,
        boolean secondRangeEndIncluded
    ) {
        return (
            TimeUtils.timeInRange(firstRangeStart, secondRangeStart, secondRangeEnd, secondRangeStartIncluded, secondRangeEndIncluded) ||
                TimeUtils.timeInRange(firstRangeEnd, secondRangeStart, secondRangeEnd, secondRangeStartIncluded, secondRangeEndIncluded) ||
                TimeUtils.timeInRange(secondRangeStart, firstRangeStart, firstRangeEnd, firstRangeStartIncluded, firstRangeEndIncluded) ||
                TimeUtils.timeInRange(secondRangeEnd, firstRangeStart, firstRangeEnd, firstRangeStartIncluded, firstRangeEndIncluded)
        );
    }

    /**
     * 判断时间是否在时间区域内
     *
     * @param ts           时间戳
     * @param rangeStart   时间区域起始时间
     * @param rangeEnd     时间区域终止时间
     * @param includeStart 是否包含起始时间
     * @param includeEnd   是否包含终止时间
     * @return 判断结果
     */
    public static boolean timeInRange(Timestamp ts, Timestamp rangeStart, Timestamp rangeEnd, boolean includeStart, boolean includeEnd) {
        if (ts == null) {
            return false;
        } else if (rangeStart == null && rangeEnd == null) {
            return true;
        } else if (rangeStart == null && rangeEnd != null) {
            if (rangeEnd.equals(ts) && includeEnd) {
                return true;
            } else if (rangeEnd.after(ts)) {
                return true;
            } else {
                return false;
            }
        } else if (rangeStart != null && rangeEnd == null) {
            if (rangeStart.equals(ts) && includeStart) {
                return true;
            } else if (rangeStart.before(ts)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (rangeStart.equals(ts) && includeStart) {
                return true;
            } else if (rangeEnd.equals(ts) && includeEnd) {
                return true;
            } else if (rangeStart.before(ts) && rangeEnd.after(ts)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
