
package Yarik;

// 1. ГОЛОВНИЙ КЛАС — COMPOSITION ROOT
public class Task5 {
    public static void main(String[] args) {
        // Створюємо конкретні реалізації залежностей
        DocumentIndex index = new ProductionDocumentIndex();
        UserSignatureValidator validator = new ProductionUserSignatureValidator();
        NotificationService notifier = new ProductionNotificationService();

        // Constructor Injection: передаємо залежності через конструктор
        DocumentApprovalWorkflow workflow =
                new DocumentApprovalWorkflow(index, validator, notifier);

        Document doc = new Document(
                "DOC-101", "Договір про співпрацю", "ivan", "manager");

        workflow.approveDocument(doc);

        // Демонструємо підміну робочих реалізацій тестовими
        System.out.println("\n--- ТЕСТОВИЙ РЕЖИМ ---");
        DocumentApprovalWorkflow testWorkflow =
                new DocumentApprovalWorkflow(
                        new MockDocumentIndex(),
                        new MockUserSignatureValidator(),
                        new MockNotificationService());

        testWorkflow.approveDocument(doc);
    }

    // 2. МОДЕЛЬ ДОКУМЕНТА
    public static class Document {
        private final String documentId;
        private final String title;
        private final String authorUsername;
        private final String approverUsername;

        public Document(String id, String title, String author, String approver) {
            this.documentId = id;
            this.title = title;
            this.authorUsername = author;
            this.approverUsername = approver;
        }

        public String getDocumentId() { return documentId; }
        public String getTitle() { return title; }
        public String getAuthorUsername() { return authorUsername; }
        public String getApproverUsername() { return approverUsername; }
    }

    // 3. АБСТРАКЦІЇ — ІНТЕРФЕЙСИ
    public interface DocumentIndex {
        void indexDocument(String id, String title, String status);
    }

    public interface UserSignatureValidator {
        boolean validateUserSignature(String username);
    }

    public interface NotificationService {
        void sendMessage(String channel, String message);
    }

    // 4. ВИСОКОРІВНЕВА БІЗНЕС-ЛОГІКА
    public static class DocumentApprovalWorkflow {
        private final DocumentIndex index;
        private final UserSignatureValidator validator;
        private final NotificationService notifier;

        // Workflow залежить від абстракцій, а не конкретних сервісів
        public DocumentApprovalWorkflow(DocumentIndex index,
                                        UserSignatureValidator validator, NotificationService notifier) {
            if (index == null || validator == null || notifier == null)
                throw new IllegalArgumentException("Залежності обов'язкові.");

            this.index = index;
            this.validator = validator;
            this.notifier = notifier;
        }

        public void approveDocument(Document doc) {
            if (doc == null || doc.getDocumentId() == null
                    || doc.getDocumentId().isBlank()
                    || doc.getTitle() == null || doc.getTitle().isBlank()
                    || doc.getApproverUsername() == null
                    || doc.getApproverUsername().isBlank())
                throw new IllegalArgumentException("Некоректний документ.");

            // Перевіряємо повноваження підписанта
            if (!validator.validateUserSignature(doc.getApproverUsername()))
                throw new IllegalStateException("Підписант не має повноважень.");

            // Зберігаємо статус документа через абстракцію індексу
            index.indexDocument(doc.getDocumentId(), doc.getTitle(), "APPROVED");

            // Надсилаємо сповіщення через абстракцію сервісу
            notifier.sendMessage("approvals",
                    "Документ '" + doc.getTitle() + "' успішно підписано.");

            System.out.println("Документ успішно узгоджено.");
        }
    }

    // 5. РОБОЧІ РЕАЛІЗАЦІЇ ЗАЛЕЖНОСТЕЙ
    // Демонстраційні імітації Elasticsearch, Active Directory та Slack
    public static class ProductionDocumentIndex implements DocumentIndex {
        @Override
        public void indexDocument(String id, String title, String status) {
            System.out.println("[Elasticsearch] " + id + ": " + status);
        }
    }

    public static class ProductionUserSignatureValidator
            implements UserSignatureValidator {
        @Override
        public boolean validateUserSignature(String username) {
            System.out.println("[Active Directory] Перевірка: " + username);
            return true; // Імітація успішної перевірки
        }
    }

    public static class ProductionNotificationService
            implements NotificationService {
        @Override
        public void sendMessage(String channel, String message) {
            System.out.println("[Slack] #" + channel + ": " + message);
        }
    }

    // 6. ТЕСТОВІ РЕАЛІЗАЦІЇ (MOCK)
    // Дозволяють перевіряти бізнес-логіку без реальних зовнішніх сервісів
    public static class MockDocumentIndex implements DocumentIndex {
        @Override
        public void indexDocument(String id, String title, String status) {
            System.out.println("[Mock Index] " + id + ": " + status);
        }
    }

    public static class MockUserSignatureValidator
            implements UserSignatureValidator {
        @Override
        public boolean validateUserSignature(String username) {
            System.out.println("[Mock Validator] Перевірка: " + username);
            return true;
        }
    }

    public static class MockNotificationService implements NotificationService {
        @Override
        public void sendMessage(String channel, String message) {
            System.out.println("[Mock Notification] #" + channel
                    + ": " + message);
        }
    }
}