package telegram.bot.telegram_tt.command;

import java.io.IOException;
import java.io.InputStream;

// This is an interface for upload/download commands, in the implementations you can see the implementation of Command Pattern
public interface FileCommand extends Command{
    String executeFile(InputStream update, Long chatId) throws IOException;
}
