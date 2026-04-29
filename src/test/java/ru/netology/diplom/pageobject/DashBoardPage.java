package ru.netology.diplom.pageobject;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class DashBoardPage {

    private final SelenideElement dashBoardElement = $("[class='heading heading_size_l heading_theme_alfa-on-white']");

    // Проверку видимости страницы добавили в конструктор,
    // чтобы данная проверка происходила автоматически каждый раз при загрузке страницы.
    public DashBoardPage() {

        dashBoardElement.shouldBe(visible);
        dashBoardElement.shouldHave(text("Путешествие дня")).shouldBe(visible);
    }

    public CardPaymentPage clickButtonPurchase() {

        $("[class='button button_size_m button_theme_alfa-on-white'] span.button__text").click();

        return new CardPaymentPage();
    }

    public CreditPaymentPage clickButtonPurchaseCredit() {

        $("[class='button button_view_extra button_size_m button_theme_alfa-on-white'] span.button__text").click();

        return new CreditPaymentPage();
    }


}
