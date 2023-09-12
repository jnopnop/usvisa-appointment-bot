package org.nop.visa.appointment.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nop.visa.appointment.scheduler.logic.UserActions;
import org.nop.visa.appointment.scheduler.support.Telegram;
import org.nop.visa.appointment.scheduler.util.DateTimePair;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@EnableRetry
@SpringBootApplication
@RequiredArgsConstructor
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private final UserActions userActions;
    private final Telegram telegram;

    private final AtomicReference<LocalDateTime> lastSeen = new AtomicReference<>();

    @Override
    public void run(String... args) {
        log.info("USA Visa Appointment Rescheduler v0.0.1");

        lastSeen.updateAndGet(existing -> {
            var fresh = userActions.login();
            if (existing == null) {
                return fresh;
            }
            if (existing.isBefore(fresh)) {
                return existing;
            }
            return fresh;
        });
        log.info("Current appointment: {}", lastSeen.get());

        List<String> availableDates = userActions.getAvailableDates();
        log.info("Available dates: {}", availableDates);

        availableDates.stream()
                .filter(this::isBefore)
                .flatMap(date -> userActions.findAvailableTimes(date)
                        .stream()
                        .map(time -> DateTimePair.of(date, time))
                )
                .map(this::tryBook)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .ifPresentOrElse(
                        booked -> updateStatus(),
                        this::updateStatus);

        log.info("Done. Till the next time!");
    }

    private void updateStatus() {
        telegram.updateStatus("Appointment: %s\nLast run: %s".formatted(
                lastSeen.get().format(DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
    }

    private boolean isBefore(String candidateDate) {
        return LocalDate.parse(candidateDate).isBefore(lastSeen.get().toLocalDate());
    }

    private Optional<LocalDateTime> tryBook(DateTimePair candidate) {
        var candidateTime = LocalDate.parse(candidate.date()).atTime(LocalTime.parse(candidate.time()));

        if (userActions.tryBook(candidate.date(), candidate.time())) {
            lastSeen.updateAndGet(existing -> {
                if (existing.isBefore(candidateTime)) {
                    return existing;
                }
                return candidateTime;
            });
            telegram.sendMessage(candidate + " booked!");
            return Optional.of(candidateTime);
        }

        telegram.sendMessage(candidate
                + "!!! Hurry Up! I've got paws, can't book an appointment for you yet");

        return Optional.empty();
    }
}
