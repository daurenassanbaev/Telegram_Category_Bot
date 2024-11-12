package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.service.CategoryUploadService;

import java.io.*;
import java.util.LinkedHashMap;

@Component
@RequiredArgsConstructor
public class UploadCommand implements FileCommand {
    private final CategoryUploadService categoryUploadService;

    @Override
    public String executeFile(InputStream inputStream, Long chatId) throws IOException {
        LinkedHashMap<String, String> map = categoryUploadService.getCategoriesFromExcelFile(inputStream);
        return categoryUploadService.addAllCategories(map, chatId);
    }

    @Override
    public Object execute(String command, Long chatId) {
        return null;
    }
}
