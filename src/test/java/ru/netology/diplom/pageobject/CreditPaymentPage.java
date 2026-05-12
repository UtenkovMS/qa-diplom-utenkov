package ru.netology.diplom.pageobject;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import ru.netology.diplom.data.DataHelper;

import java.time.Duration;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class CreditPaymentPage {

    // // Проверка видимости страницы Кредит по данным карты
    public CreditPaymentPage() {
        $(byText("Кредит по данным карты")).shouldBe(Condition.visible);
    }

    // Селекторы информационных сообщений и полей ввода данных
    private final SelenideElement notificationStatusError = $("div.notification.notification_status_error .notification__content");
    private final SelenideElement notificationTextStatusError = $("div.notification");
    private final SelenideElement notificationStatusOk = $("div.notification.notification_status_ok .notification__content");
    private final SelenideElement errorMessageForNumberCardInputField = $("span.input [class='input__sub']");
    private final SelenideElement errorMessageForInputFieldCVC = $(byText("CVC/CVV")).parent().$("[class='input__sub']");
    private final SelenideElement errorMessageForInputFieldHolder = $(byText("Владелец")).parent().$("[class='input__sub']");
    private final SelenideElement cardNumber = $("span [placeholder='0000 0000 0000 0000']");
    private final SelenideElement month = $("span [placeholder='08']");
    private final SelenideElement year = $("span [placeholder='22']");
    private final SelenideElement cvc = $("span [placeholder='999']");
    // Так как для строки "Владелец" невозможно подобрать уточняющий селектор, используем методод byText()
    // Метод byText() - производит поиск точной фразы на странице, напр. "Владелец", "Год", "Месяц" и тд.
    // После того как на странице был найден элемент со словом "Владелец" нам необходимо перейти к его родительскому элементу
    // Для этого используем метод .parent() - поиск родительского элемента.
    // Родительский элемент будет включать группу элементов, в которой находится эл-т отвечающий за нажатие кнопки
    // После этого производим поиск по конечному селектору, который отвечает за нажатие кнопки
    private final SelenideElement holder = $(byText("Владелец")).parent().$("input.input__control");
    private final SelenideElement botton = $(byText("Продолжить")).shouldBe(visible, Duration.ofSeconds(10));

    // Проверка видимости сообщения: "Операция одобрена Банком".
    // В качестве аргумента передаем ожидаемый текст (String expectedText)
    public void visibilityNotificationStatusOk(String expectedText) {

        notificationStatusOk.shouldBe(Condition.visible, Duration.ofSeconds(10)).shouldHave(Condition.text(expectedText));
    }

    // Проверка видимости сообщения: "Ошибка! Банк отказал в проведении операции".
    // В качестве аргумента передается ожидаемый текст (String expectedText)
    public void visibilityErrorNotification(String expectedText) {

        notificationStatusError.shouldBe(Condition.visible, Duration.ofSeconds(10)).shouldHave(Condition.text(expectedText));
    }

    // Проверка видимости сообщения: "Ошибка! Банк отказал в проведении операции".
    // В качестве аргумента передается ожидаемый текст (String expectedText)
    public void visibilityTextErrorNotification(String expectedText) {

        notificationTextStatusError.shouldBe(Condition.visible, Duration.ofSeconds(15)).shouldHave(Condition.text(expectedText));
    }

    // Проверка видимости сообщения о том, что введенные данные в поле Номер карты не соответствуют формату
    public void visibilityErrorMessageForInputField(String expectedText) {

        errorMessageForNumberCardInputField.shouldHave(Condition.text(expectedText))
                .shouldBe(Condition.visible);
    }

    // Проверка видимости сообщений: "Неверный формат","Неверно указан срок действия карты", "Истёк срок действия карты", "Поле обязательно для заполнения"
    public void visibilityErrorMessageForInputFieldCVC(String expectedText) {

        errorMessageForInputFieldCVC.shouldHave(Condition.text(expectedText))
                .shouldBe(Condition.visible);
    }

    // Проверка видимости сообщений: "Неверный формат","Неверно указан срок действия карты", "Истёк срок действия карты", "Поле обязательно для заполнения"
    public void notVisibilityErrorMessageForInputFieldHolder(String expectedText) {

        // .shouldNot(exist) говорит о том что объект errorMessageForInputFieldHolder не должен быть видим на странице
        errorMessageForInputFieldHolder.shouldNot(exist);
    }

    // Получение номера карты, введенного в поле ввода
    public String getNumberCardValue() {

        return cardNumber.getValue();
    }

    public String getNumberMonthValue() {

        return month.getValue();
    }

    public String getNumberYearValue() {

        return year.getValue();
    }

    public String getNumberCVCValue() {

        return cvc.getValue();
    }

    public String getHolderValue() {

        return holder.getValue();
    }

    public void visibilityActualNumberCardInInputField() {

        String actualCardNumber = cardNumber.getValue();
        cardNumber.shouldBe(Condition.visible)
                .shouldHave(Condition.value(actualCardNumber));
    }

    public void visibilityActualNumberMonthInInputField() {

        String actualNumberMonth = month.getValue();
        month.shouldBe(Condition.visible)
                .shouldHave(Condition.value(actualNumberMonth));
    }

    public void visibilityActualNumberYearInInputField() {

        String actualNumberYear = year.getValue();
        year.shouldBe(Condition.visible)
                .shouldHave(Condition.value(actualNumberYear));
    }

    public void visibilityActualNumberCvcInInputField() {

        String actualNumberCvc = cvc.getValue();
        cvc.shouldBe(Condition.visible)
                .shouldHave(Condition.value(actualNumberCvc));
    }

    public void visibilityActualHolderInInputField() {

        String actualHolder = holder.getValue();
        holder.shouldBe(Condition.visible)
                .shouldHave(Condition.value(actualHolder));
    }

    // Заполнение полей ввода на странице Кредит по данным карты
    public CreditPaymentPage payment(DataHelper.CardInfo info) {

        cardNumber.setValue(info.getNumber());
        month.setValue(info.getMonth());
        year.setValue(info.getYear());
        cvc.setValue(info.getCvc());
        holder.setValue(info.getHolder());
        botton.click();

        return new CreditPaymentPage();

    }

}
