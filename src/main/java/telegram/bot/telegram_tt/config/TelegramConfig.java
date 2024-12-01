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
 * Конфигурационный класс для инициализации Telegram бота.
 */
@Configuration
@Slf4j
public class TelegramConfig {

    /**
     * Бин для создания и регистрации Telegram бота.
     *
     * @param botName имя бота
     * @param token токен бота
     * @param addCategoryCommand команда добавления категории
     * @param viewCategoryCommand команда для просмотра категорий
     * @param removeCategoryCommand команда для удаления категории
     * @param uploadCommand команда для загрузки данных
     * @param downloadCommand команда для скачивания данных
     * @param categoryDownloadService сервис для скачивания категорий
     * @param categoryUploadService сервис для загрузки категорий
     * @return TelegramBot экземпляр бота
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

        // Создание экземпляра бота с необходимыми командами и сервисами
        TelegramBot telegramBot = new TelegramBot(botName, token, addCategoryCommand, viewCategoryCommand,
                removeCategoryCommand, uploadCommand, downloadCommand,
                categoryDownloadService, categoryUploadService, defaultCommandResponseFactory);

        try {
            // Регистрация бота в Telegram API
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);
            log.info("Telegram bot registered successfully.");
        } catch (TelegramApiException e) {
            // Логирование ошибки регистрации бота
            log.error("Exception during Telegram bot registration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register Telegram bot.", e);
        }
        return telegramBot;
    }
}
