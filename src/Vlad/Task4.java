
import java.math.BigDecimal;

// Завдання 4: Interface Segregation Principle (ISP)
// Замість одного великого інтерфейсу створюємо
// декілька маленьких інтерфейсів за ролями.

public class Task4 {

    // Оплата банківською карткою
    interface CardPayment {
        void processCreditCard(BigDecimal amount, String cardNumber);
    }

    // Банківський переказ
    interface BankTransferPayment {
        void processBankTransfer(BigDecimal amount, String iban);
    }

    // Безконтактна оплата
    interface NfcPayment {
        void processNfcTap(BigDecimal amount);
    }

    // Оплата подарунковою карткою
    interface GiftCardPayment {
        void processGiftCard(BigDecimal amount, String voucherCode);
    }

    // Друк паперового чека
    interface PaperReceiptPrinter {
        void printPaperReceipt(String text);
    }

    // Спільна перевірка вхідних даних — DRY
    static class PaymentChecks {
        static void requireAmount(BigDecimal amount) {
            if (amount == null || amount.signum() <= 0) {
                throw new IllegalArgumentException(
                        "Amount must be positive"
                );
            }
        }

        static void requireText(String text) {
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException(
                        "Text cannot be empty"
                );
            }
        }
    }

    // Онлайн-каса підтримує лише картки та перекази
    static class OnlineWebCheckoutTerminal
            implements CardPayment, BankTransferPayment {

        public void processCreditCard(
                BigDecimal amount, String cardNumber
        ) {
            PaymentChecks.requireAmount(amount);
            PaymentChecks.requireText(cardNumber);

            System.out.println(
                    "ONLINE: Card payment -> " + amount
            );
        }

        public void processBankTransfer(
                BigDecimal amount, String iban
        ) {
            PaymentChecks.requireAmount(amount);
            PaymentChecks.requireText(iban);

            System.out.println(
                    "ONLINE: Bank transfer -> " + amount
            );
        }
    }

    // POS-термінал підтримує картки, NFC та друк чека
    static class PosCardReader
            implements CardPayment, NfcPayment, PaperReceiptPrinter {

        public void processCreditCard(
                BigDecimal amount, String cardNumber
        ) {
            PaymentChecks.requireAmount(amount);
            PaymentChecks.requireText(cardNumber);

            System.out.println(
                    "POS: Card payment -> " + amount
            );
        }

        public void processNfcTap(BigDecimal amount) {
            PaymentChecks.requireAmount(amount);

            System.out.println(
                    "POS: NFC payment -> " + amount
            );
        }

        public void printPaperReceipt(String text) {
            PaymentChecks.requireText(text);

            System.out.println("Receipt: " + text);
        }
    }

    // Клієнт залежить тільки від потрібного інтерфейсу
    static void payByCard(CardPayment terminal) {
        terminal.processCreditCard(
                new BigDecimal("120.50"),
                "TEST-CARD"
        );
    }

    static void payByNfc(NfcPayment terminal) {
        terminal.processNfcTap(new BigDecimal("25.00"));
    }

    static void payByBank(BankTransferPayment terminal) {
        terminal.processBankTransfer(
                new BigDecimal("70.00"),
                "TEST-IBAN"
        );
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 4: ISP ===");

        OnlineWebCheckoutTerminal online =
                new OnlineWebCheckoutTerminal();

        PosCardReader pos = new PosCardReader();

        // Демонстрація онлайн-каси
        payByCard(online);
        payByBank(online);

        // Демонстрація POS-термінала
        payByCard(pos);
        payByNfc(pos);
        pos.printPaperReceipt("Payment successful");

        System.out.println("Task 4 completed successfully!");
    }
}
