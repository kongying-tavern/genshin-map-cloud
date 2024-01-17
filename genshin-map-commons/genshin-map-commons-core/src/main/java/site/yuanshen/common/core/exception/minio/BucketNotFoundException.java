package site.yuanshen.common.core.exception.minio;

public class BucketNotFoundException extends RuntimeException {
    public BucketNotFoundException() {
    }

    public BucketNotFoundException(String message) {
        super(message);
    }

    public BucketNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BucketNotFoundException(Throwable cause) {
        super(cause);
    }

    protected BucketNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
