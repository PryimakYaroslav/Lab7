package Yarik;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Task1 {

    public static void main(String[] args) {
        BookReturnModel loan = new BookReturnModel();

        loan.setReaderTicketNumber("R12345");
        loan.setReaderName("Іван Петренко");
        loan.setReaderEmail("ivan@example.com");

        loan.setBookIsbn("978-617-000-000-1");
        loan.setBookTitle("Основи програмування");

        loan.setExpectedReturnDate(LocalDate.of(2026, 10, 20));
        loan.setActualReturnDate(LocalDate.of(2026, 10, 25));

        LibraryLoanService service = new LibraryLoanService();
        service.processBookReturn(loan);
    }
}


// =====================================================
// 1. ОРКЕСТРАТОР — КЕРУЄ ПРОЦЕСОМ ПОВЕРНЕННЯ
// =====================================================

class LibraryLoanService {

    private final LoanValidator validator = new LoanValidator();
    private final FineCalculator fineCalculator = new FineCalculator();
    private final ReturnNoticeFormatter noticeFormatter =
            new ReturnNoticeFormatter();
    private final CatalogRepository catalogRepository =
            new CatalogRepository();
    private final EmailNotificationService emailService =
            new EmailNotificationService();

    public void processBookReturn(BookReturnModel loan) {
        validator.validate(loan);

        FineResult result = fineCalculator.calculate(loan);

        String notice = noticeFormatter.format(loan, result);

        try {
            catalogRepository.saveReturn(loan);
        } catch (IOException e) {
            System.err.println(
                    "Помилка оновлення каталогу: " + e.getMessage());
        }

        emailService.send(loan.getReaderEmail(), notice, result);
    }
}


// =====================================================
// 2. ДОМЕННА МОДЕЛЬ — ДАНІ ПРО ПОВЕРНЕННЯ
// =====================================================

class BookReturnModel {

    private String readerTicketNumber;
    private String readerName;
    private String readerEmail;
    private String bookIsbn;
    private String bookTitle;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;

    public String getReaderTicketNumber() {
        return readerTicketNumber;
    }

    public void setReaderTicketNumber(String value) {
        this.readerTicketNumber = value;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String value) {
        this.readerName = value;
    }

    public String getReaderEmail() {
        return readerEmail;
    }

    public void setReaderEmail(String value) {
        this.readerEmail = value;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String value) {
        this.bookIsbn = value;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String value) {
        this.bookTitle = value;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate value) {
        this.expectedReturnDate = value;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate value) {
        this.actualReturnDate = value;
    }
}


// =====================================================
// 3. РЕЗУЛЬТАТ РОЗРАХУНКУ
// =====================================================

class FineResult {

    private final long overdueDays;
    private final BigDecimal fineAmount;
    private final int loyaltyPointsEarned;

    public FineResult(long overdueDays, BigDecimal fineAmount,
                      int loyaltyPointsEarned) {
        this.overdueDays = overdueDays;
        this.fineAmount = fineAmount;
        this.loyaltyPointsEarned = loyaltyPointsEarned;
    }

    public long getOverdueDays() {
        return overdueDays;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public int getLoyaltyPointsEarned() {
        return loyaltyPointsEarned;
    }
}


// =====================================================
// 4. ВАЛІДАЦІЯ ВХІДНИХ ДАНИХ
// =====================================================

class LoanValidator {

    public void validate(BookReturnModel loan) {
        if (loan == null) {
            throw new IllegalArgumentException(
                    "Дані про повернення відсутні.");
        }

        if (isBlank(loan.getReaderTicketNumber())) {
            throw new IllegalArgumentException(
                    "Номер читацького квитка відсутній.");
        }

        if (isBlank(loan.getBookIsbn())) {
            throw new IllegalArgumentException(
                    "Код ISBN книги обов'язковий.");
        }

        if (loan.getExpectedReturnDate() == null
                || loan.getActualReturnDate() == null) {
            throw new IllegalArgumentException(
                    "Дати повернення обов'язкові.");
        }

        if (loan.getActualReturnDate().isBefore(
                loan.getExpectedReturnDate().minusYears(1))) {
            throw new IllegalStateException(
                    "Неможлива дата повернення книги.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}


// =====================================================
// 5. РОЗРАХУНОК ШТРАФУ ТА БОНУСІВ
// =====================================================

class FineCalculator {

    private static final BigDecimal DAILY_FINE =
            new BigDecimal("15.00");

    private static final int ON_TIME_BONUS = 10;

    public FineResult calculate(BookReturnModel loan) {
        long overdueDays = ChronoUnit.DAYS.between(
                loan.getExpectedReturnDate(),
                loan.getActualReturnDate());

        BigDecimal fineAmount = BigDecimal.ZERO;
        int points = 0;

        if (overdueDays > 0) {
            fineAmount = DAILY_FINE.multiply(
                    BigDecimal.valueOf(overdueDays));
        } else {
            points = ON_TIME_BONUS;
        }

        return new FineResult(overdueDays, fineAmount, points);
    }
}


// =====================================================
// 6. ФОРМУВАННЯ АКТА ПОВЕРНЕННЯ
// =====================================================

class ReturnNoticeFormatter {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String format(BookReturnModel loan, FineResult result) {
        StringBuilder notice = new StringBuilder();

        notice.append("БІБЛІОТЕЧНИЙ АКТ ПОВЕРНЕННЯ КНИГИ\n");

        notice.append("Читач: ")
                .append(loan.getReaderName())
                .append(" (Квиток №")
                .append(loan.getReaderTicketNumber())
                .append(")\n");

        notice.append("Книга: \"")
                .append(loan.getBookTitle())
                .append("\" (ISBN: ")
                .append(loan.getBookIsbn())
                .append(")\n");

        notice.append("Планова дата: ")
                .append(loan.getExpectedReturnDate().format(DATE_FORMAT))
                .append(" | Фактична: ")
                .append(loan.getActualReturnDate().format(DATE_FORMAT))
                .append("\n");

        if (result.getOverdueDays() > 0) {
            notice.append(String.format(
                    "УВАГА! Прострочення: %d днів. "
                            + "Штраф до сплати: %.2f грн.%n",
                    result.getOverdueDays(),
                    result.getFineAmount()));
        } else {
            notice.append("Дякуємо за вчасне повернення! ")
                    .append("Нараховано бонусних балів: ")
                    .append(result.getLoyaltyPointsEarned())
                    .append("\n");
        }

        return notice.toString();
    }
}


// =====================================================
// 7. ЗБЕРЕЖЕННЯ ІСТОРІЇ У ФАЙЛ
// =====================================================

class CatalogRepository {

    private static final String CATALOG_FILE =
            "library_catalog_history.txt";

    public void saveReturn(BookReturnModel loan) throws IOException {
        String record = String.format(
                "%s|RETURNED|%s|By_%s%n",
                loan.getBookIsbn(),
                loan.getActualReturnDate().format(
                        DateTimeFormatter.ISO_LOCAL_DATE),
                loan.getReaderTicketNumber());

        try (FileWriter writer = new FileWriter(CATALOG_FILE, true)) {
            writer.write(record);
        }
    }
}


// =====================================================
// 8. НАДСИЛАННЯ ПОВІДОМЛЕНЬ
// =====================================================

class EmailNotificationService {

    public void send(String email, String notice, FineResult result) {
        String subject;

        if (result.getOverdueDays() > 0) {
            subject = "Повідомлення про штраф за прострочення книги";
        } else {
            subject = "Підтвердження повернення книги";
        }

        System.out.println("Надсилання пошти на " + email
                + " через smtp.citylibrary.local");
        System.out.println("Тема: " + subject);
        System.out.println("Текст: " + notice);
    }
}
