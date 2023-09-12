package org.nop.visa.appointment.scheduler.configuration.properties;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;
import org.springframework.util.StringUtils;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramProperties {
    @NotBlank
    String baseUri;
    @NotBlank
    String token;
    @NotBlank
    String chatId;
    String statusMessageId;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(statusMessageId)) {
            log.warn("telegram.statusMessageId hasn't been provided, " +
                    "current appointment status will be sent as a new message. \n" +
                    "To avoid annoying notifications note the first messageId produced by the bot and " +
                    "set telegram.statusMessageId to it. " +
                    "This way you'll get notifications only if a sooner appointment is found");
        }
    }

    public String getUrl(String apiMethod) {
        return baseUri + "/bot" + token + "/" + apiMethod;
    }
}
