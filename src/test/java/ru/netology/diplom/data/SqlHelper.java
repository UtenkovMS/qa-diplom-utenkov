package ru.netology.diplom.data;

import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlHelper {

    // Класс QueryRunner является частью библиотеки Apache Commons DBUtils,
    // предназначенной для упрощения работы с базами данных в Java-приложениях
    // QueryRunner упрощает работу с SQL-запросами
    // query - переводится как запрос
    private static final QueryRunner QUERY_RUNNER = new QueryRunner();

    private SqlHelper() {
    }

    // Класс Connection - интерфейс библиотеки JDBC (Java Database Connectivity)
    // используется для соединения с базой данных.
    // Параметр throws SQLException необходим для стабильности приложения,
    // если возникнут ошибки при подключении к базе данных, программа не завершится,
    // а выдаст сообщение об ошибке.
    private static Connection getConn() throws SQLException {

        //`DriverManager.getConnection()` — это метод Java для получения соединения с базой данных.
        // Данный метод устанавливает соединение с указанной базой данных.
        // Параметр db.url - задается в bild.gradle
        return DriverManager.getConnection(System.getProperty("db.url"), "app", "pass");
    }

    // Аннотация @SneakyThrows используется для подавления сообщений об исключениях
    @SneakyThrows

    // Метод получения cтатуса платежа (Approved/Declined) из БД из таблицы payment_entity
    // Метод передает результат SQL запроса во вложенный класс DataHelper.CardStatus
    // Вложенным классом является CardStatus, который в свою очередь находится в классе DataHelper
    // Вложенный класс принимает данные в виде строки String status
    // Переменной status и присваивается значение (Approved/Declined) полученное из БД
    public static DataHelper.CardStatus getCardStatus() {

        // Запрос: выбери значение: SELECT, из столбца: status, из таблицы: payment_entity,
        // упорядочив значения: ORDER BY, по столбцу: created в порядке убывания: DESC,
        // взяв одно значение: LIMIT 1
        var statusSQL = "SELECT status FROM payment_entity ORDER BY created DESC LIMIT 1";
        // Конструкция try нужна чтобы установить соединение с ресурсом, напр. с БД, и после закрыть его.
        // В скобках указывается адрес ресурса. В данном случае это метод getConn() который подключается к
        // адресу db.url в котором зашит порт БД SQL.
        try (var conn = getConn()) {

            // Здесь пишется тело запроса (query)
            // Метод query() класса QUERY_RUNNER для выполнения sql запросов
            // В скобках указываются аргументы conn для подключения к БД, statusSQL - команда sql запроса,
            // new BeanHandler<>(DataHelper.VerificationСode.class) - это  утилита, которая обрабатывает ответ запроса
            // и преобразует его в объект DataHelper.CardStatus.class
            // В скобочках мы делаем ссылку на объект т.е. класс DataHelper объект CardStatus
            // метод .query применяется для выборки и возврата данных.
            return QUERY_RUNNER.query(conn, statusSQL, new BeanHandler<>(DataHelper.CardStatus.class));
        }
    }

    @SneakyThrows
    // Метод получения статуса платежа (Approved/Declined) из БД из таблицы credit_request_entity
    public static DataHelper.CardStatus getCreditCardStatus() {

        var statusSQL = "SELECT status FROM credit_request_entity ORDER BY created DESC LIMIT 1";

        try (var conn = getConn()) {

            return QUERY_RUNNER.query(conn, statusSQL, new BeanHandler<>(DataHelper.CardStatus.class));
        }
    }

    @SneakyThrows
    // Метод очистки таблиц БД
    public static void cleanDataBase() {
        try (var conn = getConn()) {

            // Метод execute() предназначен для выполнения SQL-запросов,
            // которые не предполагают возвращения данных (результатов), но выполняют изменения в базе данных.
            QUERY_RUNNER.execute(conn, "DELETE FROM credit_request_entity");
            QUERY_RUNNER.execute(conn, "DELETE FROM order_entity");
            QUERY_RUNNER.execute(conn, "DELETE FROM payment_entity");
        }
    }

    @SneakyThrows
    public static void cleanCredit_request_entity() {
        try (var conn = getConn()) {

            try {

            // Метод execute() предназначен для выполнения SQL-запросов,
            // которые не предполагают возвращения данных (результатов), но выполняют изменения в базе данных.
            QUERY_RUNNER.execute(conn, "DELETE FROM credit_request_entity");
            } catch (Exception e) {
                // Ловим ошибку
            }
        }
    }

    @SneakyThrows
    public static void cleanPayment_entity() {
        try (var conn = getConn()) {

            try {

            // Метод execute() предназначен для выполнения SQL-запросов,
            // которые не предполагают возвращения данных (результатов), но выполняют изменения в базе данных.
            QUERY_RUNNER.execute(conn, "DELETE FROM payment_entity");

            } catch (Exception e) {
                // Ловим ошибку
            }
        }
    }
}
