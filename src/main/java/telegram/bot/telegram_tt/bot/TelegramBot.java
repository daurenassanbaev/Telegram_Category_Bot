package telegram.bot.telegram_tt.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.bot.telegram_tt.command.*;
import telegram.bot.telegram_tt.factory.CommandResponseFactory;
import telegram.bot.telegram_tt.service.CategoryDownloadService;
import telegram.bot.telegram_tt.service.CategoryUploadService;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Бот Telegram для управления деревьями категорий.
 * Поддерживает добавление, просмотр, удаление категорий и загрузку/выгрузку деревьев категорий.
 */
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final String botName;
    private final Map<String, Command> commands = new HashMap<>();
    private final CommandResponseFactory commandResponseFactory;

    @Value("${images.first}")
    private String PHOTO_PATH;
    @Value("${images.second}")
    private String PHOTO_PATH_1;
    private final Set<Long> waiting = new HashSet<>();

    public TelegramBot(String botName, String token, AddCategoryCommand addCategoryCommand, ViewCategoryCommand viewCategoryCommand, RemoveCategoryCommand removeCategoryCommand, UploadCommand uploadCommand, DownloadCommand downloadCommand, CategoryDownloadService categoryDownloadService, CategoryUploadService categoryUploadService, CommandResponseFactory commandResponseFactory) {
        super(token);
        this.botName = botName;
        commands.put("/addElement", addCategoryCommand);
        commands.put("/viewTree", viewCategoryCommand);
        commands.put("/removeElement", removeCategoryCommand);
        commands.put("/download", downloadCommand);
        commands.put("/upload", uploadCommand);
        this.commandResponseFactory = commandResponseFactory;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            if (message.hasText()) {
                String messageText = message.getText();
                String response = "";
                // Обрабатываем команду help
                if (messageText.equals("/help")) {
                    response = commandResponseFactory.createResponse(messageText);
                } else if (messageText.equals("/start")) {
                    response = commandResponseFactory.createResponse(messageText);
                } else if (messageText.equals("/upload")) {
                    response = commandResponseFactory.createResponse(messageText);
                    waiting.add(chatId); // Ожидаем загрузку файла от пользователя
                } else {
                    // Обработка других команд
                    Object responseObject = handleCommand(messageText, chatId);
                    if (responseObject instanceof SendDocument) {
                        try {
                            execute((SendDocument) responseObject);
                            return;
                        } catch (TelegramApiException e) {
                            log.error("Ошибка отправки документа: ", e);
                            response = "Ошибка отправки документа. Попробуйте еще раз.";
                        }
                    } else {
                        response = (String) responseObject;
                    }
                }
                sendMessage(chatId, response);
            } else if (update.getMessage().hasDocument() && waiting.contains(chatId)) {
                waiting.remove(chatId); // Убираем пользователя из списка ожидания
                String fileId = message.getDocument().getFileId();
                GetFile getFileMethod = new GetFile(fileId);
                try {
                    File file = execute(getFileMethod);
                    if (checkUploadFileFormat(file.getFilePath())) {
                        InputStream inputStream = downloadFileAsStream(file);
                        Command uploadCommand = commands.get("/upload");
                        if (uploadCommand instanceof FileCommand) {
                            String response = ((FileCommand) uploadCommand).executeFile(inputStream, chatId);
                            inputStream.close();
                            sendMessage(chatId, response);
                        } else {
                            sendMessage(chatId, "Команда загрузки настроена неправильно.");
                        }
                    } else {
                        sendMessage(chatId, "Ошибка: Пожалуйста, загрузите файл в формате Excel (.xls или .xlsx).");
                    }

                } catch (TelegramApiException | IOException e) {
                    log.error("Ошибка при загрузке или обработке файла", e);
                    sendMessage(chatId, "Произошла ошибка при загрузке файла. Попробуйте позже.");
                }
            } else {
                sendMessage(chatId, "Некорректная команда. Список комманд вы можете узнать через команду /help");
            }
        }
    }

    private boolean checkUploadFileFormat(String filePath) {
        if (filePath != null && (filePath.endsWith(".xls") || filePath.endsWith(".xlsx"))) {
            return true;
        }
        return false;
    }


    /**
     * Обрабатывает входящую команду на основе карты команд.
     *
     * @param messageText текст команды
     * @param chatId идентификатор чата
     * @return ответное сообщение
     */
    private Object handleCommand(String messageText, Long chatId) {
        for (String commandKey : commands.keySet()) {
            if (messageText.startsWith(commandKey)) {
                try {
                    Object forReturn = commands.get(commandKey).execute(messageText, chatId);
                    if (forReturn instanceof SendDocument) {
                        SendDocument sendDocument = (SendDocument) forReturn;
                        return sendDocument;
                    }
                    return forReturn.toString();
                } catch (Exception e) {
                    log.error("Ошибка выполнения команды: ", e);
                    return "Ошибка выполнения команды. Попробуйте еще раз.";
                }
            }
        }
        return commandResponseFactory.createResponse(messageText);
    }



    /**
     * Отправляет сообщение в определенный чат.
     *
     * @param chatId идентификатор чата
     * @param text текст сообщения
     */
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            if (text.equals("/upload")) {
                sendPhoto(chatId, PHOTO_PATH); // Отправляем фото для загрузки
                sendPhoto(chatId, PHOTO_PATH_1);
            }
            execute(message); // Отправляем сообщение
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: ", e);
            // Если ошибка при отправке сообщения, отправляем текст с ошибкой пользователю
            sendMessage(chatId, "Произошла ошибка при отправке сообщения. Попробуйте позже.");
        }
    }
    private void sendPhoto(long chatId, String photoPath) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(new java.io.File(photoPath)));
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке фото: ", e);
            throw new RuntimeException("Ошибка при отправке фото.");
        }
    }


    @Override
    public String getBotUsername() {
        return this.botName;
    }
}
