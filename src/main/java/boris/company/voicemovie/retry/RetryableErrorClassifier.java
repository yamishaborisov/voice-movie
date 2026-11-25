package boris.company.voicemovie.retry;

import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import software.amazon.awssdk.core.exception.SdkServiceException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Component
public class RetryableErrorClassifier {

    public boolean isRetryable(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (isNetworkError(cause)) {
                return true;
            }

            Integer statusCode = extractStatusCode(cause);

            if (statusCode != null) {
                return isRetryableStatus(statusCode);
            }
        }

        return false;
    }

    private boolean isNetworkError(Throwable error) {
        return error instanceof ResourceAccessException
                || error instanceof AnthropicIoException
                || error instanceof SocketTimeoutException
                || error instanceof ConnectException
                || error instanceof UnknownHostException;
    }

    private Integer extractStatusCode(Throwable error) {
        return switch (error) {
            case RestClientResponseException e -> e.getStatusCode().value();
            case AnthropicServiceException e -> e.statusCode();
            case SdkServiceException e -> e.statusCode();
            default -> null;
        };
    }

    private boolean isRetryableStatus(int status) {
        return status == 408
                || status == 429
                || (status >= 500 && status < 600);
    }

}
