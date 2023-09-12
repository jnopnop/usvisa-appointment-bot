package org.nop.visa.appointment.scheduler.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVisaProperties {
    @NotBlank
    String username;
    @NotBlank
    String password;
    @NotBlank
    String scheduleId;
    @NotBlank
    String facilityId;
}
