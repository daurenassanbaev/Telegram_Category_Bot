package telegram.bot.telegram_tt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import telegram.bot.telegram_tt.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository for working with the Category entity.
 * Contains methods for searching categories by various criteria.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Method to find category by name and chat ID.
     *
     * @param name category name
     * @param chatId chat ID
     * @return Optional<Category> - category if found
     */
    Optional<Category> findByNameAndChatId(String name, Long chatId);

    /**
     * Method to get all root categories for a given chat.
     * Root categories are those that do not have a parent category.
     *
     * @param chatId chat ID
     * @return List<Category> - list of root categories
     */
    List<Category> findByParentIsNullAndChatId(Long chatId);
}
