package org.nop.visa.appointment.scheduler.configuration;

import org.nop.visa.appointment.scheduler.configuration.properties.ChromeProperties;
import org.nop.visa.appointment.scheduler.configuration.properties.TelegramProperties;
import org.nop.visa.appointment.scheduler.configuration.properties.UserVisaProperties;
import org.nop.visa.appointment.scheduler.logic.UserActions;
import org.nop.visa.appointment.scheduler.support.ExternalClient;
import org.nop.visa.appointment.scheduler.support.Telegram;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfiguration {

    @Bean
    @ConfigurationProperties("chrome")
    public ChromeProperties chromeProperties() {
        return ChromeProperties.builder().build();
    }

    @Bean(destroyMethod = "close")
    public WebDriver driver(ChromeProperties chromeProperties) {
        System.setProperty("webdriver.chrome.driver", chromeProperties.getPath());
        ChromeOptions options = new ChromeOptions();
        options.addArguments(chromeProperties.getArguments());
        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(chromeProperties.getUiTimeout());
        return driver;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ExternalClient externalClient(RestTemplate restTemplate) {
        return new ExternalClient(restTemplate);
    }

    @Bean
    @ConfigurationProperties("telegram")
    public TelegramProperties telegramProperties() {
        return TelegramProperties.builder().build();
    }

    @Bean
    public Telegram telegram(ExternalClient externalClient, TelegramProperties telegramProperties) {
        return new Telegram(externalClient, telegramProperties);
    }

    @Bean
    @ConfigurationProperties("visa.user")
    public UserVisaProperties userVisaProperties() {
        return UserVisaProperties.builder().build();
    }

    @Bean
    public UserActions userAction(WebDriver driver, UserVisaProperties userVisaProperties,
                                  ExternalClient externalClient) {
        return new UserActions(driver, externalClient, userVisaProperties);
    }
}
