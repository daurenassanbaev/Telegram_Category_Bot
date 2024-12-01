package telegram.bot.telegram_tt.composite;

import java.util.List;

public interface CategoryComponent {
    String getName();
    CategoryComponent getParent();
    List<CategoryComponent> getChildren();
    void addChild(CategoryComponent child);
}
