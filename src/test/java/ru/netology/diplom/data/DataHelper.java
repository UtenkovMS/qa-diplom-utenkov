package ru.netology.diplom.data;


import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Класс Datahelper - помощник данных, возвращает запрашиваемые данные.
public class DataHelper {

    // Конструктор DataHelper объявлен приватным и пустым.
    // Чтобы нельзя было создать методы класса извне, этот способ обеспечивает безопасность данных.
    // У конструктора такое же название, как и у класса.
    // Данный конструктор нужен просто пустым для обеспечения безопасности данных.
    private DataHelper() {
    }

    // Настройка библиотеки Faker, которая будет возвращать значения на "en" английском языке
    private static final Faker faker = new Faker(new Locale("en"));

    // Метод генерирующий рандомный номер карты
    private static final String numberCard = faker.numerify("#### #### #### ####");
    // Метод генерирующий текущий месяц
    private static final String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MM"));

    // Метод генерирующий прошедший месяц
    private static final String pastMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("MM"));

    // Метод генерирующий текущий год
    private static final String currentYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yy"));

    // Метод генерирующий будущий год через 5 лет
    private static final String futureYearAfter5Years = LocalDate.now().plusYears(5).format(DateTimeFormatter.ofPattern("yy"));

    // Метод генерирующий будущий год
    private static final String futureYearAfter6Years = LocalDate.now().plusYears(6).format(DateTimeFormatter.ofPattern("yy"));

    // Метод генерирующий прошлый год
    private static final String pastYear = LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern("yy"));

    // Метод генерирующий рандомный cvc
    private static final String cvc = faker.numerify("###");

    // Метод генерирующий случайное имя держателя карты .lastName() - фамилия
    private static final String holder = faker.name().lastName();

    public static CardInfo getCardInfoWithApprovedStatus() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithDeclinedStatus() {

        return new CardInfo("4444 4444 4444 4442", currentMonth, currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithRandomData() {

        return new CardInfo(numberCard, currentMonth, currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithShortNumberCard() {

        return new CardInfo("4444 4444 4444 444", currentMonth, currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithLongNumberCard() {

        return new CardInfo("4444 4444 4444 44413", currentMonth, currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithEmptyNumberCard() {

        return new CardInfo("", currentMonth, currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithNumberCardFromZeroes() {

        return new CardInfo("0000 0000 0000 0000", currentMonth, currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithNumberCardFromLetters() {

        return new CardInfo("jKHu PEay mnTY QiaW", currentMonth, currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithNumberCardFromCharacters() {

        return new CardInfo("##++ //** @!^& ={[<", currentMonth, currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithPastNumberMonth() {

        return new CardInfo("4444 4444 4444 4441", pastMonth, currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithShortNumberMonth() {

        return new CardInfo("4444 4444 4444 4441", "1", currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithLongNumberMonth() {

        return new CardInfo("4444 4444 4444 4441", "011", currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithEmptyNumberMonth() {

        return new CardInfo("4444 4444 4444 4441", "", currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithMonthFromZeros() {

        return new CardInfo("4444 4444 4444 4441", "00", currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithMonthFromLetters() {

        return new CardInfo("4444 4444 4444 4441", "ab", currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithMonthFromCharacters() {

        return new CardInfo("4444 4444 4444 4441", "%^", currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithInvalidMonth() {

        return new CardInfo("4444 4444 4444 4441", "13", currentYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithPastNumberYear() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, pastYear, holder, cvc);
    }

    public static CardInfo getCardInfoWithShortNumberYear() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, "6", holder, cvc);
    }

    public static CardInfo getCardInfoWithLongNumberYear() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, "2026", holder, cvc);
    }

    public static CardInfo getCardInfoWithEmptyNumberYear() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, "", holder, cvc);
    }

    public static CardInfo getCardInfoWithYearFromZeros() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, "00", holder, cvc);
    }

    public static CardInfo getCardInfoWithYearFromLetters() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, "ab", holder, cvc);
    }

    public static CardInfo getCardInfoWithYearFromCharacters() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, "@#", holder, cvc);
    }

    public static CardInfo getCardInfoWithFutureYearAfter5Years() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, futureYearAfter5Years, holder, cvc);
    }

    public static CardInfo getCardInfoWithFutureYearAfter6Years() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, futureYearAfter6Years, holder, cvc);
    }

    public static CardInfo getCardInfoWithShortNumberCVC() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, holder, "63");
    }

    public static CardInfo getCardInfoWithLongNumberCVC() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, holder, "1234");
    }

    public static CardInfo getCardInfoWithEmptyNumberCVC() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, holder, "");
    }

    public static CardInfo getCardInfoWithCVCFromZeros() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, holder, "000");
    }

    public static CardInfo getCardInfoWithCVCFromLetters() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, holder, "abc");
    }

    public static CardInfo getCardInfoWithCVCFromCharacters() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, holder, "@$#");
    }

    public static CardInfo getCardInfoWithShortHolderName() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "A", cvc);
    }

    public static CardInfo getCardInfoWithLongHolderNameFrom2DigitSymbol() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "Li", cvc);
    }

    public static CardInfo getCardInfoWithHolderNameFrom21DigitSymbol() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "Sergey Petrovich Zuev", cvc);
    }

    public static CardInfo getCardInfoWithLongHolderNameFromMore21DigitSymbol() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "Sergey Petrovich Lykov", cvc);
    }

    public static CardInfo getCardInfoForHolderNameFromNumbers() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "123", cvc);
    }

    public static CardInfo getCardInfoForHolderNameWithEmptyField() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "", cvc);
    }

    public static CardInfo getCardInfoForHolderNameIfEnteredLettersOnRussianLanguage() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "Владимир Петров", cvc);
    }

    public static CardInfo getCardInfoForHolderNameFromLettersOnGeorgiaLanguage() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "ნიკოლოზ", cvc);
    }

    public static CardInfo getCardInfoForHolderNameFromLettersWithNumbers() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "Sergey123", cvc);
    }

    public static CardInfo getCardInfoForHolderNameFromCharacters() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "*#%", cvc);
    }

    public static CardInfo getCardInfoForHolderNameFromInvalidLettersWithCharacters() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "#Sergey*", cvc);
    }

    public static CardInfo getCardInfoFForHolderNameFromValidLettersWithCharacters() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "Anna-Maria", cvc);
    }

    public static CardInfo getCardInfoForHolderNameFromLettersWithEmptySpace() {

        return new CardInfo("4444 4444 4444 4441", currentMonth, currentYear, "Maria ", cvc);
    }

    @Value
    public static class CardInfo {
        String number;
        String month;
        String year;
        String holder;
        String cvc;
    }

    //  Даёт много удобных операций, автоматически генерируя методы для работы с полями.
    @Data
    // Позволяет легко создавать объекты без конкретных данных.
    @NoArgsConstructor
    //  Помогает создать объекты сразу с заданием нужных значений.
    @AllArgsConstructor

    public static class CardStatus {
        String status;
    }
}
