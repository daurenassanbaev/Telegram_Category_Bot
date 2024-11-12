package telegram.bot.telegram_tt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import telegram.bot.telegram_tt.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Category.
 * Содержит методы для поиска категорий по различным критериям.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Метод для поиска категории по имени и идентификатору чата.
     *
     * @param name имя категории
     * @param chatId идентификатор чата
     * @return Optional<Category> - категория, если найдена
     */
    Optional<Category> findByNameAndChatId(String name, Long chatId);

    /**
     * Метод для получения всех корневых категорий для заданного чата.
     * Корневые категории - те, у которых нет родительской категории.
     *
     * @param chatId идентификатор чата
     * @return List<Category> - список корневых категорий
     */
    List<Category> findByParentIsNullAndChatId(Long chatId);
}
