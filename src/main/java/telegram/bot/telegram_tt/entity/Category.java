package telegram.bot.telegram_tt.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import telegram.bot.telegram_tt.composite.CategoryComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность для представления категории в базе данных.
 * Маппинг на таблицу category в базе данных.
 */

// Здесь используется Builder Pattern
@Entity
@Table(name = "category")
@Getter
@Setter
public class Category implements CategoryComponent {

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

    public Category() {

    }

    private Category(Long id, String name, Long chatId, Category parent, List<Category> children) {
        this.id = id;
        this.name = name;
        this.chatId = chatId;
        this.parent = parent;
        this.children = children;
    }

    public static CategoryBuilder builder() {
        return new CategoryBuilder();
    }

    @Override
    public void addChild(CategoryComponent child) {
        if (child instanceof Category) {
            this.children.add((Category) child);
        }
    }


    public static class CategoryBuilder {
        private Long id;
        private String name;
        private Long chatId;
        private Category parent;
        private List<Category> children;

        CategoryBuilder() {
        }

        public CategoryBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CategoryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CategoryBuilder chatId(Long chatId) {
            this.chatId = chatId;
            return this;
        }

        public CategoryBuilder parent(Category parent) {
            this.parent = parent;
            return this;
        }

        public CategoryBuilder children(List<Category> children) {
            this.children = children;
            return this;
        }

        public Category build() {
            return new Category(this.id, this.name, this.chatId, this.parent, this.children);
        }

        public String toString() {
            return "Category.CategoryBuilder(id=" + this.id + ", name=" + this.name + ", chatId=" + this.chatId + ", parent=" + this.parent + ", children=" + this.children + ")";
        }
    }
}
