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
    String path = "/usr/local/bin/chromedriver";

    @NotNull
    @Builder.Default
    Duration uiTimeout = Duration.ofSeconds(30);

    @NotNull
    @Builder.Default
    List<String> arguments = List.of(
            "--window-size=1440,900",
            "--ignore-certificate-errors",
            "--silent",
            "--headless" // remove this arg to see the browser
    );
}
