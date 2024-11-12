package telegram.bot.telegram_tt.service;

import telegram.bot.telegram_tt.entity.Category;
import java.util.List;

public interface CategoryService {
    String addRootCategory(String name, Long chatId);
    String addChildCategory(String name, String child, Long chatId);
    String removeCategory(String name, Long chatId);
    boolean categoryExists(String name, Long chatId);
    List<Category> findByParentIsNullAndChatId(Long chatId);
    String viewCategoryTree(Long chatId);
}
