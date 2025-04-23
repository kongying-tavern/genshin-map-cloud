package site.yuanshen.data.enums;


import lombok.Getter;

public enum SocketIOEventEnum {

    MESSAGE("message", 0),
    ;

    @Getter
    private final String event;
    @Getter
    private final int code;

    SocketIOEventEnum(String event, int code) {
        this.event = event;
        this.code = code;
    }

    public static SocketIOEventEnum getItem(String event) {
        for (SocketIOEventEnum item : values()) {
            if (item.getEvent().equals(event)) {
                return item;
            }
        }
        return null;
    }

}
