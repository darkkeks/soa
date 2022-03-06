# Chat client

## Сборка и запуск

```shell
# build server
./gradlew serverJar

# build client for native platform
./gradlew clientJar

# build client for x86 linux
./gradlew -Plwjgl-natives=natives-linux clientJar

# launch server
java -jar build/libs/*-server.jar

# launch client
java -jar build/libs/*-client.jar
```

## TODO:

- [x] Установку имени пользователя;
- [x] Подключение к серверу по сетевому имени/адресу;
- [x] Отправку голосовых сообщений;
- [x] Получение и воспроизведение сообщений от сервера;
- [x] Отключение от сервера.
---
- [x] Подключение одного пользователя;
- [x] Получение сообщений от пользователя и отправку их обратно;
- [x] Отключение пользователя от сервера.
---
- [x] Общение 2-х и более пользователей одновременно;
- [x] Отправку сообщений от пользователей только в случае наличия сигнала либо только при нажатой
  кнопке (push-to-talk) – на усмотрение разработчика.
- [x] Вывод списка подключенных пользователей, идентификацию говорящего пользователя;
- [x] Актуализацию списка пользователей при подключении и отключении.
---
- [x] Каждый пользователь может быть подключен только к одной комнате
- [x] И слышать сообщения только от тех пользователей, что подключены к этой комнате.
---
- [ ] Поддержать opus
- [ ] Валидировать voice пакеты
- [ ] Закрывать соединения без лишнего шума в логах
- [ ] Позволять клиенту переподключаться