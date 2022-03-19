# soa-mafia

## TODO

1. Реализовать базовое клиент-серверное приложение «Привет Мафия» на основе технологии gRPC.
    - Клиент обеспечивает:
        - [ ] Установку имени пользователя
        - [ ] Подключение к серверу по сетевому имени/адресу
        - [ ] Отображение списка подключившихся игроков
    - Сервер обеспечивает:
        - [ ] Подключение игроков
        - [ ] Рассылку уведомлений о подключившихся/отключившихся игроках
2. Реализовать базовое приложение «Боты мафии». Оно реализует все возможности приложения «Привет Мафия», а также:
    - Клиент обеспечивает:
        - [ ] Автоматический вход в сессию игры, когда набирается достаточное число игроков
        - [ ] Отображение состояния игрока и действий, происходящих в игре
        - [ ] Случайный выбор действий среди возможных на каждом этапе игры
    - Сервер обеспечивает:
        - [ ] Создание одной сессии игры при подключении достаточного количества игроков
        - [ ] Назначение игрокам случайных ролей в соответствии с требованиями
        - [ ] Получение от игроков выбранных действий, их выполнение и изменение состояния игроков
        - [ ] Учет статуса игры и завершение игры при выигрыше мирных жителей или мафии
3. Модифицировать клиент и сервер таким образом, чтобы они обеспечивали:
    - [ ] Возможность общения игроков внутри сессии, с учетом состояния игры:
      днем все игроки могут свободно общаться, ночью могут общаться только игроки мафии между собой, «духи» отключаются
      от общения до новой сессии игры. Для реализации общения можно использовать сервис чата, разработанный в предыдущей
      работе.
    - [ ] Одновременное ведение нескольких сеансов игры при подключении достаточного количества игроков.