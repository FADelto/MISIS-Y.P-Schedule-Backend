# Бэкенд приложения для просмотра расписания
Frontend:
https://github.com/FADelto/MISIS-Y.P-Schedule-Frontend

Макет приложения:
https://www.figma.com/file/AdPtt8IgBkfQvuebVXsEMv/Schedule-app?type=design&node-id=0%3A1&mode=design&t=2rfRbSg3QKpedQyE-1
## Функции:
- Тестирование приложения и документация API приложения в Swagger 
- Получение расписания с Google Sheets при помощи Google API
- Получение новостей и информации с Яндекс Пачки при помощи Selenium
- Хранение новостей и расписания в PostgreSQL
- Сборка проекта при помощи Gradle
- Контейнеризация приложения при помощи Docker
## TO-DO List:
 - [x] Получение расписания с Google Sheets при помощи Google API
 - [ ] Обновление расписания в базе данных
 - [x] Получения новостей с пачки
 - [ ] Добавление новых новостей в базу данных
 - [ ] Создание документации к API в Swagger
 - [x]  Подготовка к контейнеризации приложения в Docker (Создание двух фазной сборки)
## Для запуска:
 Получить credetials для работы с Google Sheets API в [Google API](https://console.cloud.google.com/apis/credentials)
- Положить файл в директорию authFiles
- Если запуск будет не через Docker то создать базу данных и указать в application.properties данные для подключения
- Можно запустить через IDE или с помощью Docker

Для запуска при помощи Docker:
  
  Создать образ:
  ```docker compose build```
  
  Запуск приложения:
  ```docker compose up```
