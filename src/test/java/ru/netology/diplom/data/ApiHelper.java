package ru.netology.diplom.data;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class ApiHelper {

    private ApiHelper() {
    }

    // Создаем объект requestSpec.
    // Создаем класс-конструктор new RequestSpecBuilder(), который сохраняет настройки HTTP-запросов.
    // RequestSpecification — выступает конечным хранилищем этих настроек, которое затем используется при отправке запросов.
    private static final RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost") // адрес сервера, к которому будем отправлять запросы.
            .setPort(8080) // номер порта сервера.
            .setAccept(ContentType.JSON) // тип принимаемых данных (JSON).
            .setContentType(ContentType.JSON) // тип отправляемых данных (JSON).
            .log(LogDetail.ALL) // настройка полного логирования каждого шага взаимодействия с сервером (полезно для дебага).
            .build(); // создаёт финальный объект спецификации, который позже будет использоваться при отправке запросов.

    // Метод sendRequest - отправка запроса
    public static Response sendRequest200(DataHelper.CardInfo info) {
        // Given - When - Then
        // Предусловия

        return given()
                .spec(requestSpec) // используем предустановленные настройки запроса
                // Выполняемые действия
                .body(info) // добавляем тело запроса (наш объект пользователя - user)
                .when().log().all() // запускает выполнение запроса .post и выводит логи .log().all()
                .post("/api/v1/pay")// отправляем POST-запрос на соответствующий путь
                // Проверки
                .then().log().all() // Запускает проверку возвращенного результата, в данном случае код статуса
                .statusCode(200) // проверяем, что сервер ответил успешным кодом 200
                .extract()
                .response();
    }

    // Метод sendRequest - отправка запроса
    public static Response sendRequest400(DataHelper.CardInfo info) {
        // Given - When - Then
        // Предусловия

        return given()
                .spec(requestSpec) // используем предустановленные настройки запроса
                // Выполняемые действия
                .body(info) // добавляем тело запроса (наш объект пользователя - user)
                .when().log().all() // запускает выполнение запроса .post и выводит логи .log().all()
                .post("/api/v1/pay")// отправляем POST-запрос на соответствующий путь
                // Проверки
                .then().log().all() // Запускает проверку возвращенного результата, в данном случае код статуса
                .statusCode(400) // проверяем, что сервер ответил кодом 500
                .extract()
                .response();
    }

    public static Response sendRequest500(DataHelper.CardInfo info) {
        // Given - When - Then
        // Предусловия

        return given()
                .spec(requestSpec) // используем предустановленные настройки запроса
                // Выполняемые действия
                .body(info) // добавляем тело запроса (наш объект пользователя - user)
                .when().log().all() // запускает выполнение запроса .post и выводит логи .log().all()
                .post("/api/v1/pay")// отправляем POST-запрос на соответствующий путь
                // Проверки
                .then().log().all() // Запускает проверку возвращенного результата, в данном случае код статуса
                .statusCode(500) // проверяем, что сервер ответил кодом 500
                .extract()
                .response();
    }

}
