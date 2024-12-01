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
import telegram.bot.telegram_tt.factory.DefaultCommandResponseFactory;
import telegram.bot.telegram_tt.service.CategoryDownloadService;
import telegram.bot.telegram_tt.service.CategoryUploadService;

/**
 * Configuration class for initializing Telegram bot.
 */
@Configuration
@Slf4j
public class TelegramConfig {

    /**
     * Bin for creating and registering a Telegram bot.
     *
     * @param botName bot name
     * @param token bot token
     * @param addCategoryCommand command to add a category
     * @param viewCategoryCommand command to view categories
     * @param removeCategoryCommand command to delete a category
     * @param uploadCommand command to upload data
     * @param downloadCommand command to download data
     * @param categoryDownloadService service to download categories
     * @param categoryUploadService service to upload categories
     * @param defaultCommandResponseFactory factory for getting default commands
     * @return TelegramBot bot instance
     */
    @Bean
    public TelegramBot telegramBot(@Value("${bot.name}") String botName,
                                   @Value("${bot.token}") String token,
                                   AddCategoryCommand addCategoryCommand,
                                   ViewCategoryCommand viewCategoryCommand,
                                   RemoveCategoryCommand removeCategoryCommand,
                                   UploadCommand uploadCommand,
                                   DownloadCommand downloadCommand,
                                   CategoryDownloadService categoryDownloadService,
                                   CategoryUploadService categoryUploadService,
                                   DefaultCommandResponseFactory defaultCommandResponseFactory) {
        log.info("Initializing Telegram bot with name: {}", botName);

        TelegramBot telegramBot = new TelegramBot(botName, token, addCategoryCommand, viewCategoryCommand,
                removeCategoryCommand, uploadCommand, downloadCommand,
                categoryDownloadService, categoryUploadService, defaultCommandResponseFactory);

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);
            log.info("Telegram bot registered successfully.");
        } catch (TelegramApiException e) {
            log.error("Exception during Telegram bot registration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register Telegram bot.", e);
        }
        return telegramBot;
    }
}
