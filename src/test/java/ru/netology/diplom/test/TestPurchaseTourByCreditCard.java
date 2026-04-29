package ru.netology.diplom.test;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Step;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import ru.netology.diplom.data.ApiHelper;
import ru.netology.diplom.data.DataHelper;
import ru.netology.diplom.data.SqlHelper;
import ru.netology.diplom.pageobject.CreditPaymentPage;
import ru.netology.diplom.pageobject.DashBoardPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.*;
import static ru.netology.diplom.data.SqlHelper.*;

public class TestPurchaseTourByCreditCard {

    DashBoardPage dashBoardPage;
    CreditPaymentPage creditPayment;

    @BeforeAll

    // Добавляем листенер allur перед выполнением всех тестов
    public static void addListenerAllure() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    // Методы помеченные аннотацией @AfterAll выполняются после прохождения всех тестов
    // После прохождения тестов полезно удалить созданные записи в БД, чтобы подготовить её для следующего запуска.
    @AfterAll
    public static void cleanDB() {
        cleanDataBase();
    }

    @AfterAll
    // Удаление листенера allur после выполнения всех тестов
    public static void removeListenerAllure() {
        SelenideLogger.removeListener("allure");
    }

    // @BeforeEach - это аннотация в JUnit, означающая, что данный метод выполняется перед каждым тестом.
    @BeforeEach
    void setUp() {

        cleanCredit_request_entity();
        // Метод открывает страницу и сохраняет ссылку на объект типа DashBoardPage.
        dashBoardPage = open("http://localhost:8080", DashBoardPage.class);
        creditPayment = dashBoardPage.clickButtonPurchaseCredit();
    }

    @Step("Тестирование покупки тура в кредит c валидными данными")

    // ПОЗИТИВНЫЕ СЦЕНАРИИ

    @Test
    @DisplayName("Покупка тура в кредит со статусом APPROVED")
    void purchaseForCreditWithApprovedStatus() {

        var approvedCard = DataHelper.getCardInfoWithApprovedStatus();
        creditPayment.payment(approvedCard);
        creditPayment.visibilityMessageAboutApprovedOperation("Операция одобрена Банком");

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);

        Response apiResponse = ApiHelper.sendRequest200(approvedCard);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);
    }

    @Test
    @DisplayName("Видимость уведомления об ошибке при покупке тура в кредит со статусом DECLINED")
    void visibilityOfErrorNotificationForValidCardWithDeclinedStatus() {

        var declinedCard = DataHelper.getCardInfoWithDeclinedStatus();
        creditPayment.payment(declinedCard);

        assertAll(

                // Отображение текста об ошибке
                () -> creditPayment.visibilityTextErrorNotification("Ошибка! Банк отказал в проведении операции"),
                // Отображение уведомления об ошибке
                () -> creditPayment.visibilityErrorNotification("Ошибка! Банк отказал в проведении операции")
        );
    }

    @Test
    @DisplayName("Api-тест покупка тура в кредит со статусом DECLINED")
    void apiTestPurchaseForValidCreditCardWithDeclinedStatus() {

        var declinedCard = DataHelper.getCardInfoWithDeclinedStatus();
        creditPayment.payment(declinedCard);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("DECLINED", paymentStatusInText);

        Response apiResponse = ApiHelper.sendRequest200(declinedCard);
        assertEquals("DECLINED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);
    }

    // НЕГАТИВНЫЕ СЦЕНАРИИ

    @Step("Тестирование покупки тура в кредит с невалидным значением поля 'Номер карты'")

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'Номер карты' введен случайный номер")
    void purchaseForCreditNumberCard() {

        var randomCard = DataHelper.getCardInfoWithRandomData();
        creditPayment.payment(randomCard);
        creditPayment.visibilityErrorNotification("Ошибка! Банк отказал в проведении операции");

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(randomCard);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'Номер карты' введен номер из 15 символов")
    void purchaseForNumberCreditCardWith15symbols() {

        var shortNumderCard = DataHelper.getCardInfoWithShortNumberCard();
        creditPayment.payment(shortNumderCard);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");

        creditPayment.visibilityActualNumberCardInInputField();
        var actualNumber = creditPayment.getNumberCardValue();

        assertEquals(18, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(shortNumderCard);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'Номер карты' введен номер из 17 символов")
    void purchaseForNumberCreditCardWith17symbols() {

        var longNumderCard = DataHelper.getCardInfoWithLongNumberCard();
        creditPayment.payment(longNumderCard);
        creditPayment.visibilityMessageAboutApprovedOperation("Операция одобрена Банком");

        creditPayment.visibilityActualNumberCardInInputField();
        var actualNumber = creditPayment.getNumberCardValue();

        assertEquals(19, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(longNumderCard);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если поле 'Номер карты' не заполнено")
    void visibilityErrorMessageIfNumberCreditCardEmpty() {

        var emptyNumberCard = DataHelper.getCardInfoWithEmptyNumberCard();
        creditPayment.payment(emptyNumberCard);
        creditPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит c незаполненным полем 'Номер карты'")
    void apiTestPurchaseForNumberCreditCardWithEmptyField() {

        var emptyNumberCard = DataHelper.getCardInfoWithEmptyNumberCard();
        creditPayment.payment(emptyNumberCard);

        creditPayment.visibilityActualNumberCardInInputField();
        var actualNumber = creditPayment.getNumberCardValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(emptyNumberCard);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'Номер карты' введены нули")
    void purchaseForNumberCreditCardFromZeros() {

        var numberCardFromZeros = DataHelper.getCardInfoWithNumberCardFromZeroes();
        creditPayment.payment(numberCardFromZeros);
        creditPayment.visibilityErrorNotification("Ошибка! Банк отказал в проведении операции");

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(numberCardFromZeros);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'Номер карты' введены нули")
    void apiTestPurchaseIfEnteredZeroesInNumberCreditCardField() {

        var numberCardFromZeros = DataHelper.getCardInfoWithNumberCardFromZeroes();
        creditPayment.payment(numberCardFromZeros);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(numberCardFromZeros);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Номер карты' введены буквы")
    void visibilityErrorMessageIfNumberCreditCardFromLetters() {

        var numberCardFromLetters = DataHelper.getCardInfoWithNumberCardFromLetters();
        creditPayment.payment(numberCardFromLetters);
        creditPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит с номером из букв")
    void apiTestPurchaseForNumberCreditCardFromLetters() {

        var numberCardFromLetters = DataHelper.getCardInfoWithNumberCardFromLetters();
        creditPayment.payment(numberCardFromLetters);

        creditPayment.visibilityActualNumberCardInInputField();
        var actualNumber = creditPayment.getNumberCardValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(numberCardFromLetters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Номер карты' введены знаки")
    void visibilityErrorMessageIfNumberCreditCardFromCharacters() {

        var numberCardFromCharacters = DataHelper.getCardInfoWithNumberCardFromCharacters();
        creditPayment.payment(numberCardFromCharacters);
        creditPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит с номером из знаков")
    void apiTestPurchaseForNumberCreditCardFromCharacters() {

        var numberCardFromCharacters = DataHelper.getCardInfoWithNumberCardFromCharacters();
        creditPayment.payment(numberCardFromCharacters);

        creditPayment.visibilityActualNumberCardInInputField();
        var actualNumber = creditPayment.getNumberCardValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(numberCardFromCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Step("Тестирование покупки тура в кредит с невалидным значением месяца")

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Месяц' указан прошедший месяц")
    void visibilityErrorMessageIfPastNumberMonthEnteredInMonthField() {

        var pastNumberMonth = DataHelper.getCardInfoWithPastNumberMonth();
        creditPayment.payment(pastNumberMonth);
        creditPayment.visibilityErrorMessageForInputField("Неверно указан срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура по карте, если в поле 'Месяц' указан прошедший месяц")
    void apiTestPurchaseIfPastNumberMonthEnteredInMonthField() {

        var pastNumberMonth = DataHelper.getCardInfoWithPastNumberMonth();
        creditPayment.payment(pastNumberMonth);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(pastNumberMonth);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'Месяц' введена 1 цифра")
    void purchaseIf1DigitNumberEnteredInMonthField() {

        var shortNumberMonth = DataHelper.getCardInfoWithShortNumberMonth();
        creditPayment.payment(shortNumberMonth);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");

        creditPayment.visibilityActualNumberMonthInInputField();
        var actualNumberMonth = creditPayment.getNumberMonthValue();

        assertEquals(1, actualNumberMonth.length());

        Response apiResponse = ApiHelper.sendRequest400(shortNumberMonth);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'Месяц' введены 3 цифры")
    void purchaseIf3DigitNumberEnteredInMonthField() {

        var longNumberMonth = DataHelper.getCardInfoWithLongNumberMonth();
        creditPayment.payment(longNumberMonth);
        creditPayment.visibilityErrorMessageForInputField("Неверно указан срок действия карты");

        creditPayment.visibilityActualNumberMonthInInputField();
        var actualNumberMonth = creditPayment.getNumberMonthValue();

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
        creditPayment.payment(emptyNumberMonth);
        creditPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если поле 'Месяц' не заполнено")
    void apiTestPurchaseIfMonthFieldEmpty() {

        var emptyNumberMonth = DataHelper.getCardInfoWithEmptyNumberMonth();
        creditPayment.payment(emptyNumberMonth);

        creditPayment.visibilityActualNumberMonthInInputField();
        var actualNumber = creditPayment.getNumberMonthValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(emptyNumberMonth);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'Месяц' введены нули 00")
    void visibilityErrorNotificationIfEnteredZeroesInMonthField() {

        var numberMonthFromZeros = DataHelper.getCardInfoWithMonthFromZeros();
        creditPayment.payment(numberMonthFromZeros);
        creditPayment.visibilityErrorMessageForInputField("Неверно указан срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'Месяц' введены нули 00")
    void apiTestPurchaseIfEnteredZeroesInMonthField() {

        var numberMonthFromZeros = DataHelper.getCardInfoWithMonthFromZeros();
        creditPayment.payment(numberMonthFromZeros);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
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
        creditPayment.payment(numberMonthFromLetters);
        creditPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'Месяц' введены буквы")
    void apiTestPurchaseIfEnteredLettersInMonthField() {

        var numberMonthFromLetters = DataHelper.getCardInfoWithMonthFromLetters();
        creditPayment.payment(numberMonthFromLetters);

        creditPayment.visibilityActualNumberMonthInInputField();
        var actualNumber = creditPayment.getNumberMonthValue();

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
        creditPayment.payment(numberMonthFromCharacters);
        creditPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'Месяц' введены знаки")
    void apiTestPurchaseIfEnteredCharactersInMonthField() {

        var numberMonthFromCharacters = DataHelper.getCardInfoWithMonthFromCharacters();
        creditPayment.payment(numberMonthFromCharacters);

        creditPayment.visibilityActualNumberMonthInInputField();
        var actualNumber = creditPayment.getNumberMonthValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(numberMonthFromCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Месяц' введено значение 13")
    void visibilityErrorMessageIfEntered13InMonthField() {

        var numberNonExistentMonthFromZeros = DataHelper.getCardInfoWithInvalidMonth();
        creditPayment.payment(numberNonExistentMonthFromZeros);
        creditPayment.visibilityErrorMessageForInputField("Неверно указан срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'Месяц' введено несуществующее значение 13")
    void apiTestPurchaseIfEntered13InMonthField() {

        var invalidNumberMonth = DataHelper.getCardInfoWithInvalidMonth();
        creditPayment.payment(invalidNumberMonth);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(invalidNumberMonth);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Step("Тестирование покупки тура в кредит с невалидным значением поля 'Год'")

    @Test
    @DisplayName("Видимость сообщения об ошибке, если указан прошедший год")
    void visibilityErrorMessageIfPastNumberYearEnteredInYearField() {

        var pastNumberYear = DataHelper.getCardInfoWithPastNumberYear();
        creditPayment.payment(pastNumberYear);
        creditPayment.visibilityErrorMessageForInputField("Истёк срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'Год' указан прошедший год")
    void apiTestPurchaseIfPastNumberYearEnteredInYearField() {

        var pastNumberYear = DataHelper.getCardInfoWithPastNumberYear();
        creditPayment.payment(pastNumberYear);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(pastNumberYear);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'Год' введена 1 цифра")
    void purchaseIf1DigitNumberEnteredInYearField() {

        var shortNumberYear = DataHelper.getCardInfoWithShortNumberYear();
        creditPayment.payment(shortNumberYear);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");

        creditPayment.visibilityActualNumberMonthInInputField();
        var actualNumberMonth = creditPayment.getNumberYearValue();

        assertEquals(1, actualNumberMonth.length());

        Response apiResponse = ApiHelper.sendRequest400(shortNumberYear);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'Год' введено 4 цифры")
    void purchaseIf3DigitNumberEnteredInYearField() {

        var longNumberYear = DataHelper.getCardInfoWithLongNumberYear();
        creditPayment.payment(longNumberYear);
        creditPayment.visibilityErrorMessageForInputField("Истёк срок действия карты");

        creditPayment.visibilityActualNumberMonthInInputField();
        var actualNumberMonth = creditPayment.getNumberYearValue();

        assertEquals(2, actualNumberMonth.length());

        Response apiResponse = ApiHelper.sendRequest400(longNumberYear);
        var apistatus = apiResponse.getStatusCode();
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если поле 'Год' не заполнено")
    void visibilityErrorMessageIfYearFieldEmpty() {

        var emptyNumberYear = DataHelper.getCardInfoWithEmptyNumberYear();
        creditPayment.payment(emptyNumberYear);
        creditPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если поле 'Год' не заполнено")
    void apiTestPurchaseIfYearFieldEmpty() {

        var emptyNumberYear = DataHelper.getCardInfoWithEmptyNumberYear();
        creditPayment.payment(emptyNumberYear);

        creditPayment.visibilityActualNumberYearInInputField();
        var actualNumber = creditPayment.getNumberYearValue();

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
        creditPayment.payment(numberYearFromZeros);
        creditPayment.visibilityErrorMessageForInputField("Истёк срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'Год' введены нули 00")
    void apiTestPurchaseIfEnteredZeroesInYearField() {

        var numberYearFromZeros = DataHelper.getCardInfoWithYearFromZeros();
        creditPayment.payment(numberYearFromZeros);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
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
        creditPayment.payment(numberYearFromLetters);
        creditPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'Год' введены буквы")
    void apiTestPurchaseIfEnteredLettersInYearField() {

        var numberYearFromLetters = DataHelper.getCardInfoWithYearFromLetters();
        creditPayment.payment(numberYearFromLetters);

        creditPayment.visibilityActualNumberYearInInputField();
        var actualNumber = creditPayment.getNumberYearValue();

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
        creditPayment.payment(numberYearFromCharacters);
        creditPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'Год' введены знаки")
    void apiTestPurchaseIfEnteredCharactersInYearField() {

        var numberYearFromCharacters = DataHelper.getCardInfoWithYearFromCharacters();
        creditPayment.payment(numberYearFromCharacters);

        creditPayment.visibilityActualNumberYearInInputField();
        var actualNumber = creditPayment.getNumberYearValue();

        assertEquals(0, actualNumber.length());

        Response apiResponse = ApiHelper.sendRequest400(numberYearFromCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'Год' введено значение превышающее срок действия карты на 5 лет")
    void purchaseIfEnteredFutureYearAfter5Years() {

        var futureNumberYear = DataHelper.getCardInfoWithFutureYearAfter5Years();
        creditPayment.payment(futureNumberYear);
        creditPayment.visibilityMessageAboutApprovedOperation("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(futureNumberYear);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Год' введено значение превышающее срок действия карты на 6 лет")
    void visibilityErrorMessageIfEnteredFutureYearAfter6Years() {

        var invalidNumberYear = DataHelper.getCardInfoWithFutureYearAfter6Years();
        creditPayment.payment(invalidNumberYear);
        creditPayment.visibilityErrorMessageForInputField("Неверно указан срок действия карты");
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'Год' введено значение превышающее срок действия карты на 6 лет")
    void apiTestPurchaseIfEnteredFutureYearAfter6Years() {

        var invalidNumberYear = DataHelper.getCardInfoWithFutureYearAfter6Years();
        creditPayment.payment(invalidNumberYear);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        // Проверка на отсутствие данных БД используем assertNull()
        assertNull(paymentStatusFromDB);

        Response apiResponse = ApiHelper.sendRequest400(invalidNumberYear);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Step("Тестирование покупки тура в кредит с невалидным значением поля 'CVC'")

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'CVC' введены 2 цифра")
    void purchaseIf2DigitNumberEnteredInCVCField() {

        var shortNumberCVC = DataHelper.getCardInfoWithShortNumberCVC();
        creditPayment.payment(shortNumberCVC);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");

        creditPayment.visibilityActualNumberCvcInInputField();
        var actualNumberCVC = creditPayment.getNumberCVCValue();

        assertEquals(2, actualNumberCVC.length());

        Response apiResponse = ApiHelper.sendRequest400(shortNumberCVC);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'CVC' введено 4 цифры")
    void purchaseIf4DigitNumberEnteredInCVCField() {

        var longNumberCVC = DataHelper.getCardInfoWithLongNumberCVC();
        creditPayment.payment(longNumberCVC);
        creditPayment.visibilityMessageAboutApprovedOperation("Операция одобрена Банком");

        creditPayment.visibilityActualNumberCvcInInputField();
        var actualNumberCVC = creditPayment.getNumberCVCValue();

        assertEquals(3, actualNumberCVC.length());

        Response apiResponse = ApiHelper.sendRequest400(longNumberCVC);
        var apistatus = apiResponse.getStatusCode();
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если поле 'CVC' не заполнено")
    void visibilityErrorMessageIfCVCFieldEmpty() {

        var emptyNumberCVC = DataHelper.getCardInfoWithEmptyNumberCVC();
        creditPayment.payment(emptyNumberCVC);

        assertAll(
                () -> creditPayment.visibilityErrorMessageForInputFieldCVC("Поле обязательно для заполнения"),
                () -> creditPayment.notVisibilityErrorMessageForInputFieldHolder("Поле обязательно для заполнения")
        );
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если поле 'CVC' не заполнено")
    void apiTestPurchaseIfCVCFieldEmpty() {

        var emptyNumberCVC = DataHelper.getCardInfoWithEmptyNumberCVC();
        creditPayment.payment(emptyNumberCVC);

        creditPayment.visibilityActualNumberCvcInInputField();
        var actualNumberCVC = creditPayment.getNumberCVCValue();

        assertEquals(0, actualNumberCVC.length());

        Response apiResponse = ApiHelper.sendRequest400(emptyNumberCVC);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура в кредит, если в поле 'CVC' введены нули 000")
    void purchaseIfEnteredZeroesInCVCField() {

        var numberCVCFromZeros = DataHelper.getCardInfoWithCVCFromZeros();
        creditPayment.payment(numberCVCFromZeros);
        creditPayment.visibilityMessageAboutApprovedOperation("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(numberCVCFromZeros);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'CVC' введены буквы")
    void visibilityErrorMessageIfEnteredLettersInCVCField() {

        var numberCVCFromLetters = DataHelper.getCardInfoWithCVCFromLetters();
        creditPayment.payment(numberCVCFromLetters);

        assertAll(
                () -> creditPayment.visibilityErrorMessageForInputFieldCVC("Поле обязательно для заполнения"),
                () -> creditPayment.notVisibilityErrorMessageForInputFieldHolder("Поле обязательно для заполнения")
        );
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'CVC' введены буквы")
    void apiTestPurchaseIfEnteredLettersInCVCField() {

        var numberCVCFromLetters = DataHelper.getCardInfoWithCVCFromLetters();
        creditPayment.payment(numberCVCFromLetters);

        creditPayment.visibilityActualNumberCvcInInputField();
        var actualNumberCVC = creditPayment.getNumberCVCValue();

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
        creditPayment.payment(numberCVCFromCharacters);

        assertAll(
                () -> creditPayment.visibilityErrorMessageForInputFieldCVC("Поле обязательно для заполнения"),
                () -> creditPayment.notVisibilityErrorMessageForInputFieldHolder("Поле обязательно для заполнения")
        );
    }

    @Test
    @DisplayName("API-тест покупка тура в кредит, если в поле 'CVC' введены знаки")
    void apiTestPurchaseIfEnteredCharactersInCVCField() {

        var numberCVCFromCharacters = DataHelper.getCardInfoWithCVCFromCharacters();
        creditPayment.payment(numberCVCFromCharacters);

        creditPayment.visibilityActualNumberCvcInInputField();
        var actualNumberCVC = creditPayment.getNumberCVCValue();

        assertEquals(0, actualNumberCVC.length());

        Response apiResponse = ApiHelper.sendRequest400(numberCVCFromCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Step("Тестирование покупки тура в кредит с невалидным значением поля 'Владелец'")

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введена 1 буква")

    void visibilityErrorMessageIfShortHolderNameEnteredInHolderField() {

        var shortHolderName = DataHelper.getCardInfoWithShortHolderName();
        creditPayment.payment(shortHolderName);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Api-тест покупка тура по карте, если в поле 'Владелец' введена 1 буква")
    void apiTestPurchaseIfShortHolderNameEnteredInHolderField() {

        var shortHolderName = DataHelper.getCardInfoWithShortHolderName();
        creditPayment.payment(shortHolderName);

        var actualHolder = creditPayment.getHolderValue();
        creditPayment.visibilityActualHolderInInputField();

        assertEquals(1, actualHolder.length());

        Response apiResponse = ApiHelper.sendRequest400(shortHolderName);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура по карте, если в поле 'Владелец' введены 2 буквы")
    void purchaseIfHolderNameFrom2DigitSymbolEnteredInHolderField() {

        var holderNameFrom2DigitSymbol = DataHelper.getCardInfoWithLongHolderNameFrom2DigitSymbol();
        creditPayment.payment(holderNameFrom2DigitSymbol);
        creditPayment.visibilityMessageAboutApprovedOperation("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(holderNameFrom2DigitSymbol);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Покупка тура по карте, если в поле 'Владелец' введена 21 буква")
    void purchaseIfHolderNameFrom21DigitSymbolEnteredInHolderField() {

        var holderNameFrom21DigitSymbol = DataHelper.getCardInfoWithHolderNameFrom21DigitSymbol();
        creditPayment.payment(holderNameFrom21DigitSymbol);
        creditPayment.visibilityMessageAboutApprovedOperation("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(holderNameFrom21DigitSymbol);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Покупка тура по карте, если в поле 'Владелец' введены 22 буквы")
    void purchaseIfHolderNameFrom22DigitSymbolEnteredInHolderField() {

        var longHolderName = DataHelper.getCardInfoWithLongHolderNameFromMore21DigitSymbol();
        creditPayment.payment(longHolderName);
        creditPayment.visibilityMessageAboutApprovedOperation("Операция одобрена Банком");

        creditPayment.visibilityActualHolderInInputField();
        var actualHolder = creditPayment.getHolderValue();

        assertEquals(21, actualHolder.length());
    }

    @Test
    @DisplayName("Api-тест покупка тура по карте, если в поле 'Владелец' введены 22 буквы")
    void apiTestPurchaseIfHolderNameFrom22DigitSymbolEnteredInHolderField() {

        var longHolderName = DataHelper.getCardInfoWithLongHolderNameFromMore21DigitSymbol();
        creditPayment.payment(longHolderName);

        Response apiResponse = ApiHelper.sendRequest400(longHolderName);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость текста об ошибке, если в поле 'Владелец' введены цифры")
    void visibilityErrorMessageForPurchaseIfHolderNameFromNumbers() {

        var holderNameFromNumbers = DataHelper.getCardInfoForHolderNameFromNumbers();
        creditPayment.payment(holderNameFromNumbers);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены цифры")
    void holderFieldShouldBeEmptyIfNumberEntered() {

        var holderNameFromNumbers = DataHelper.getCardInfoForHolderNameFromNumbers();
        creditPayment.payment(holderNameFromNumbers);

        var actualHolder = creditPayment.getHolderValue();
        creditPayment.visibilityActualHolderInInputField();

        assertEquals(0, actualHolder.length());
    }

    @Test
    @DisplayName("Api-тест покупка тура по карте, если в поле 'Владелец' введены цифры")
    void apiTestPurchaseIfHolderNameFromNumbers() {

        var holderNameFromNumbers = DataHelper.getCardInfoForHolderNameFromNumbers();
        creditPayment.payment(holderNameFromNumbers);

        Response apiResponse = ApiHelper.sendRequest400(holderNameFromNumbers);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Покупка тура по карте, если в поле 'Владелец' пустое")
    void purchaseIfHolderNameEmpty() {

        var holderNameEmpty = DataHelper.getCardInfoForHolderNameWithEmptyField();
        creditPayment.payment(holderNameEmpty);
        creditPayment.visibilityErrorMessageForInputField("Поле обязательно для заполнения");

        var actualHolder = creditPayment.getHolderValue();
        creditPayment.visibilityActualHolderInInputField();

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
        creditPayment.payment(holderNameOnRussianLanguage);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова на русском языке")
    void holderFieldShouldBeEmptyIfEnteredOnRussianLanguage() {

        var holderNameOnRussianLanguage = DataHelper.getCardInfoForHolderNameIfEnteredLettersOnRussianLanguage();
        creditPayment.payment(holderNameOnRussianLanguage);

        var actualHolder = creditPayment.getHolderValue();
        creditPayment.visibilityActualHolderInInputField();

        assertEquals(0, actualHolder.length());
    }

    @Test
    @DisplayName("Api-тест покупка тура, если в поле 'Владелец' введены слова на русском языке")
    void apiTestPurchaseIfHolderNameEnteredOnRussianLanguage() {

        var holderNameOnRussianLanguage = DataHelper.getCardInfoForHolderNameIfEnteredLettersOnRussianLanguage();
        creditPayment.payment(holderNameOnRussianLanguage);

        Response apiResponse = ApiHelper.sendRequest400(holderNameOnRussianLanguage);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова на грузинском языке")
    void visibilityErrorMessageIfHolderNameEnteredOnGeorgiaLanguage() {

        var holderNameOnGeorgiaLanguage = DataHelper.getCardInfoForHolderNameFromLettersOnGeorgiaLanguage();
        creditPayment.payment(holderNameOnGeorgiaLanguage);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова на грузинском языке")
    void holderFieldShouldBeEmptyIfEnteredOnGeorgiaLanguage() {

        var holderNameOnGeorgiaLanguage = DataHelper.getCardInfoForHolderNameFromLettersOnGeorgiaLanguage();
        creditPayment.payment(holderNameOnGeorgiaLanguage);

        var actualHolder = creditPayment.getHolderValue();
        creditPayment.visibilityActualHolderInInputField();

        assertEquals(0, actualHolder.length());
    }

    @Test
    @DisplayName("Api-тест покупка тура, если в поле 'Владелец' введены слова на грузинском языке")
    void apiTestPurchaseIfHolderNameEnteredOnGeorgiaLanguage() {

        var holderNameOnGeorgiaLanguage = DataHelper.getCardInfoForHolderNameFromLettersOnGeorgiaLanguage();
        creditPayment.payment(holderNameOnGeorgiaLanguage);

        Response apiResponse = ApiHelper.sendRequest400(holderNameOnGeorgiaLanguage);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова с цифрами")
    void visibilityErrorMessageIfHolderNameEnteredLettersWithNumbers() {

        var holderNameFromLettersWithNumbers = DataHelper.getCardInfoForHolderNameFromLettersWithNumbers();
        creditPayment.payment(holderNameFromLettersWithNumbers);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова с цифрами")
    void holderFieldShouldBeEmptyIfEnteredLettersWithNumbers() {

        var holderNameFromLettersWithNumbers = DataHelper.getCardInfoForHolderNameFromLettersWithNumbers();
        creditPayment.payment(holderNameFromLettersWithNumbers);

        var actualHolder = creditPayment.getHolderValue();
        creditPayment.visibilityActualHolderInInputField();

        assertEquals(6, actualHolder.length());
    }

    @Test
    @DisplayName("Api-тест покупка тура, если в поле 'Владелец' введены слова с цифрами")
    void apiTestPurchaseIfHolderNameEnteredLettersWithNumbers() {

        var holderNameFromLettersWithNumbers = DataHelper.getCardInfoForHolderNameFromLettersWithNumbers();
        creditPayment.payment(holderNameFromLettersWithNumbers);

        Response apiResponse = ApiHelper.sendRequest400(holderNameFromLettersWithNumbers);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены знаки")
    void visibilityErrorMessageIfHolderNameEnteredCharacters() {

        var holderNameFromCharacters = DataHelper.getCardInfoForHolderNameFromCharacters();
        creditPayment.payment(holderNameFromCharacters);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены знаки")
    void holderFieldShouldBeEmptyIfEnteredCharacters() {

        var holderNameFromCharacters = DataHelper.getCardInfoForHolderNameFromCharacters();
        creditPayment.payment(holderNameFromCharacters);

        var actualHolder = creditPayment.getHolderValue();
        creditPayment.visibilityActualHolderInInputField();

        assertEquals(0, actualHolder.length());
    }

    @Test
    @DisplayName("Api-тест покупка тура, если в поле 'Владелец' введены знаки")
    void apiTestPurchaseIfHolderNameEnteredCharacters() {

        var holderNameFromCharacters = DataHelper.getCardInfoForHolderNameFromCharacters();
        creditPayment.payment(holderNameFromCharacters);

        Response apiResponse = ApiHelper.sendRequest400(holderNameFromCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова с невалидными знаками")
    void visibilityErrorMessageIfHolderNameEnteredInvalidLettersWithCharacters() {

        var holderNameFromCInvalidLettersWithCharacters = DataHelper.getCardInfoForHolderNameFromInvalidLettersWithCharacters();
        creditPayment.payment(holderNameFromCInvalidLettersWithCharacters);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова с невалидными знаками")
    void holderFieldShouldBeEmptyIfEnteredInvalidLettersWithCharacters() {

        var holderNameFromCInvalidLettersWithCharacters = DataHelper.getCardInfoForHolderNameFromInvalidLettersWithCharacters();
        creditPayment.payment(holderNameFromCInvalidLettersWithCharacters);

        var actualHolder = creditPayment.getHolderValue();
        creditPayment.visibilityActualHolderInInputField();

        assertEquals(6, actualHolder.length());
    }

    @Test
    @DisplayName("Api-тест покупка тура, если в поле 'Владелец' введены слова с невалидными знаками")
    void apiTestPurchaseIfHolderNameEnteredInvalidLettersWithCharacters() {

        var holderNameFromCInvalidLettersWithCharacters = DataHelper.getCardInfoForHolderNameFromInvalidLettersWithCharacters();
        creditPayment.payment(holderNameFromCInvalidLettersWithCharacters);

        Response apiResponse = ApiHelper.sendRequest400(holderNameFromCInvalidLettersWithCharacters);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова с валидными знаками")
    void visibilityErrorMessageIfHolderNameEnteredValidLettersWithCharacters() {

        var holderNameFromValidLettersWithCharacters = DataHelper.getCardInfoFForHolderNameFromValidLettersWithCharacters();
        creditPayment.payment(holderNameFromValidLettersWithCharacters);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова с валидными знаками")
    void holderFieldShouldBeEmptyIfEnteredValidLettersWithCharacters() {

        var holderNameFromValidLettersWithCharacters = DataHelper.getCardInfoFForHolderNameFromValidLettersWithCharacters();
        creditPayment.payment(holderNameFromValidLettersWithCharacters);

        creditPayment.visibilityMessageAboutApprovedOperation("Операция одобрена Банком");

        Response apiResponse = ApiHelper.sendRequest200(holderNameFromValidLettersWithCharacters);
        assertEquals("APPROVED", apiResponse.path("status"));
        var apistatus = apiResponse.getStatusCode();
        assertEquals(200, apistatus);

        var paymentStatusFromDB = SqlHelper.getCreditCardStatus();
        var paymentStatusInText = paymentStatusFromDB.getStatus();
        assertEquals("APPROVED", paymentStatusInText);
    }

    @Test
    @DisplayName("Видимость сообщения об ошибке, если в поле 'Владелец' введены слова с пробелом")
    void visibilityErrorMessageIfHolderNameEnteredLettersWithEmptySpace() {

        var holderNameFromLettersWithEmptySpace = DataHelper.getCardInfoForHolderNameFromLettersWithEmptySpace();
        creditPayment.payment(holderNameFromLettersWithEmptySpace);
        creditPayment.visibilityErrorMessageForInputField("Неверный формат");
    }

    @Test
    @DisplayName("Поле 'Владелец' должно быть пустым, если введены слова с пробелом")
    void holderFieldShouldBeEmptyIfEnteredLettersWithEmptySpace() {

        var holderNameFromLettersWithEmptySpace = DataHelper.getCardInfoForHolderNameFromLettersWithEmptySpace();
        creditPayment.payment(holderNameFromLettersWithEmptySpace);

        var actualHolder = creditPayment.getHolderValue();
        creditPayment.visibilityActualHolderInInputField();

        assertEquals(5, actualHolder.length());
    }

    @Test
    @DisplayName("Api-тест покупка тура, если в поле 'Владелец' введены слова с пробелом")
    void apiTestPurchaseIfHolderNameEnteredLettersWithEmptySpace() {

        var holderNameFromLettersWithEmptySpace = DataHelper.getCardInfoForHolderNameFromLettersWithEmptySpace();
        creditPayment.payment(holderNameFromLettersWithEmptySpace);

        Response apiResponse = ApiHelper.sendRequest400(holderNameFromLettersWithEmptySpace);
        var apistatus = apiResponse.getStatusCode();
        // Проверка кода статуса HTTP ответа, пишем значение статуса, как в методе sendRequest400()
        assertEquals(400, apistatus);
    }

}
