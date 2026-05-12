package ru.netology.diplom.test;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Step;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import ru.netology.diplom.data.ApiHelper;
import ru.netology.diplom.data.DataHelper;
import ru.netology.diplom.data.SqlHelper;
import ru.netology.diplom.pageobject.CardPaymentPage;
import ru.netology.diplom.pageobject.DashBoardPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.*;
import static ru.netology.diplom.data.SqlHelper.cleanDataBase;
import static ru.netology.diplom.data.SqlHelper.cleanPayment_entity;

// Тестирование покупки тура
public class TestPurchaseTourByCard {

    DashBoardPage dashBoardPage;
    CardPaymentPage cardPayment;

    @BeforeAll

    // Добавляем листенер allur перед выполнением всех тестов
    public static void addListenerAllure() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    // Методы помеченные аннотацией @AfterAll выполняются после прохождения всех тестов
    @AfterAll
    public static void removeAll() {
        // Очистка базы данных после выполнения всех тестов
        cleanDataBase();
        // Удаление листенера allur после выполнения всех тестов
        SelenideLogger.removeListener("allure");
    }

    // @BeforeEach - это аннотация в JUnit, означающая, что данный метод выполняется перед каждым тестом.
    @BeforeEach
    void setUp() {

        // Очистка таблицы БД перед выполнением каждого теста
        cleanPayment_entity();
        // Метод открывает страницу и сохраняет ссылку на объект типа DashBoardPage.
        dashBoardPage = open("http://localhost:8080", DashBoardPage.class);
        cardPayment = dashBoardPage.clickButtonPurchase();
    }

    // ПОЗИТИВНЫЕ СЦЕНАРИИ

    @Step("Тестирование покупки тура c валидными данными")

    @Test
    @DisplayName("Покупка тура с валидными данными со статусом APPROVED")
    void purchaseForValidCardWithApprovedStatus() {

        var approvedCard = DataHelper.getCardInfoWithApprovedStatus();
        cardPayment.payment(approvedCard);
        cardPayment.visibilityNotificationStatusOk("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(approvedCard);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Видимость уведомления с ошибкой при покупке тура со статусом DECLINED")
    void visibilityErrorNotificationForValidCardWithDeclinedStatus() {

        var declinedCard = DataHelper.getCardInfoWithDeclinedStatus();
        cardPayment.payment(declinedCard);

        assertAll(

                () -> cardPayment.visibilityTextErrorMessage("Ошибка! Банк отказал в проведении операции"),
                () -> cardPayment.visibilityNotificationStatusError("Ошибка! Банк отказал в проведении операции")
        );
    }

    @Test
    @DisplayName("API-тест покупка тура со статусом DECLINED")
    void apiTestPurchaseForValidCardWithDeclinedStatus() {

        var declinedCard = DataHelper.getCardInfoWithDeclinedStatus();
        cardPayment.payment(declinedCard);

        Response apiResponse = ApiHelper.sendRequest200(declinedCard);
        assertEquals("DECLINED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("DECLINED", paymentStatusInText);
    }

    // НЕГАТИВНЫЕ СЦЕНАРИИ

    @Step("Тестирование покупки тура с невалидным значением номера карты")

    @Test
    @DisplayName("Покупка тура, если в поле 'Номер карты' введен случайный номер")
    void purchaseIfEnteredRandomNumberCard() {

        var randomCard = DataHelper.getCardInfoWithRandomData();
        cardPayment.payment(randomCard);
        cardPayment.visibilityNotificationStatusError("Ошибка! Банк отказал в проведении операции");

        Response apiResponse = ApiHelper.sendRequest400(randomCard);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Номер карты' введено 15 цифр")
    void purchaseIf15DigitNumberEnteredInNumberCarField() {

        var shortNumderCard = DataHelper.getCardInfoWithShortNumberCard();
        cardPayment.payment(shortNumderCard);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");

        Response apiResponse = ApiHelper.sendRequest400(shortNumderCard);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);

        cardPayment.visibilityActualNumberCardInInputField();
        var actualNumberCard = cardPayment.getNumberCardValue();
        assertEquals(18, actualNumberCard.length());
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Номер карты' введено 17 цифр")
    void purchaseIf17DigitNumberEnteredInNumberCarField() {

        var longNumderCard = DataHelper.getCardInfoWithLongNumberCard();
        cardPayment.payment(longNumderCard);
        cardPayment.visibilityNotificationStatusOk("Операция одобрена Банком");

        cardPayment.visibilityActualNumberCardInInputField();
        var actualNumberCard = cardPayment.getNumberCardValue();

        assertEquals(19, actualNumberCard.length());

        Response apiResponse = ApiHelper.sendRequest400(longNumderCard);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если поле 'Номер карты' не заполнено")
    void visibilityErrorMessageIfNumberCardFieldEmpty() {

        var emptyNumberCard = DataHelper.getCardInfoWithEmptyNumberCard();
        cardPayment.payment(emptyNumberCard);
        cardPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура, если поле 'Номер карты' не заполнено")
    void apiPurchaseIfNumberCardFieldEmpty() {

        var emptyNumberCard = DataHelper.getCardInfoWithEmptyNumberCard();
        cardPayment.payment(emptyNumberCard);

        cardPayment.visibilityActualNumberCardInInputField();
        var actualNumberCard = cardPayment.getNumberCardValue();

        assertEquals(0, actualNumberCard.length());

        Response apiResponse = ApiHelper.sendRequest400(emptyNumberCard);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость уведомления об ошибке, если в поле 'Номер карты' введены нули")
    void visibilityErrorNotificationIfEnteredZeroesInNumberCardField() {

        var numberCardFromZeros = DataHelper.getCardInfoWithNumberCardFromZeroes();
        cardPayment.payment(numberCardFromZeros);
        cardPayment.visibilityNotificationStatusError("Ошибка! Банк отказал в проведении операции");
    }

    @Test
    @DisplayName("API-тест покупка тура, если в поле 'Номер карты' введены нули")
    void apiTestPurchaseIfEnteredZeroesInNumberCardField() {

        var numberCardFromZeros = DataHelper.getCardInfoWithNumberCardFromZeroes();
        cardPayment.payment(numberCardFromZeros);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(numberCardFromZeros);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }


    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Номер карты' введены буквы")
    void visibilityErrorMessageIfEnteredLettersInNumberCardField() {

        var numberCardFromLetters = DataHelper.getCardInfoWithNumberCardFromLetters();
        cardPayment.payment(numberCardFromLetters);
        cardPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура, если в поле 'Номер карты' введены буквы")
    void apiPurchaseIfEnteredLettersInNumberCardField() {

        var numberCardFromLetters = DataHelper.getCardInfoWithNumberCardFromLetters();
        cardPayment.payment(numberCardFromLetters);

        cardPayment.visibilityActualNumberCardInInputField();
        var actualNumberCard = cardPayment.getNumberCardValue();

        assertEquals(0, actualNumberCard.length());

        Response apiResponse = ApiHelper.sendRequest400(numberCardFromLetters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Номер карты' введены знаки")
    void visibilityErrorMessageIfEnteredCharactersInNumberCardField() {

        var numberCardFromCharacters = DataHelper.getCardInfoWithNumberCardFromCharacters();
        cardPayment.payment(numberCardFromCharacters);
        cardPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура, если в поле 'Номер карты' введены знаки")
    void apiPurchaseIfEnteredCharactersInNumberCardField() {

        var numberCardFromCharacters = DataHelper.getCardInfoWithNumberCardFromCharacters();
        cardPayment.payment(numberCardFromCharacters);

        cardPayment.visibilityActualNumberCardInInputField();
        var actualNumberCard = cardPayment.getNumberCardValue();

        assertEquals(0, actualNumberCard.length());

        Response apiResponse = ApiHelper.sendRequest400(numberCardFromCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Step("Тестирование покупки тура с невалидным значением поля 'Месяц'")

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Месяц' указан прошедший месяц")
    void visibilityErrorMessageIfPastNumberMonthEnteredInMonthField() {

        var pastNumberMonth = DataHelper.getCardInfoWithPastNumberMonth();
        cardPayment.payment(pastNumberMonth);
        cardPayment.visibilityErrorMessageForInputField("Неверно указан срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура, если в поле 'Месяц' указан прошедший месяц")
    void apiTestPurchaseIfPastNumberMonthEnteredInMonthField() {

        var pastNumberMonth = DataHelper.getCardInfoWithPastNumberMonth();
        cardPayment.payment(pastNumberMonth);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(pastNumberMonth);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Месяц' введена 1 цифра")
    void purchaseIf1DigitNumberEnteredInMonthField() {

        var shortNumberMonth = DataHelper.getCardInfoWithShortNumberMonth();
        cardPayment.payment(shortNumberMonth);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");

        cardPayment.visibilityActualNumberMonthInInputField();
        var actualNumberMonth = cardPayment.getNumberMonthValue();

        assertEquals(1, actualNumberMonth.length());

        Response apiResponse = ApiHelper.sendRequest400(shortNumberMonth);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Месяц' введены 3 цифры")
    void purchaseIf3DigitNumberEnteredInMonthField() {

        var longNumberMonth = DataHelper.getCardInfoWithLongNumberMonth();
        cardPayment.payment(longNumberMonth);
        cardPayment.visibilityErrorMessageForInputField("Неверно указан срок действия карты");

        cardPayment.visibilityActualNumberMonthInInputField();
        var actualNumberMonth = cardPayment.getNumberMonthValue();

        assertEquals(2, actualNumberMonth.length());

        Response apiResponse = ApiHelper.sendRequest400(longNumberMonth);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если поле 'Месяц' не заполнено")
    void visibilityErrorMessageIfMonthFieldEmpty() {

        var emptyNumberMonth = DataHelper.getCardInfoWithEmptyNumberMonth();
        cardPayment.payment(emptyNumberMonth);
        cardPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если поле 'Месяц' не заполнено")
    void apiPurchaseIfMonthFieldEmpty() {

        var emptyNumberMonth = DataHelper.getCardInfoWithEmptyNumberMonth();
        cardPayment.payment(emptyNumberMonth);

        cardPayment.visibilityActualNumberMonthInInputField();
        var actualNumber = cardPayment.getNumberMonthValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(emptyNumberMonth);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Месяц' введены нули 00")
    void visibilityErrorNotificationIfEnteredZeroesInMonthField() {

        var numberMonthFromZeros = DataHelper.getCardInfoWithMonthFromZeros();
        cardPayment.payment(numberMonthFromZeros);
        cardPayment.visibilityErrorMessageForInputField("Неверно указан срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Месяц' введены нули 00")
    void apiTestPurchaseIfEnteredZeroesInMonthField() {

        var numberMonthFromZeros = DataHelper.getCardInfoWithMonthFromZeros();
        cardPayment.payment(numberMonthFromZeros);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(numberMonthFromZeros);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Месяц' введены буквы")
    void visibilityErrorMessageIfEnteredLettersInMonthField() {

        var numberMonthFromLetters = DataHelper.getCardInfoWithMonthFromLetters();
        cardPayment.payment(numberMonthFromLetters);
        cardPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Месяц' введены буквы")
    void apiPurchaseIfEnteredLettersInMonthField() {

        var numberMonthFromLetters = DataHelper.getCardInfoWithMonthFromLetters();
        cardPayment.payment(numberMonthFromLetters);

        cardPayment.visibilityActualNumberMonthInInputField();
        var actualNumber = cardPayment.getNumberMonthValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(numberMonthFromLetters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Месяц' введены знаки")
    void visibilityErrorMessageIfEnteredCharactersInMonthField() {

        var numberMonthFromCharacters = DataHelper.getCardInfoWithMonthFromCharacters();
        cardPayment.payment(numberMonthFromCharacters);
        cardPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Месяц' введены знаки")
    void apiPurchaseIfEnteredCharactersInMonthField() {

        var numberMonthFromCharacters = DataHelper.getCardInfoWithMonthFromCharacters();
        cardPayment.payment(numberMonthFromCharacters);

        cardPayment.visibilityActualNumberMonthInInputField();
        var actualNumber = cardPayment.getNumberMonthValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(numberMonthFromCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Месяц' введено значение 13")
    void visibilityErrorMessageIfEntered13InMonthField() {

        var invalidNumberMonth = DataHelper.getCardInfoWithInvalidMonth();
        cardPayment.payment(invalidNumberMonth);
        cardPayment.visibilityErrorMessageForInputField("Неверно указан срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Месяц' введено несуществующее значение 13")
    void apiTestPurchaseIfEntered13InMonthField() {

        var invalidNumberMonth = DataHelper.getCardInfoWithInvalidMonth();
        cardPayment.payment(invalidNumberMonth);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(invalidNumberMonth);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Step("Тестирование покупки тура с невалидным значением поля 'Год'")

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Год' указан прошедший год")
    void visibilityErrorMessageIfPastNumberYearEnteredInYearField() {

        var pastNumberYear = DataHelper.getCardInfoWithPastNumberYear();
        cardPayment.payment(pastNumberYear);
        cardPayment.visibilityErrorMessageForInputField("Истёк срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Год' указан прошедший год")
    void apiTestPurchaseIfPastNumberYearEnteredInYearField() {

        var pastNumberYear = DataHelper.getCardInfoWithPastNumberYear();
        cardPayment.payment(pastNumberYear);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(pastNumberYear);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Год' введена 1 цифра")
    void purchaseIf1DigitNumberEnteredInYearField() {

        var shortNumberYear = DataHelper.getCardInfoWithShortNumberYear();
        cardPayment.payment(shortNumberYear);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");

        cardPayment.visibilityActualNumberMonthInInputField();
        var actualNumberMonth = cardPayment.getNumberYearValue();

        assertEquals(1, actualNumberMonth.length());

        Response apiResponse = ApiHelper.sendRequest400(shortNumberYear);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Год' введены 4 цифры")
    void purchaseIf3DigitNumberEnteredInYearField() {

        var longNumberYear = DataHelper.getCardInfoWithLongNumberYear();
        cardPayment.payment(longNumberYear);
        cardPayment.visibilityErrorMessageForInputField("Истёк срок действия карты");

        cardPayment.visibilityActualNumberMonthInInputField();
        var actualNumberMonth = cardPayment.getNumberYearValue();

        assertEquals(2, actualNumberMonth.length());

        Response apiResponse = ApiHelper.sendRequest400(longNumberYear);
        var apistatus = apiResponse.getStatusCode();
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если поле 'Год' не заполнено")
    void visibilityErrorMessageIfYearFieldEmpty() {

        var emptyNumberYear = DataHelper.getCardInfoWithEmptyNumberYear();
        cardPayment.payment(emptyNumberYear);
        cardPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если поле 'Год' не заполнено")
    void apiPurchaseIfYearFieldEmpty() {

        var emptyNumberYear = DataHelper.getCardInfoWithEmptyNumberYear();
        cardPayment.payment(emptyNumberYear);

        cardPayment.visibilityActualNumberYearInInputField();
        var actualNumber = cardPayment.getNumberYearValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(emptyNumberYear);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Год' введены нули 00")
    void visibilityErrorMessageIfEnteredZeroesInYearField() {

        var numberYearFromZeros = DataHelper.getCardInfoWithYearFromZeros();
        cardPayment.payment(numberYearFromZeros);
        cardPayment.visibilityErrorMessageForInputField("Истёк срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Год' введены нули 00")
    void apiTestPurchaseIfEnteredZeroesInYearField() {

        var numberYearFromZeros = DataHelper.getCardInfoWithYearFromZeros();
        cardPayment.payment(numberYearFromZeros);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(numberYearFromZeros);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Год' введены буквы")
    void visibilityErrorMessageIfEnteredLettersInYearField() {

        var numberYearFromLetters = DataHelper.getCardInfoWithYearFromLetters();
        cardPayment.payment(numberYearFromLetters);
        cardPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Год' введены буквы")
    void apiPurchaseIfEnteredLettersInYearField() {

        var numberYearFromLetters = DataHelper.getCardInfoWithYearFromLetters();
        cardPayment.payment(numberYearFromLetters);

        cardPayment.visibilityActualNumberYearInInputField();
        var actualNumber = cardPayment.getNumberYearValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(numberYearFromLetters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Год' введены знаки")
    void visibilityErrorMessageIfEnteredCharactersInYearField() {

        var numberYearFromCharacters = DataHelper.getCardInfoWithYearFromCharacters();
        cardPayment.payment(numberYearFromCharacters);
        cardPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Год' введены знаки")
    void apiPurchaseIfEnteredCharactersInYearField() {

        var numberYearFromCharacters = DataHelper.getCardInfoWithYearFromCharacters();
        cardPayment.payment(numberYearFromCharacters);

        cardPayment.visibilityActualNumberYearInInputField();
        var actualNumber = cardPayment.getNumberYearValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(numberYearFromCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Год' введено значение превышающее срок действия карты на 5 лет")
    void purchaseIfEnteredFutureYearAfter5Years() {

        var futureNumberYear = DataHelper.getCardInfoWithFutureYearAfter5Years();
        cardPayment.payment(futureNumberYear);
        cardPayment.visibilityNotificationStatusOk("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(futureNumberYear);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Год' введено значение превышающее срок действия карты на 6 лет")
    void purchaseIfEnteredFutureYearAfter6Years() {

        var invalidNumberYear = DataHelper.getCardInfoWithFutureYearAfter6Years();
        cardPayment.payment(invalidNumberYear);
        cardPayment.visibilityErrorMessageForInputField("Неверно указан срок действия карты");

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(invalidNumberYear);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Step("Тестирование покупки тура с невалидным значением поля 'CVC'")

    @Test
    @DisplayName("Покупка тура, если в поле 'CVC' введены 2 цифры")
    void purchaseIf2DigitNumberEnteredInCVCField() {

        var shortNumberCVC = DataHelper.getCardInfoWithShortNumberCVC();
        cardPayment.payment(shortNumberCVC);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");

        cardPayment.visibilityActualNumberCvcInInputField();
        var actualNumberCVC = cardPayment.getNumberCVCValue();

        assertEquals(2, actualNumberCVC.length());

        Response apiResponse = ApiHelper.sendRequest400(shortNumberCVC);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'CVC' введено 4 цифры")
    void purchaseIf4DigitNumberEnteredInCVCField() {

        var longNumberCVC = DataHelper.getCardInfoWithLongNumberCVC();
        cardPayment.payment(longNumberCVC);
        cardPayment.visibilityNotificationStatusOk("Операция одобрена Банком");

        cardPayment.visibilityActualNumberCvcInInputField();
        var actualNumberCVC = cardPayment.getNumberCVCValue();

        assertEquals(3, actualNumberCVC.length());

        Response apiResponse = ApiHelper.sendRequest400(longNumberCVC);
        var apistatus = apiResponse.getStatusCode();
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если поле 'CVC' не заполнено")
    void visibilityErrorMessageIfCVCFieldEmpty() {

        var emptyNumberCVC = DataHelper.getCardInfoWithEmptyNumberCVC();
        cardPayment.payment(emptyNumberCVC);

        assertAll(
                () -> cardPayment.visibilityErrorMessageForInputFieldCVC("Поле обязательно для заполнения"),
                () -> cardPayment.notVisibilityErrorMessageForInputFieldHolder("Поле обязательно для заполнения")
        );
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если поле 'CVC' не заполнено")
    void apiTestPurchaseIfCVCFieldEmpty() {

        var emptyNumberCVC = DataHelper.getCardInfoWithEmptyNumberCVC();
        cardPayment.payment(emptyNumberCVC);

        cardPayment.visibilityActualNumberCvcInInputField();
        var actualNumberCVC = cardPayment.getNumberCVCValue();

        assertEquals(0, actualNumberCVC.length());

        Response apiResponse = ApiHelper.sendRequest400(emptyNumberCVC);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'CVC' введены нули 000")
    void purchaseIfEnteredZeroesInCVCField() {

        var numberCVCFromZeros = DataHelper.getCardInfoWithCVCFromZeros();
        cardPayment.payment(numberCVCFromZeros);
        cardPayment.visibilityNotificationStatusOk("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(numberCVCFromZeros);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'CVC' введены буквы")
    void visibilityErrorMessageIfEnteredLettersInCVCField() {

        var numberCVCFromLetters = DataHelper.getCardInfoWithCVCFromLetters();
        cardPayment.payment(numberCVCFromLetters);

        assertAll(
                () -> cardPayment.visibilityErrorMessageForInputFieldCVC("Поле обязательно для заполнения"),
                () -> cardPayment.notVisibilityErrorMessageForInputFieldHolder("Поле обязательно для заполнения")
        );
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'CVC' введены буквы")
    void apiPurchaseIfEnteredLettersInCVCField() {

        var numberCVCFromLetters = DataHelper.getCardInfoWithCVCFromLetters();
        cardPayment.payment(numberCVCFromLetters);

        cardPayment.visibilityActualNumberCvcInInputField();
        var actualNumberCVC = cardPayment.getNumberCVCValue();

        assertEquals(0, actualNumberCVC.length());

        Response apiResponse = ApiHelper.sendRequest400(numberCVCFromLetters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'CVC' введены знаки")
    void visibilityErrorMessageIfEnteredCharactersInCVCField() {

        var numberCVCFromCharacters = DataHelper.getCardInfoWithCVCFromCharacters();
        cardPayment.payment(numberCVCFromCharacters);

        assertAll(
                () -> cardPayment.visibilityErrorMessageForInputFieldCVC("Поле обязательно для заполнения"),
                () -> cardPayment.notVisibilityErrorMessageForInputFieldHolder("Поле обязательно для заполнения")
        );
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'CVC' введены знаки")
    void apiPurchaseIfEnteredCharactersInCVCField() {

        var numberCVCFromCharacters = DataHelper.getCardInfoWithCVCFromCharacters();
        cardPayment.payment(numberCVCFromCharacters);

        cardPayment.visibilityActualNumberCvcInInputField();
        var actualNumberCVC = cardPayment.getNumberCVCValue();

        assertEquals(0, actualNumberCVC.length());

        Response apiResponse = ApiHelper.sendRequest400(numberCVCFromCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Step("Тестирование покупки тура с невалидным значением поля 'Владелец'")

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введена 1 буква")
    void visibilityErrorMessageIfShortHolderNameEnteredInHolderField() {

        var shortHolderName = DataHelper.getCardInfoWithShortHolderName();
        cardPayment.payment(shortHolderName);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Владелец' введена 1 буква")
    void apiPurchaseIfShortHolderNameEnteredInHolderField() {

        var shortHolderName = DataHelper.getCardInfoWithShortHolderName();
        cardPayment.payment(shortHolderName);

        var actualHolder = cardPayment.getHolderValue();
        cardPayment.visibilityActualHolderInInputField();

        assertEquals(1, actualHolder.length());

        Response apiResponse = ApiHelper.sendRequest400(shortHolderName);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Владелец' введены 2 буквы")
    void purchaseIfHolderNameFrom2DigitSymbolEnteredInHolderField() {

        var holderNameFrom2DigitSymbol = DataHelper.getCardInfoWithLongHolderNameFrom2DigitSymbol();
        cardPayment.payment(holderNameFrom2DigitSymbol);
        cardPayment.visibilityNotificationStatusOk("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(holderNameFrom2DigitSymbol);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Владелец' введена 21 буква")
    void purchaseIfHolderNameFrom21DigitSymbolEnteredInHolderField() {

        var holderNameFrom21DigitSymbol = DataHelper.getCardInfoWithHolderNameFrom21DigitSymbol();
        cardPayment.payment(holderNameFrom21DigitSymbol);
        cardPayment.visibilityNotificationStatusOk("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(holderNameFrom21DigitSymbol);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Владелец' введены 22 буквы")
    void purchaseIfHolderNameFrom22DigitSymbolEnteredInHolderField() {

        var longHolderName = DataHelper.getCardInfoWithLongHolderNameFromMore21DigitSymbol();
        cardPayment.payment(longHolderName);
        cardPayment.visibilityNotificationStatusOk("Операция одобрена Банком");

        cardPayment.visibilityActualHolderInInputField();
        var actualHolder = cardPayment.getHolderValue();

        assertEquals(21, actualHolder.length());
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Владелец' введены 22 буквы")
    void apiPurchaseIfHolderNameFrom22DigitSymbolEnteredInHolderField() {

        var longHolderName = DataHelper.getCardInfoWithLongHolderNameFromMore21DigitSymbol();
        cardPayment.payment(longHolderName);

        Response apiResponse = ApiHelper.sendRequest400(longHolderName);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены цифры")
    void visibilityErrorMessageIfHolderNameFromNumbers() {

        var holderNameFromNumbers = DataHelper.getCardInfoForHolderNameFromNumbers();
        cardPayment.payment(holderNameFromNumbers);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены цифры")
    void holderFieldShouldBeEmptyIfNumberEntered() {

        var holderNameFromNumbers = DataHelper.getCardInfoForHolderNameFromNumbers();
        cardPayment.payment(holderNameFromNumbers);

        var actualHolder = cardPayment.getHolderValue();
        cardPayment.visibilityActualHolderInInputField();

        assertEquals(0, actualHolder.length());
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Владелец' введены цифры")
    void apiPurchaseIfHolderNameFromNumbers() {

        var holderNameFromNumbers = DataHelper.getCardInfoForHolderNameFromNumbers();
        cardPayment.payment(holderNameFromNumbers);

        Response apiResponse = ApiHelper.sendRequest400(holderNameFromNumbers);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура, если в поле 'Владелец' пустое")
    void purchaseIfHolderNameEmpty() {

        var holderNameEmpty = DataHelper.getCardInfoForHolderNameWithEmptyField();
        cardPayment.payment(holderNameEmpty);
        cardPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");

        var actualHolder = cardPayment.getHolderValue();
        cardPayment.visibilityActualHolderInInputField();

        assertEquals(0, actualHolder.length());

        Response apiResponse = ApiHelper.sendRequest400(holderNameEmpty);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова на русском языке")
    void visibilityErrorMessageIfHolderNameEnteredOnRussianLanguage() {

        var holderNameOnRussianLanguage = DataHelper.getCardInfoForHolderNameIfEnteredLettersOnRussianLanguage();
        cardPayment.payment(holderNameOnRussianLanguage);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова на русском языке")
    void holderFieldShouldBeEmptyIfEnteredOnRussianLanguage() {

        var holderNameOnRussianLanguage = DataHelper.getCardInfoForHolderNameIfEnteredLettersOnRussianLanguage();
        cardPayment.payment(holderNameOnRussianLanguage);

        var actualHolder = cardPayment.getHolderValue();
        cardPayment.visibilityActualHolderInInputField();

        assertEquals(0, actualHolder.length());
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Владелец' введены слова на русском языке")
    void apiPurchaseIfHolderNameEnteredOnRussianLanguage() {

        var holderNameOnRussianLanguage = DataHelper.getCardInfoForHolderNameIfEnteredLettersOnRussianLanguage();
        cardPayment.payment(holderNameOnRussianLanguage);

        Response apiResponse = ApiHelper.sendRequest400(holderNameOnRussianLanguage);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова на грузинском языке")
    void visibilityErrorMessageIfHolderNameEnteredOnGeorgiaLanguage() {

        var holderNameOnGeorgiaLanguage = DataHelper.getCardInfoForHolderNameFromLettersOnGeorgiaLanguage();
        cardPayment.payment(holderNameOnGeorgiaLanguage);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова на грузинском языке")
    void holderFieldShouldBeEmptyIfEnteredOnGeorgiaLanguage() {

        var holderNameOnGeorgiaLanguage = DataHelper.getCardInfoForHolderNameFromLettersOnGeorgiaLanguage();
        cardPayment.payment(holderNameOnGeorgiaLanguage);

        var actualHolder = cardPayment.getHolderValue();
        cardPayment.visibilityActualHolderInInputField();

        assertEquals(0, actualHolder.length());
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Владелец' введены слова на грузинском языке")
    void apiPurchaseIfHolderNameEnteredOnGeorgiaLanguage() {

        var holderNameOnGeorgiaLanguage = DataHelper.getCardInfoForHolderNameFromLettersOnGeorgiaLanguage();
        cardPayment.payment(holderNameOnGeorgiaLanguage);

        Response apiResponse = ApiHelper.sendRequest400(holderNameOnGeorgiaLanguage);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова с цифрами")
    void visibilityErrorMessageIfHolderNameEnteredLettersWithNumbers() {

        var holderNameFromLettersWithNumbers = DataHelper.getCardInfoForHolderNameFromLettersWithNumbers();
        cardPayment.payment(holderNameFromLettersWithNumbers);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова с цифрами")
    void holderFieldShouldBeEmptyIfEnteredLettersWithNumbers() {

        var holderNameFromLettersWithNumbers = DataHelper.getCardInfoForHolderNameFromLettersWithNumbers();
        cardPayment.payment(holderNameFromLettersWithNumbers);

        var actualHolder = cardPayment.getHolderValue();
        cardPayment.visibilityActualHolderInInputField();

        assertEquals(6, actualHolder.length());
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Владелец' введены слова с цифрами")
    void apiPurchaseIfHolderNameEnteredLettersWithNumbers() {

        var holderNameFromLettersWithNumbers = DataHelper.getCardInfoForHolderNameFromLettersWithNumbers();
        cardPayment.payment(holderNameFromLettersWithNumbers);

        Response apiResponse = ApiHelper.sendRequest400(holderNameFromLettersWithNumbers);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены знаки")
    void visibilityErrorMessageIfHolderNameEnteredCharacters() {

        var holderNameFromCharacters = DataHelper.getCardInfoForHolderNameFromCharacters();
        cardPayment.payment(holderNameFromCharacters);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены знаки")
    void holderFieldShouldBeEmptyIfEnteredCharacters() {

        var holderNameFromCharacters = DataHelper.getCardInfoForHolderNameFromCharacters();
        cardPayment.payment(holderNameFromCharacters);

        var actualHolder = cardPayment.getHolderValue();
        cardPayment.visibilityActualHolderInInputField();

        assertEquals(0, actualHolder.length());
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Владелец' введены знаки")
    void apiPurchaseIfHolderNameEnteredCharacters() {

        var holderNameFromCharacters = DataHelper.getCardInfoForHolderNameFromCharacters();
        cardPayment.payment(holderNameFromCharacters);

        Response apiResponse = ApiHelper.sendRequest400(holderNameFromCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова с невалидными знаками")
    void visibilityErrorMessageIfHolderNameEnteredInvalidLettersWithCharacters() {

        var holderNameFromCInvalidLettersWithCharacters = DataHelper.getCardInfoForHolderNameFromInvalidLettersWithCharacters();
        cardPayment.payment(holderNameFromCInvalidLettersWithCharacters);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова с невалидными знаками")
    void holderFieldShouldBeEmptyIfEnteredInvalidLettersWithCharacters() {

        var holderNameFromCInvalidLettersWithCharacters = DataHelper.getCardInfoForHolderNameFromInvalidLettersWithCharacters();
        cardPayment.payment(holderNameFromCInvalidLettersWithCharacters);

        var actualHolder = cardPayment.getHolderValue();
        cardPayment.visibilityActualHolderInInputField();

        assertEquals(6, actualHolder.length());
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Владелец' введены слова с невалидными знаками")
    void apiPurchaseIfHolderNameEnteredInvalidLettersWithCharacters() {

        var holderNameFromCInvalidLettersWithCharacters = DataHelper.getCardInfoForHolderNameFromInvalidLettersWithCharacters();
        cardPayment.payment(holderNameFromCInvalidLettersWithCharacters);

        Response apiResponse = ApiHelper.sendRequest400(holderNameFromCInvalidLettersWithCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова с валидными знаками")
    void visibilityErrorMessageIfHolderNameEnteredValidLettersWithCharacters() {

        var holderNameFromValidLettersWithCharacters = DataHelper.getCardInfoFForHolderNameFromValidLettersWithCharacters();
        cardPayment.payment(holderNameFromValidLettersWithCharacters);
        cardPayment.visibilityNotificationStatusOk("Операция одобрена Банком");
    }

    @Test
    @DisplayName("Поле 'Владелец' не должно быть пустым, если введены слова с валидными знаками")
    void holderFieldShouldBeNotEmptyIfEnteredValidLettersWithCharacters() {

        var holderNameFromValidLettersWithCharacters = DataHelper.getCardInfoFForHolderNameFromValidLettersWithCharacters();
        cardPayment.payment(holderNameFromValidLettersWithCharacters);

        cardPayment.visibilityNotificationStatusOk("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(holderNameFromValidLettersWithCharacters);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова с пробелом")
    void visibilityErrorMessageIfHolderNameEnteredLettersWithEmptySpace() {

        var holderNameFromLettersWithEmptySpace = DataHelper.getCardInfoForHolderNameFromLettersWithEmptySpace();
        cardPayment.payment(holderNameFromLettersWithEmptySpace);
        cardPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова с пробелом")
    void holderFieldShouldBeEmptyIfEnteredLettersWithEmptySpace() {

        var holderNameFromLettersWithEmptySpace = DataHelper.getCardInfoForHolderNameFromLettersWithEmptySpace();
        cardPayment.payment(holderNameFromLettersWithEmptySpace);

        var actualHolder = cardPayment.getHolderValue();
        cardPayment.visibilityActualHolderInInputField();

        assertEquals(5, actualHolder.length());
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Владелец' введены слова с пробелом")
    void apiPurchaseIfHolderNameEnteredLettersWithEmptySpace() {

        var holderNameFromLettersWithEmptySpace = DataHelper.getCardInfoForHolderNameFromLettersWithEmptySpace();
        cardPayment.payment(holderNameFromLettersWithEmptySpace);

        Response apiResponse = ApiHelper.sendRequest400(holderNameFromLettersWithEmptySpace);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

}
