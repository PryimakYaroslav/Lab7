package Yarik;

// =====================================================
// 1. ГОЛОВНИЙ КЛАС І COMPOSITION ROOT
// =====================================================

public class Task5 {

    public static void main(String[] args) {
        // Створення конкретних реалізацій залежностей
        DocumentIndex index = new ProductionDocumentIndex();
        UserSignatureValidator validator =
                new ProductionUserSignatureValidator();
        NotificationService notifier =
                new ProductionNotificationService();

        // Впровадження залежностей через конструктор
        DocumentApprovalWorkflow workflow =
                new DocumentApprovalWorkflow(index, validator, notifier);

        Document doc = new Document();
        doc.setDocumentId("DOC-101");
        doc.setTitle("Договір про співпрацю");
        doc.setAuthorUsername("ivan");
        doc.setApproverUsername("manager");

        workflow.approveDocument(doc);

        // Демонстрація підміни реалізацій для тестування
        System.out.println("\n--- ТЕСТОВИЙ РЕЖИМ ---");

        DocumentApprovalWorkflow testWorkflow =
                new DocumentApprovalWorkflow(
                        new MockDocumentIndex(),
                        new MockUserSignatureValidator(),
                        new MockNotificationService());

        testWorkflow.approveDocument(doc);
    }


    // =====================================================
    // 2. МОДЕЛЬ ДОКУМЕНТА
    // =====================================================

    public static class Document {
        private String documentId;
        private String title;
        private String authorUsername;
        private String approverUsername;

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthorUsername() {
            return authorUsername;
        }

        public void setAuthorUsername(String authorUsername) {
            this.authorUsername = authorUsername;
        }

        public String getApproverUsername() {
            return approverUsername;
        }

        public void setApproverUsername(String approverUsername) {
            this.approverUsername = approverUsername;
        }
    }


    // =====================================================
    // 3. АБСТРАКЦІЇ — ІНТЕРФЕЙСИ
    // =====================================================

    // Абстракція для пошукового індексу
    public interface DocumentIndex {
        void indexDocument(String docId, String title, String status);
    }

    // Абстракція для перевірки повноважень підписанта
    public interface UserSignatureValidator {
        boolean validateUserSignature(String username);
    }

    // Абстракція для надсилання сповіщень
    public interface NotificationService {
        void sendMessage(String channel, String message);
    }


    // =====================================================
    // 4. ВИСОКОРІВНЕВА БІЗНЕС-ЛОГІКА
    // =====================================================

    public static class DocumentApprovalWorkflow {

        private final DocumentIndex searchIndex;
        private final UserSignatureValidator adValidator;
        private final NotificationService notifier;

        // Constructor Injection — залежності передаються ззовні
        public DocumentApprovalWorkflow(
                DocumentIndex searchIndex,
                UserSignatureValidator adValidator,
                NotificationService notifier) {

            if (searchIndex == null
                    || adValidator == null
                    || notifier == null) {
                throw new IllegalArgumentException(
                        "Усі залежності повинні бути задані.");
            }

            this.searchIndex = searchIndex;
            this.adValidator = adValidator;
            this.notifier = notifier;
        }

        public void approveDocument(Document doc) {
            if (doc == null
                    || doc.getDocumentId() == null
                    || doc.getDocumentId().isBlank()
                    || doc.getTitle() == null
                    || doc.getTitle().isBlank()
                    || doc.getApproverUsername() == null
                    || doc.getApproverUsername().isBlank()) {
                throw new IllegalArgumentException(
                        "Некоректний документ для узгодження.");
            }

            boolean isValidApprover =
                    adValidator.validateUserSignature(
                            doc.getApproverUsername());

            if (!isValidApprover) {
                throw new IllegalStateException(
                        "Підписант не має повноважень для підпису.");
            }

            searchIndex.indexDocument(
                    doc.getDocumentId(),
                    doc.getTitle(),
                    "APPROVED");

            notifier.sendMessage(
                    "approvals",
                    "Документ '" + doc.getTitle()
                            + "' (ID: " + doc.getDocumentId()
                            + ") успішно підписано користувачем "
                            + doc.getApproverUsername() + ".");

            System.out.println("Документ успішно узгоджено.");
        }
    }


    // =====================================================
    // 5. РОБОЧА РЕАЛІЗАЦІЯ ПОШУКОВОГО ІНДЕКСУ
    // =====================================================

    public static class ProductionDocumentIndex
            implements DocumentIndex {

        @Override
        public void indexDocument(
                String docId, String title, String status) {

            System.out.println(
                    "[Elasticsearch] Документ " + docId
                            + " ('" + title
                            + "') проіндексовано зі статусом '"
                            + status + "'.");
        }
    }


    // =====================================================
    // 6. РОБОЧА РЕАЛІЗАЦІЯ ПЕРЕВІРКИ ПІДПИСАНТА
    // =====================================================

    public static class ProductionUserSignatureValidator
            implements UserSignatureValidator {

        @Override
        public boolean validateUserSignature(String username) {
            System.out.println(
                    "[Active Directory] Перевірка користувача '"
                            + username + "'.");

            // Демонстраційна імітація перевірки
            return true;
        }
    }


    // =====================================================
    // 7. РОБОЧА РЕАЛІЗАЦІЯ СПОВІЩЕНЬ
    // =====================================================

    public static class ProductionNotificationService
            implements NotificationService {

        @Override
        public void sendMessage(String channel, String message) {
            System.out.println(
                    "[Slack] Повідомлення у канал #"
                            + channel + ": " + message);
        }
    }


    // =====================================================
    // 8. ТЕСТОВА РЕАЛІЗАЦІЯ ПОШУКОВОГО ІНДЕКСУ
    // =====================================================

    public static class MockDocumentIndex implements DocumentIndex {

        @Override
        public void indexDocument(
                String docId, String title, String status) {

            System.out.println(
                    "[Mock Index] Документ " + docId
                            + " отримав статус '" + status + "'.");
        }
    }


    // =====================================================
    // 9. ТЕСТОВА РЕАЛІЗАЦІЯ ПЕРЕВІРКИ ПІДПИСАНТА
    // =====================================================

    public static class MockUserSignatureValidator
            implements UserSignatureValidator {

        @Override
        public boolean validateUserSignature(String username) {
            System.out.println(
                    "[Mock Validator] Перевірка користувача: "
                            + username);

            return true;
        }
    }


    // =====================================================
    // 10. ТЕСТОВА РЕАЛІЗАЦІЯ СПОВІЩЕНЬ
    // =====================================================

    public static class MockNotificationService
            implements NotificationService {

        @Override
        public void sendMessage(String channel, String message) {
            System.out.println(
                    "[Mock Notification] Канал: #" + channel);
            System.out.println("Повідомлення: " + message);
        }
    }
}
