package org.nop.visa.appointment.scheduler.logic;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.nop.visa.appointment.scheduler.configuration.properties.UserVisaProperties;
import org.nop.visa.appointment.scheduler.support.ExternalClient;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.function.ThrowingSupplier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
@RequiredArgsConstructor
public class UserActions {
    private static final String LOGIN_URL = "https://ais.usvisa-info.com/en-ca/niv/users/sign_in";
    private static final String APPOINTMENT_URL = "https://ais.usvisa-info.com/en-ca/niv/schedule/${scheduleId}/appointment";
    private static final String DATE_URL = "https://ais.usvisa-info.com/en-ca/niv/schedule/${scheduleId}/appointment/days/${facilityId}.json?&appointments[expedite]=false";
    private static final String TIME_URL = "https://ais.usvisa-info.com/en-ca/niv/schedule/${scheduleId}/appointment/times/${facilityId}.json?date=${date}&appointments[expedite]=false";
    private static final Pattern DATE_TIME_PATTERN = Pattern.compile(".+: (.+,.+,\\s.+?)\\s.+");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM, yyyy, HH:mm");

    private final WebDriver driver;
    private final ExternalClient externalClient;
    private final UserVisaProperties props;

    public LocalDateTime login() {
        withDelay(() -> driver.get(LOGIN_URL));
        withDelay(() ->
                driver.findElement(By.xpath("//a[@class=\"down-arrow bounce\"]"))
                        .click());

        withDelay(() ->
                driver.findElement(By.id("user_email"))
                        .sendKeys(props.getUsername()));
        withDelay(() ->
                driver.findElement(By.id("user_password"))
                        .sendKeys(props.getPassword()));
        withDelay(() ->
                driver.findElement(By.className("icheckbox"))
                        .click());
        withDelay(() ->
                driver.findElement(By.name("commit"))
                        .click());

        return getCurrentAppointment();
    }

    private LocalDateTime getCurrentAppointment() {
        var currAppointmentString = driver.findElement(By.className("consular-appt")).getText();
        Matcher matcher = DATE_TIME_PATTERN.matcher(currAppointmentString);
        if (!matcher.find()) {
            throw new RuntimeException("Could not find current appointment");
        }
        return LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER);
    }

    public List<String> getAvailableDates() {
        ResponseEntity<List<Map<String, Object>>> response = externalClient.request(userURL(DATE_URL), GET, buildCommonHeaders());
        return response.getBody().stream()
                .map(entry -> (String) entry.get("date"))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public List<String> findAvailableTimes(String date) {
        ResponseEntity<Map<String, Object>> response = externalClient
                .request(userURL(TIME_URL, Map.of("date", date)), GET, buildCommonHeaders());
        return ((List<String>) response.getBody().get("available_times"))
                .stream()
                .toList();
    }

    public boolean tryBook(String date, String time) {
        try {
            log.info("Trying to book {}, {}", date, time);
            driver.get(userURL(APPOINTMENT_URL));

            // For group appointment see https://github.com/dvalbuena1/visa_rescheduler_aws/blob/main/visa.py#L176-L180
            var payload = new LinkedMultiValueMap<String, String>();
            payload.add("utf8", driver.findElement(By.name("utf8")).getAttribute("value"));
            payload.add("authenticity_token", driver.findElement(By.name("authenticity_token")).getAttribute("value"));
            payload.add("confirmed_limit_message", driver.findElement(By.name("use_consulate_appointment_capacity")).getAttribute("value"));
            payload.add("appointments[consulate_appointment][facility_id]", props.getFacilityId());
            payload.add("appointments[consulate_appointment][date]", date);
            payload.add("appointments[consulate_appointment][time]", time);

            var headers = buildCommonHeaders();
            headers.remove("Accept");
            headers.remove("X-Requested-With");
            headers.put("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);

            return withDelay(() -> {
                var response = externalClient.requestSimple(userURL(APPOINTMENT_URL), POST, payload, headers);
                return !response.getStatusCode().isError();
            });
        } catch (Exception e) {
            log.error("Failed to book {}, {}", date, time);
            return false;
        }
    }

    private String userURL(String urlTemplate) {
        return userURL(urlTemplate, Map.of());
    }

    private String userURL(String urlTemplate, Map<String, String> additionalMapping) {
        var replacementMap = new HashMap<>(Map.of(
                "scheduleId", props.getScheduleId(),
                "facilityId", props.getFacilityId()));
        replacementMap.putAll(additionalMapping);
        return StrSubstitutor.replace(urlTemplate, replacementMap);
    }

    private Map<String, String> buildCommonHeaders() {
        var headers = new HashMap<String, String>();
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("User-Agent", ((ChromeDriver) driver).executeScript("return navigator.userAgent").toString());
        headers.put("Referer", userURL(APPOINTMENT_URL));
        headers.put("Cookie", "_yatri_session=" + driver.manage().getCookieNamed("_yatri_session").getValue());

        return headers;
    }

    @SneakyThrows
    public static <T> T withDelay(ThrowingSupplier<T> supplier) {
        var delayMillis = ThreadLocalRandom.current().nextInt(100, 1000);
        TimeUnit.MILLISECONDS.sleep(delayMillis);
        return supplier.get();
    }

    @SneakyThrows
    public static void withDelay(ThrowingRunnable runnable) {
        var delayMillis = ThreadLocalRandom.current().nextInt(100, 1000);
        TimeUnit.MILLISECONDS.sleep(delayMillis);
        runnable.run();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {

        void run() throws Exception;
    }
}
