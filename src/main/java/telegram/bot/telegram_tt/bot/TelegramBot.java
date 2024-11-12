package telegram.bot.telegram_tt.bot;

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
import telegram.bot.telegram_tt.service.CategoryDownloadService;
import telegram.bot.telegram_tt.service.CategoryUploadService;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Telegram bot for managing category trees.
 * Supports adding, viewing, removing categories, and downloading/uploading category trees.
 */
public class TelegramBot extends TelegramLongPollingBot {

    private final String botName;
    private final Map<String, Command> commands = new HashMap<>();

    private static final String RULES = """
            Команды:
            
            1) /viewTree - 📜 Показать текущее дерево категорий в структурированном формате.
            
            2) /addElement <название элемента> - ➕ Добавить элемент как корневой, если у него нет родителя.
            
            3) /addElement <родительский элемент> <дочерний элемент> - ➕ Добавить дочерний элемент к существующему родителю. Если родителя нет, будет показано сообщение об ошибке.
            
            4) /removeElement <название элемента> - 🗑️ Удалить указанный элемент и все его дочерние элементы. Если элемент не найден, будет показано сообщение об ошибке.
            
            5) /help - ℹ️ Показать список команд и их описание.
            
            6) /download - 📥 Скачать документ Excel с деревом категорий.
            
            7) /upload - 📤 Загрузить документ Excel с деревом категорий и сохранить все элементы в базе данных.
            """;

    private static final String START_MESSAGE = """
            Добро пожаловать в Category Bot! 👋
            Этот бот позволяет создавать, просматривать и удалять дерево категорий.
            Напишите "/help", чтобы узнать, какие команды доступны.
            """;

    private static final String UNKNOWN_COMMAND = "❌ Неизвестная команда. Напишите /help, чтобы увидеть доступные команды.";

    private static final String UPLOAD_MESSAGE = """
            ⚠️ При отправке убедитесь, что объекты уникальны. В колонке "Категория" не должно быть дубликатов. Формат листа должен быть, как на фотках.(2 фото означает, что имя таблицы должна быть "Category Tree") :
            
            "-" - означает отсутствие родительской категории.
            
            Пожалуйста, отправьте ваш файл, я с удовольствием обработаю его 😊
            """;

    private static final String PHOTO_PATH = "/Users/daurenassanbaev/Downloads/telegram-tt/image/img.png";
    private static final String PHOTO_PATH_1 = "/Users/daurenassanbaev/Downloads/telegram-tt/image/img_1.png";
    private final Set<Long> waiting = new HashSet<>();

    public TelegramBot(String botName, String token, AddCategoryCommand addCategoryCommand, ViewCategoryCommand viewCategoryCommand, RemoveCategoryCommand removeCategoryCommand, UploadCommand uploadCommand, DownloadCommand downloadCommand, CategoryDownloadService categoryDownloadService, CategoryUploadService categoryUploadService) {
        super(token);
        this.botName = botName;
        commands.put("/addElement", addCategoryCommand);
        commands.put("/viewTree", viewCategoryCommand);
        commands.put("/removeElement", removeCategoryCommand);
        commands.put("/download", downloadCommand);
        commands.put("/upload", uploadCommand);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            if (message.hasText()) {
                String messageText = message.getText();
                String response = "";
                if (messageText.equals("/help")) {
                    response = RULES;
                } else if (messageText.equals("/start")) {
                    response = START_MESSAGE;
                } else if (messageText.equals("/upload")) {
                    response = UPLOAD_MESSAGE;
                    waiting.add(chatId);
                } else {
                    Object responseObject = handleCommand(messageText, chatId);
                    if (responseObject instanceof SendDocument) {
                        try {
                            execute((SendDocument) responseObject);
                            return;
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        response = (String) responseObject;
                    }
                }
                sendMessage(chatId, response);
            } else if (update.getMessage().hasDocument() && waiting.contains(chatId)) {
                waiting.remove(chatId);
                String fileId = message.getDocument().getFileId();
                GetFile getFileMethod = new GetFile(fileId);
                try {
                    File file = execute(getFileMethod);
                    InputStream inputStream = downloadFileAsStream(file);
                    Command uploadCommand = commands.get("/upload");
                    if (uploadCommand instanceof FileCommand) {
                        String response = ((FileCommand) uploadCommand).executeFile(inputStream, chatId);
                        inputStream.close();
                        sendMessage(chatId, response);
                    } else {
                        sendMessage(chatId, "Команда загрузки настроена неправильно.");
                    }
                } catch (TelegramApiException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    /**
     * Handles the incoming command based on the command map.
     *
     * @param messageText the command text
     * @param chatId the chat ID
     * @return the response message
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
                    e.printStackTrace();
                    return "Ошибка выполнения команды. Попробуйте еще раз.";
                }
            }
        }
        return UNKNOWN_COMMAND;
    }



    /**
     * Sends a message to a specific chat.
     *
     * @param chatId the chat ID
     * @param text the message text
     */
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            if (text.equals(UPLOAD_MESSAGE)) {
                sendPhoto(chatId, PHOTO_PATH);
                sendPhoto(chatId, PHOTO_PATH_1);
            }
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void sendPhoto(long chatId, String photoPath) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(new java.io.File(photoPath)));
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String getBotUsername() {
        return this.botName;
    }
}