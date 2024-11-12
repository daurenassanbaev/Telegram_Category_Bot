package telegram.bot.telegram_tt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import telegram.bot.telegram_tt.entity.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameAndChatId(String name, Long chatId);
    List<Category> findByParentIsNullAndChatId(Long chatId);
}
