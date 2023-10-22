package site.yuanshen.common.core.exception;

public class GenshinApiException extends RuntimeException {
    public GenshinApiException() {
    }

    public GenshinApiException(String message) {
        super(message);
    }

    public GenshinApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenshinApiException(Throwable cause) {
        super(cause);
    }

    protected GenshinApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
