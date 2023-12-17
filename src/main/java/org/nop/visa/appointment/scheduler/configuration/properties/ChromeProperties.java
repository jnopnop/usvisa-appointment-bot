package org.nop.visa.appointment.scheduler.configuration.properties;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

import java.time.Duration;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChromeProperties {
    @NotBlank
    @Builder.Default
    String chromeDriverPath = "/usr/local/bin/chromedriver";

    @NotBlank
    @Builder.Default
    String chromePath = "/usr/local/bin/chrome";

    @NotNull
    @Builder.Default
    Duration uiTimeout = Duration.ofSeconds(30);

    @NotNull
    @Builder.Default
    List<String> arguments = List.of(
            "--headless=new",
            "--no-sandbox",
            "--disable-gpu",
            "--window-size=1280x1696",
            "--single-process",
            "--disable-dev-shm-usage",
            "--disable-dev-tools",
            "--no-zygote",
            "--remote-debugging-port=9222"
    );
}
