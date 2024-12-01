package telegram.bot.telegram_tt.command;

import java.io.IOException;
import java.io.InputStream;

// Это интерфейс для upload/download commands, в имплементациях можно увидеть реализацию Command Pattern
public interface FileCommand extends Command{
    String executeFile(InputStream update, Long chatId) throws IOException;
}
