package site.yuanshen.common.core.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TimeWrapper {
    public static TimeWrapper create() {
        return new TimeWrapper();
    }

    Timestamp time;

    public Timestamp getTime() {
        return time;
    }

    public TimeWrapper setTime() {
        this.time = TimeUtils.getCurrentTimestamp();
        return this;
    }

    public TimeWrapper setTime(Date dt) {
        this.time = TimeUtils.toTimestamp(dt);
        return this;
    }

    public TimeWrapper setTime(LocalDateTime dt, String tz) {
        this.time = TimeUtils.toTimestamp(dt, tz);
        return this;
    }

    public TimeWrapper setTime(LocalDateTime dt, ZoneId tz) {
        this.time = TimeUtils.toTimestamp(dt, tz);
        return this;
    }

    public TimeWrapper setHour(int hour) {
        this.time = TimeUtils.toMillisecond(this.time, hour, null, null, null);
        return this;
    }

    public TimeWrapper setMinute(int minute) {
        this.time = TimeUtils.toMillisecond(this.time, null, minute, null, null);
        return this;
    }

    public TimeWrapper setSecond(int second) {
        this.time = TimeUtils.toMillisecond(this.time, null, null, second, null);
        return this;
    }

    public TimeWrapper setMs(int ms) {
        this.time = TimeUtils.toMillisecond(this.time, null, null, null, ms);
        return this;
    }

    public TimeWrapper toFirstSecond() {
        this.time = TimeUtils.toFirstSecondOfDay(this.time);
        return this;
    }

    public TimeWrapper toFirstMs() {
        this.time = TimeUtils.toFirstSecondOfDay(this.time);
        return this;
    }

    public TimeWrapper toLastSecond() {
        this.time = TimeUtils.toLastSecondOfDay(this.time);
        return this;
    }

    public TimeWrapper toLastMs() {
        this.time = TimeUtils.toLastSecondOfDay(this.time);
        return this;
    }

    public TimeWrapper offsetSecond(long offset) {
        this.time = TimeUtils.toTimeOffsetInSecond(this.time, offset);
        return this;
    }

    public TimeWrapper offsetMs(long offset) {
        this.time = TimeUtils.toTimeOffsetInMillisecond(this.time, offset);
        return this;
    }

    public String format(String format) {
        return TimeUtils.formatTime(this.time, format);
    }
}
