package telegram.bot.telegram_tt.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность для представления категории в базе данных.
 * Маппинг на таблицу category в базе данных.
 */
@Entity
@Table(name = "category")
@Getter
@Setter
public class Category {

    /**
     * Идентификатор категории (первичный ключ).
     * Генерация значения происходит автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название категории.
     */
    private String name;

    /**
     * Идентификатор чата, с которым связана категория.
     */
    private Long chatId;

    /**
     * Родительская категория.
     * Связь Many-to-One, т.е. одна категория может иметь одного родителя.
     */
    @ManyToOne
    private Category parent;

    /**
     * Список дочерних категорий.
     * Связь One-to-Many, т.е. одна категория может иметь несколько дочерних категорий.
     * CascadeType.ALL означает, что все операции с родителем будут касаться и дочерних категорий.
     * orphanRemoval = true означает, что если дочерняя категория больше не связана с родителем, она будет удалена.
     * FetchType.EAGER означает, что дочерние категории будут загружаться сразу при загрузке родительской категории.
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Category> children = new ArrayList<>();

}
