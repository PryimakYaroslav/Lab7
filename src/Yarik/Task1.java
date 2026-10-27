package Yarik;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// Головний клас програми
public class Task1 {
    public static void main(String[] args) {
        BookReturnModel loan = new BookReturnModel(
                "R12345", "Іван Петренко", "ivan@example.com",
                "978-617-000-000-1", "Основи програмування",
                LocalDate.of(2026, 10, 20),
                LocalDate.of(2026, 10, 25));

        LibraryLoanService service = new LibraryLoanService();
        service.processBookReturn(loan);
    }
}

// Керує процесом повернення книги
class LibraryLoanService {
    private final LoanValidator validator = new LoanValidator();
    private final FineCalculator calculator = new FineCalculator();
    private final ReturnNoticeFormatter formatter = new ReturnNoticeFormatter();
    private final CatalogRepository catalog = new CatalogRepository();
    private final EmailNotificationService email = new EmailNotificationService();

    public void processBookReturn(BookReturnModel loan) {
        validator.validate(loan);
        FineResult result = calculator.calculate(loan);
        String notice = formatter.format(loan, result);

        try {
            catalog.saveReturn(loan);
        } catch (IOException e) {
            System.err.println("Помилка запису історії: " + e.getMessage());
        }

        email.send(loan.getReaderEmail(), notice, result);
    }
}

// Зберігає інформацію про читача та книгу
class BookReturnModel {
    private String ticket;
    private String reader;
    private String email;
    private String isbn;
    private String title;
    private LocalDate expected;
    private LocalDate actual;

    public BookReturnModel(String ticket, String reader, String email,
                           String isbn, String title,
                           LocalDate expected, LocalDate actual) {
        this.ticket = ticket;
        this.reader = reader;
        this.email = email;
        this.isbn = isbn;
        this.title = title;
        this.expected = expected;
        this.actual = actual;
    }

    public String getTicket() { return ticket; }
    public String getReader() { return reader; }
    public String getReaderEmail() { return email; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public LocalDate getExpected() { return expected; }
    public LocalDate getActual() { return actual; }

    public void setReader(String reader) { this.reader = reader; }
    public void setReaderEmail(String email) { this.email = email; }
    public void setTitle(String title) { this.title = title; }
}

// Містить результат розрахунку штрафу
class FineResult {
    private final long overdueDays;
    private final BigDecimal amount;
    private final int points;

    public FineResult(long days, BigDecimal amount, int points) {
        this.overdueDays = days;
        this.amount = amount;
        this.points = points;
    }

    public long getOverdueDays() { return overdueDays; }
    public BigDecimal getAmount() { return amount; }
    public int getPoints() { return points; }
}

// Перевіряє правильність вхідних даних
class LoanValidator {
    public void validate(BookReturnModel loan) {
        if (loan == null)
            throw new IllegalArgumentException("Дані відсутні.");

        if (isBlank(loan.getTicket()))
            throw new IllegalArgumentException("Не вказано номер квитка.");

        if (isBlank(loan.getReader()))
            throw new IllegalArgumentException("Не вказано ім'я читача.");

        if (isBlank(loan.getReaderEmail()))
            throw new IllegalArgumentException("Не вказано електронну пошту.");

        if (isBlank(loan.getIsbn()) || isBlank(loan.getTitle()))
            throw new IllegalArgumentException("Дані книги обов'язкові.");

        if (loan.getExpected() == null || loan.getActual() == null)
            throw new IllegalArgumentException("Дати повернення обов'язкові.");

        if (loan.getActual().isBefore(loan.getExpected().minusYears(1)))
            throw new IllegalArgumentException("Неможлива дата повернення.");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

// Розраховує штраф або бонусні бали
class FineCalculator {
    private static final BigDecimal DAILY_FINE = new BigDecimal("15.00");
    private static final int ON_TIME_BONUS = 10;

    public FineResult calculate(BookReturnModel loan) {
        long days = ChronoUnit.DAYS.between(
                loan.getExpected(), loan.getActual());

        BigDecimal fine = BigDecimal.ZERO;
        int points = 0;

        if (days > 0) {
            fine = DAILY_FINE.multiply(BigDecimal.valueOf(days));
        } else {
            points = ON_TIME_BONUS;
        }

        return new FineResult(days, fine, points);
    }
}

// Формує текст акта повернення
class ReturnNoticeFormatter {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String format(BookReturnModel loan, FineResult result) {
        StringBuilder text = new StringBuilder();

        text.append("БІБЛІОТЕЧНИЙ АКТ ПОВЕРНЕННЯ\n");
        text.append("Читач: ").append(loan.getReader())
                .append(" (квиток №").append(loan.getTicket()).append(")\n");
        text.append("Книга: ").append(loan.getTitle())
                .append(" (ISBN: ").append(loan.getIsbn()).append(")\n");
        text.append("Планова дата: ")
                .append(loan.getExpected().format(FORMAT));
        text.append("\nФактична дата: ")
                .append(loan.getActual().format(FORMAT)).append("\n");

        if (result.getOverdueDays() > 0) {
            text.append("Прострочення: ")
                    .append(result.getOverdueDays()).append(" днів\n");
            text.append("Штраф: ").append(result.getAmount()).append(" грн\n");
        } else {
            text.append("Книгу повернуто вчасно.\n");
            text.append("Бонусні бали: ").append(result.getPoints()).append("\n");
        }

        return text.toString();
    }
}

// Зберігає історію повернення у файл
class CatalogRepository {
    private static final String FILE_NAME = "library_catalog_history.txt";

    public void saveReturn(BookReturnModel loan) throws IOException {
        String record = String.format("%s|RETURNED|%s|%s%n",
                loan.getIsbn(), loan.getActual(), loan.getTicket());

        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            writer.write(record);
        }
    }
}

// Імітує надсилання електронного листа
class EmailNotificationService {
    public void send(String address, String notice, FineResult result) {
        String subject = result.getOverdueDays() > 0
                ? "Повідомлення про штраф"
                : "Підтвердження повернення книги";

        System.out.println("\nНадсилання листа на: " + address);
        System.out.println("Тема: " + subject);
        System.out.println("Текст:\n" + notice);
    }
}