# Промежуточная аттестация по курсу Automation QA на Java
Студент: Дудоров И.В.

Написаны автотесты для API Employee.

### Стек:
1. Контрактные тесты: JDBC, RestAssured.
2. Тесты на бизнес-логику: Hibernate, RestAssured.

### Общие замечания:
1. Тесты запускаются в последовательном режиме (параллельное выполнение
пока не работает).
2. Иногда при выполнении тесты падают в связи с недоступностью БД
(обрывы соединения) или неожиданными ответами SC 500 от сервера.
3. Для увеличения стабильности тестов в @BeforeEach и @AfterEach
добавлены задержки 500 мс.
4. Предоставление тестовых данных и инструментов в большинство тестов
реализовано на ParameterResolver'ах.
5. Настройки подключения к DB и API вынесены в, не отслеживаемые GIT,
ресурсы (__для проверки аттестации__ файлы войдут в итоговый Commit).
6. Настройки Hibernate и Junit вынесены в, отслеживаемые GIT,
ресурсы.
7. Все найденные баги подписаны в тестах методом "//TODO: (краткое
описание)". При этом тесты не проходят.
8. Тестовые данные стираются в конце выполнения каждого тестового набора
(класса).


### Контрактные тесты:

__1. Позитивные:__

1.1 Добавление нового сотрудника к компании

1.2 Получение списка сотрудников компании

1.3 Получение сотрудника по id

1.4 Изменение информации о сотруднике


__2. Негативные:__

2.1. Добавление нового сотрудника без авторизации

2.2. Добавление нового сотрудника к отсутствующей компании

2.3. Изменение информации о сотруднике без авторизации

2.4. Изменение информации о сотруднике по несуществующему id

2.5. Получение списка сотрудников несуществующей компании

2.6. Получение списка сотрудников компании, в которой нет сотрудников

2.7. Получение сотрудника по несуществующему id

2.8. Не добавление сотрудника без обязательного поля (набор параметризованных тестов)

2.9. Добавление сотрудника без необязательного поля (набор параметризованных тестов)


### Тесты на бизнес-логику:
__1. Позитивные:__

 1.1 Добавление нового сотрудника к компании

 1.2 Получение списка сотрудников компании

 1.3 Получение сотрудника по id

 1.4 Изменение информации о сотруднике

 1.5 Добавление 5 новых сотрудников к компании

__2. Негативные:__

 2.1 Добавление нового сотрудника без авторизации

 2.2 Добавление нового сотрудника к отсутствующей компании

 2.3 Добавление уже существующего сотрудника (все поля)

 2.4 Добавление сотрудника на уже существующий id

 2.5 Изменение информации о сотруднике без авторизации

 2.6 Изменение информации о сотруднике по несуществующему id

 2.7 Получение списка сотрудников несуществующей компании

 2.8 Получение списка сотрудников компании, в которой нет сотрудников

 2.9 Получение сотрудника по несуществующему id

 2.10 Добавление сотрудника без обязательного поля (id)

 2.11 Добавление сотрудника без обязательного поля (firstName)

 2.12 Добавление сотрудника без обязательного поля (lastName)

 2.13 Добавление сотрудника без обязательного поля (companyId)

 2.14 Добавление сотрудника без необязательного поля (middleName)

 2.15 Добавление сотрудника без необязательного поля (email)

 2.16 Добавление сотрудника без необязательного поля (url)

 2.17 Добавление сотрудника без необязательного поля (phone)

 2.18 Добавление сотрудника без необязательного поля (birthdate)

### Тесты для разработки:
Контрактные тесты:
1. Позитивный параметризованный тест на создание Employee с валидными
данными для каждого поля запроса
2. Позитивный параметризованный тест на изменение Employee с валидными
данными для каждого поля запроса
3. Негативный параметризованный тест на создание Employee с невалидными
данными для каждого поля запроса
4. Негативный параметризованный тест на изменение Employee с невалидными
данными для каждого поля запроса
5. Негативный параметризованный тест на параметр companyId запроса
Employee по номеру компании
6. Негативный параметризованный тест на путь id запроса Employee по id
7. Негативный параметризованный тест на хэдэр "x-client-token" запроса
на создание Employee
8. Негативный параметризованный тест на путь id запроса на изменение
Employee по id
9. Негативный параметризованный тест на хэдэр "x-client-token" запроса
на изменение Employee по id

### Обнаруженные баги:

1. Написать BUG-репорт, ЧТО при создании с неправильным телефоном возвращается ошибка 500 вместо 400
2. Написать BUG-репорт, что при запросе Employee через API поле "url" меняется на "avatar_url"
3. Написать BUG-репорт на несоответствие формата поля "birthdate" в запросах GET по id сотрудника, GET по id компании ("birthdate": "2023-08-12") и требованиях в Swagger ("birthdate": "2023-08-12T10:55:01.426Z")
4. Написать BUG-репорт. SC должен быть 404, если компании нет (в негативном тесте)
5. Написать BUG-репорт, что при создании Employee через API удаляется email
6. Написать BUG-репорт, что при создании Employee через API изменяется заданный id и возвращается автоматически присвоенный
7. Написать BUG-репорт, что при обновлении employee не обновляется поле phone.
8. Написать BUG-репорт, что можно создать дубликата Employee
9. Написать BUG-репорт, что можно добавить нового Employee на уже занятый id (фактически новому Employee выдаётся автоматически новый id) связан с BUG-репортом № 6 об игнорировании id при создании нового Employee
10. Написать BUG-репорт, что не должны создаваться Employee с id = 0
11. Написать BUG-репорт, что при ошибке в запросе на создание Employee выдаётся SC 500 вместо SC4XX
12. Написать BUG-репорт, что не создаётся Employee без номера телефона (SC 500), в Swagger поле Phone не отмечено как обязательное
13. Написать BUG-репорт, что при ошибке в id в URI запроса на изменение Employee выдаётся SC 500 вместо SC4XX
14. Написать BUG-репорт, что при запросе несуществующего сотрудника возвращается SC 200 вместо SC404
15. Написать BUG-репорт, что при запросе несуществующего сотрудника не возвращается тело ответа с "message":"Not found"
16. Написать BUG-репорт, что создаются Employee без поля "id" (в Swagger отмечено как обязательное)
17. Написать BUG-репорт, что создаются Employee без поля "isActive" (в Swagger отмечено как обязательное)
18. Написать BUG-репорт, что не создаются Employee без поля "phone" (в Swagger отмечен как необязательный)
