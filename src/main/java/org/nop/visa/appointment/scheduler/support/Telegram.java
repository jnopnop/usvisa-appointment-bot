package org.nop.visa.appointment.scheduler.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nop.visa.appointment.scheduler.configuration.properties.TelegramProperties;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import java.util.Map;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
public class Telegram {
    private final ExternalClient httpClient;
    private final TelegramProperties properties;

    public void updateStatus(String status) {
        if (StringUtils.hasText(properties.getStatusMessageId())) {
            httpClient.request(properties.getUrl("editMessageText"), HttpMethod.POST, Map.of(
                    "chat_id", properties.getChatId(),
                    "message_id", properties.getStatusMessageId(),
                    "text", status
            ), Map.of(
                    CONTENT_TYPE, APPLICATION_JSON_VALUE,
                    ACCEPT, APPLICATION_JSON_VALUE
            ));
        } else {
            log.warn("Updating status via sending a new message. I warned you. " +
                    "You do really want to set telegram.statusMessageId !");
            sendMessage(status);
        }
    }

    public void sendMessage(String message) {
        httpClient.request(properties.getUrl("sendMessage"), POST, Map.of(
                "chat_id", properties.getChatId(),
                "text", message
        ), Map.of(
                CONTENT_TYPE, APPLICATION_JSON_VALUE,
                ACCEPT, APPLICATION_JSON_VALUE
        ));
    }
}
