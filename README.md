



# Управление иерархией категорий

Это Telegram-бот, который позволяет пользователям создавать, просматривать и удалять дерево категорий. Бот также поддерживает работу с Excel-документами для загрузки и скачивания дерева категорий.

## Используемые технологии
- **Java**
- **Spring Boot**
- **Postgres + Flyway** (для миграций базы данных)
- **Maven** (для сборки проекта)

## Основные функциональные возможности

- **/viewTree** — отображает дерево категорий в структурированном виде.
- **/addElement <название элемента>** — добавляет новый элемент. Если не указан родитель, элемент становится корневым.
- **/addElement <родительский элемент> <дочерний элемент>** — добавляет дочерний элемент к указанному родителю. Если родительский элемент не найден, выводится соответствующее сообщение.
- **/removeElement <название элемента>** — удаляет указанный элемент и все его дочерние элементы. Если элемент не найден, выводится соответствующее сообщение.
- **/help** — отображает список доступных команд с их кратким описанием.
- **/download** — скачивает Excel-документ с деревом категорий.
- **/upload** — принимает Excel-документ с деревом категорий и сохраняет все элементы в базе данных.

## Запуск

1. Выполните команду для сборки проекта:
```bash
./mvnw clean package -DskipTests
```

2. Чтобы запустить проект с помощью Docker Compose:

    - Сначала создайте файл `.env` и укажите в нем настройки:
   ```bash
   SPRING_DATASOURCE_URL=YOUR_URL
   SPRING_DATASOURCE_USERNAME=YOUR_USERNAME
   SPRING_DATASOURCE_PASSWORD=YOUR_PASSWORD
   TG_BOT_NAME=YOUR_BOT_NAME
   TG_BOT_TOKEN=YOUR_BOT_TOKEN
   ```

    - Затем выполните команду для запуска:
   ```bash
   docker-compose --env-file .env up --build
   ```

## Примечания

- Убедитесь, что у вас установлен Docker и Docker Compose.
- Для работы с PostgreSQL необходимо настроить подключение к базе данных в `.env` файле.