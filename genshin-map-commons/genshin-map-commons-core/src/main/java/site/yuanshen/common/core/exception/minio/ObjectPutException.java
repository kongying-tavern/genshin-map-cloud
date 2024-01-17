package site.yuanshen.common.core.exception.minio;

public class ObjectPutException extends RuntimeException {
    public ObjectPutException() {
    }

    public ObjectPutException(String message) {
        super(message);
    }

    public ObjectPutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectPutException(Throwable cause) {
        super(cause);
    }

    protected ObjectPutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
