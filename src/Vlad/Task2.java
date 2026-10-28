package Vlad;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// (OCP) принцип відкритості/закритості.
// Замість великого if-else використовуємо патерн Strategy

public class Task2 {

    // Дані клієнта страхової компанії
    record PolicyHolder(
            String id,
            int age,
            BigDecimal insuredAmount,
            boolean hasPriorClaims
    ) {
        PolicyHolder {
            if (id == null || id.isBlank())
                throw new IllegalArgumentException("ID is required");

            if (age < 0 || age > 120)
                throw new IllegalArgumentException("Invalid age");

            if (insuredAmount == null || insuredAmount.signum() < 0)
                throw new IllegalArgumentException("Invalid amount");
        }
    }

    // Точка розширення системи
    interface PremiumStrategy {
        String policyName();
        BigDecimal calculate(PolicyHolder holder);
    }

    // Автострахування
    static class AutoStrategy implements PremiumStrategy {
        public String policyName() {
            return "AUTO";
        }

        public BigDecimal calculate(PolicyHolder h) {
            BigDecimal premium =
                    h.insuredAmount().multiply(new BigDecimal("0.03"));

            if (h.age() < 25 || h.hasPriorClaims()) {
                premium = premium.multiply(new BigDecimal("1.20"));
            }

            return premium;
        }
    }

    // Медичне страхування
    static class HealthStrategy implements PremiumStrategy {
        public String policyName() {
            return "HEALTH";
        }

        public BigDecimal calculate(PolicyHolder h) {
            BigDecimal premium = new BigDecimal("200");

            if (h.age() > 40) {
                premium = premium.add(
                        BigDecimal.valueOf((h.age() - 40) * 10L)
                );
            }

            if (h.hasPriorClaims()) {
                premium = premium.add(new BigDecimal("150"));
            }

            return premium;
        }
    }

    // Страхування майна
    static class PropertyStrategy implements PremiumStrategy {
        public String policyName() {
            return "PROPERTY";
        }

        public BigDecimal calculate(PolicyHolder h) {
            return h.insuredAmount()
                    .multiply(new BigDecimal("0.005"));
        }
    }

    // Туристичне страхування
    static class TravelStrategy implements PremiumStrategy {
        public String policyName() {
            return "TRAVEL";
        }

        public BigDecimal calculate(PolicyHolder h) {
            BigDecimal premium = new BigDecimal("30");

            if (h.age() >= 65) {
                premium = premium.multiply(new BigDecimal("1.50"));
            }

            return premium;
        }
    }

    // НОВИЙ тип страхування — для демонстрації OCP
    static class PetStrategy implements PremiumStrategy {
        public String policyName() {
            return "PET";
        }

        public BigDecimal calculate(PolicyHolder h) {
            BigDecimal premium = new BigDecimal("40");

            if (h.hasPriorClaims()) {
                premium = premium.add(new BigDecimal("15"));
            }

            return premium;
        }
    }

    // Основний калькулятор не залежить від типу страхування
    static class InsurancePremiumCalculator {
        public BigDecimal calculatePremium(
                PolicyHolder holder,
                PremiumStrategy strategy
        ) {
            BigDecimal premium = strategy.calculate(holder)//викликає потрібн стратегію страхув
                    .setScale(2, RoundingMode.HALF_UP);

            if (premium.signum() < 0) {
                throw new IllegalStateException("Negative premium");
            }

            System.out.println(
                    strategy.policyName() + " -> $" + premium
            );

            return premium;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 2: OCP ===");

        PolicyHolder client = new PolicyHolder(
                "C-001",
                22,
                new BigDecimal("10000"),
                true
        );

        InsurancePremiumCalculator calculator =
                new InsurancePremiumCalculator();
        //стратегії
        List<PremiumStrategy> strategies = List.of(
                new AutoStrategy(),
                new HealthStrategy(),
                new PropertyStrategy(),
                new TravelStrategy(),
                new PetStrategy()
        );

        for (PremiumStrategy strategy : strategies) {
            calculator.calculatePremium(client, strategy);
        }

        System.out.println("Task 2 completed successfully!");
    }
}
