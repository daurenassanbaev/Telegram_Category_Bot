package telegram.bot.telegram_tt.factory;

// Это базовый интерфейс для Factory Method Pattern
public interface CommandResponseFactory {
    String createResponse(String messageText);
}
