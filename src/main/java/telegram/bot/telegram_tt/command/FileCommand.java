package telegram.bot.telegram_tt.command;

import java.io.IOException;
import java.io.InputStream;

public interface FileCommand extends Command{
    String executeFile(InputStream update, Long chatId) throws IOException;
}
