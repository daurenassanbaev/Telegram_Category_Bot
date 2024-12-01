package telegram.bot.telegram_tt.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity to represent a category in the database.
 * Mapping to the category table in the database.
 */

// This uses the Builder Pattern
@Entity
@Table(name = "category")
@Getter
@Setter
public class Category {

    /**
     * Category ID (primary key).
     * Value is generated automatically.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Category name.
     */
    private String name;

    /**
     * The ID of the chat the category is associated with.
     */
    private Long chatId;

    /**
     * Parent category.
     * Many-to-One relationship, i.e. one category can have one parent.
     */
    @ManyToOne
    private Category parent;

    /**
     * List of child categories.
     * One-to-Many relationship, i.e. one category can have several child categories.
     * CascadeType.ALL means that all operations with the parent will also affect the child categories.
     * orphanRemoval = true means that if the child category is no longer associated with the parent, it will be removed.
     * FetchType.EAGER means that the child categories will be loaded immediately when the parent category is loaded.
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
