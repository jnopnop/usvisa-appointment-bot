package org.nop.visa.appointment.scheduler.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ExternalClient {
    private final RestTemplate restTemplate;

    @Retryable(
            maxAttempts = 5,
            backoff = @Backoff(
                    random = true,
                    delay = 500,
                    maxDelay = 10_000,
                    multiplier = 2
            ))
    public <RES> ResponseEntity<RES> request(String url, HttpMethod method, Map<String, String> headers) {
        HttpHeaders requestHeaders = new HttpHeaders();
        headers.forEach(requestHeaders::add);
        HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);
        return restTemplate.exchange(url, method, requestEntity, new ParameterizedTypeReference<>(){});
    }

    @Retryable(
            maxAttempts = 5,
            backoff = @Backoff(
                    random = true,
                    delay = 500,
                    maxDelay = 10_000,
                    multiplier = 2
            ))
    public <REQ, RES> ResponseEntity<RES> request(String url, HttpMethod method, REQ body,
                                                  Map<String, String> headers) {
        HttpHeaders requestHeaders = new HttpHeaders();
        headers.forEach(requestHeaders::add);
        HttpEntity<REQ> requestEntity = new HttpEntity<>(body, requestHeaders);
        return restTemplate.exchange(url, method, requestEntity, new ParameterizedTypeReference<>(){});
    }

    @Retryable(
            maxAttempts = 5,
            backoff = @Backoff(
                    random = true,
                    delay = 500,
                    maxDelay = 10_000,
                    multiplier = 2
            ))
    public <REQ> ResponseEntity<?> requestSimple(String url, HttpMethod method, REQ body,
                                           Map<String, String> headers) {
        HttpHeaders requestHeaders = new HttpHeaders();
        headers.forEach(requestHeaders::add);
        HttpEntity<REQ> requestEntity = new HttpEntity<>(body, requestHeaders);
        return restTemplate.exchange(url, method, requestEntity, Void.class);
    }
}
