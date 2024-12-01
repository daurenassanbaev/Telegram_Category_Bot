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
 * Telegram bot for managing category trees.
 * Supports adding, viewing, deleting categories and uploading/downloading category trees.
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
                if (messageText.equals("/help")) {
                    response = commandResponseFactory.createResponse(messageText);
                } else if (messageText.equals("/start")) {
                    response = commandResponseFactory.createResponse(messageText);
                } else if (messageText.equals("/upload")) {
                    response = commandResponseFactory.createResponse(messageText);
                    waiting.add(chatId); // Wait for the user to upload a file
                } else {
                    // Processing other commands
                    Object responseObject = handleCommand(messageText, chatId);
                    if (responseObject instanceof SendDocument) {
                        try {
                            execute((SendDocument) responseObject);
                            return;
                        } catch (TelegramApiException e) {
                            log.error("Error sending document: ", e);
                            response = "Error sending document. Try again.";
                        }
                    } else {
                        response = (String) responseObject;
                    }
                }
                sendMessage(chatId, response);
            } else if (update.getMessage().hasDocument() && waiting.contains(chatId)) {
                waiting.remove(chatId); // Remove the user from the waiting list
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
                            sendMessage(chatId, "The boot command is not configured correctly.");
                        }
                    } else {
                        sendMessage(chatId, "Error: Please upload a file in Excel format (.xls or .xlsx).");
                    }

                } catch (TelegramApiException | IOException e) {
                    log.error("Error loading or processing file", e);
                    sendMessage(chatId, "There was an error loading the file. Please try again later.");
                }
            } else {
                sendMessage(chatId, "Incorrect command. You can find the list of commands using the /help command");
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
     * Processes an incoming command based on the command map.
     *
     * @param messageText command text
     * @param chatId chat ID
     * @return response message
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
                    log.error("Error executing command: ", e);
                    return "Error executing command. Try again.";
                }
            }
        }
        return commandResponseFactory.createResponse(messageText);
    }



    /**
     * Sends a message to a specific chat.
     *
     * @param chatId chat ID
     * @param text message text
     */
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            if (text.equals(commandResponseFactory.createResponse("/upload"))) {
                sendPhoto(chatId, PHOTO_PATH);
                sendPhoto(chatId, PHOTO_PATH_1);
            }
            execute(message); // Sending a message
        } catch (TelegramApiException e) {
            log.error("Error sending message: ", e);
            // If there is an error sending the message, send the text with the error to the user
            sendMessage(chatId, "There was an error sending your message. Try again later.");
        }
    }
    private void sendPhoto(long chatId, String photoPath) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(new java.io.File(photoPath)));
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Error sending photo: ", e);
            throw new RuntimeException("Error sending photo.");
        }
    }


    @Override
    public String getBotUsername() {
        return this.botName;
    }
}
