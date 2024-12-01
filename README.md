# Managing category hierarchy

This is a Telegram bot that allows users to create, view, and delete a category tree. The bot also supports working with Excel documents to upload and download the category tree.

## Technologies used
- **Java**
- **Spring Boot**
- **Postgres + Flyway** (for database migrations)
- **Maven** (for building the project)

## Main functionality

- **/viewTree** — displays the category tree in a structured form.
- **/addElement <element name>** — adds a new element. If the parent is not specified, the element becomes the root.
- **/addElement <parent element> <child element>** — adds a child element to the specified parent. If the parent element is not found, a corresponding message is displayed.
- **/removeElement <element name>** — removes the specified element and all its children. If the element is not found, a corresponding message is displayed.
- **/help** — displays a list of available commands with their brief description.
- **/download** — downloads an Excel document with a category tree.
- **/upload** — accepts an Excel document with a category tree and saves all elements in the database.

## Run

1. Run the command to build the project:
```bash
./mvnw clean package -DskipTests
```

2. To run the project with Docker Compose:

- First, create a `.env` file and specify the settings in it:
```bash
SPRING_DATASOURCE_URL=YOUR_URL
SPRING_DATASOURCE_USERNAME=YOUR_USERNAME
SPRING_DATASOURCE_PASSWORD=YOUR_PASSWORD
TG_BOT_NAME=YOUR_BOT_NAME
TG_BOT_TOKEN=YOUR_BOT_TOKEN
```

- Then run the command to run:
```bash
docker-compose --env-file .env up --build
```

## Notes

- Make sure you have Docker installed and Docker Compose.
- To work with PostgreSQL, you need to configure a database connection in the `.env` file.
