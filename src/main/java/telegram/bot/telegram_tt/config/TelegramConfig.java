package telegram.bot.telegram_tt.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import telegram.bot.telegram_tt.bot.TelegramBot;
import telegram.bot.telegram_tt.command.*;
import telegram.bot.telegram_tt.service.CategoryDownloadService;
import telegram.bot.telegram_tt.service.CategoryUploadService;

/**
 * Configuration class for initializing the Telegram Bot.
 */
@Configuration
@Slf4j
public class TelegramConfig {

    @Bean
    public TelegramBot telegramBot(@Value("${bot.name}") String botName,
                                   @Value("${bot.token}") String token,
                                   AddCategoryCommand addCategoryCommand,
                                   ViewCategoryCommand viewCategoryCommand,
                                   RemoveCategoryCommand removeCategoryCommand,
                                   UploadCommand uploadCommand,
                                   DownloadCommand downloadCommand,
                                   CategoryDownloadService categoryDownloadService,
                                   CategoryUploadService categoryUploadService) {
        TelegramBot telegramBot = new TelegramBot(botName, token, addCategoryCommand, viewCategoryCommand,
                removeCategoryCommand, uploadCommand, downloadCommand,
                categoryDownloadService,  categoryUploadService);
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            log.error("Exception during Telegram bot registration: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return telegramBot;
    }
}
