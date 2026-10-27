package Yarik;

// =====================================================
// 1. ГОЛОВНИЙ КЛАС
// =====================================================

public class Task3 {

    public static void main(String[] args) {
        PetrolVehicle car = new PetrolVehicle(
                "AA1234BB", 20, 60);

        ElectricVehicle electricCar = new ElectricVehicle(
                "BC5678EE", 50, 80);

        // Заправка бензинового автомобіля
        car.refuel(10);

        // Заряджання електромобіля
        electricCar.charge(15);

        // Рух транспортних засобів
        car.drive(100);
        electricCar.drive(100);

        System.out.println("\nПробіг бензинового автомобіля: "
                + car.getMileageKm() + " км");

        System.out.println("Пробіг електромобіля: "
                + electricCar.getMileageKm() + " км");
    }
}


// =====================================================
// 2. БАЗОВИЙ КЛАС ТРАНСПОРТНОГО ЗАСОБУ
// =====================================================

abstract class Vehicle {

    private final String licensePlate;
    private double mileageKm;

    public Vehicle(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException(
                    "Номерний знак обов'язковий.");
        }

        this.licensePlate = licensePlate;
        this.mileageKm = 0;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public double getMileageKm() {
        return mileageKm;
    }

    protected void addMileage(double distanceKm) {
        if (!Double.isFinite(distanceKm) || distanceKm <= 0) {
            throw new IllegalArgumentException(
                    "Відстань повинна бути додатною.");
        }

        mileageKm += distanceKm;
    }

    public abstract void drive(double distanceKm);
}


// =====================================================
// 3. ІНТЕРФЕЙС ТРАНСПОРТУ, ЩО ЗАПРАВЛЯЄТЬСЯ
// =====================================================

interface Refuelable {

    double getFuelLevelLiters();

    double getTankCapacityLiters();

    void refuel(double liters);
}


// =====================================================
// 4. ІНТЕРФЕЙС ТРАНСПОРТУ, ЩО ЗАРЯДЖАЄТЬСЯ
// =====================================================

interface Chargeable {

    double getBatteryLevelKWh();

    double getBatteryCapacityKWh();

    void charge(double kWh);
}


// =====================================================
// 5. БЕНЗИНОВИЙ АВТОМОБІЛЬ
// =====================================================

class PetrolVehicle extends Vehicle implements Refuelable {

    private double fuelLevelLiters;
    private final double tankCapacityLiters;

    private static final double FUEL_PER_KM = 0.08;

    public PetrolVehicle(String licensePlate,
                         double fuelLevelLiters,
                         double tankCapacityLiters) {
        super(licensePlate);

        if (!Double.isFinite(tankCapacityLiters)
                || tankCapacityLiters <= 0) {
            throw new IllegalArgumentException(
                    "Місткість бака повинна бути додатною.");
        }

        if (!Double.isFinite(fuelLevelLiters)
                || fuelLevelLiters < 0
                || fuelLevelLiters > tankCapacityLiters) {
            throw new IllegalArgumentException(
                    "Некоректний рівень палива.");
        }

        this.fuelLevelLiters = fuelLevelLiters;
        this.tankCapacityLiters = tankCapacityLiters;
    }

    @Override
    public double getFuelLevelLiters() {
        return fuelLevelLiters;
    }

    @Override
    public double getTankCapacityLiters() {
        return tankCapacityLiters;
    }

    @Override
    public void refuel(double liters) {
        if (!Double.isFinite(liters) || liters <= 0) {
            throw new IllegalArgumentException(
                    "Кількість палива повинна бути додатною.");
        }

        if (fuelLevelLiters + liters > tankCapacityLiters) {
            throw new IllegalStateException(
                    "Перевищено місткість паливного бака!");
        }

        fuelLevelLiters += liters;

        System.out.println("Автомобіль " + getLicensePlate()
                + " заправлено на " + liters
                + " л. Залишок: " + fuelLevelLiters + " л.");
    }

    @Override
    public void drive(double distanceKm) {
        if (!Double.isFinite(distanceKm) || distanceKm <= 0) {
            throw new IllegalArgumentException(
                    "Відстань повинна бути додатною.");
        }

        double fuelConsumed = distanceKm * FUEL_PER_KM;

        if (fuelConsumed > fuelLevelLiters) {
            throw new IllegalStateException(
                    "Недостатньо палива для поїздки!");
        }

        fuelLevelLiters -= fuelConsumed;
        addMileage(distanceKm);

        System.out.println("Автомобіль " + getLicensePlate()
                + " проїхав " + distanceKm
                + " км. Залишок палива: "
                + fuelLevelLiters + " л.");
    }
}


// =====================================================
// 6. ЕЛЕКТРОМОБІЛЬ
// =====================================================

class ElectricVehicle extends Vehicle implements Chargeable {

    private double batteryLevelKWh;
    private final double batteryCapacityKWh;

    private static final double ENERGY_PER_KM = 0.18;

    public ElectricVehicle(String licensePlate,
                           double batteryLevelKWh,
                           double batteryCapacityKWh) {
        super(licensePlate);

        if (!Double.isFinite(batteryCapacityKWh)
                || batteryCapacityKWh <= 0) {
            throw new IllegalArgumentException(
                    "Місткість батареї повинна бути додатною.");
        }

        if (!Double.isFinite(batteryLevelKWh)
                || batteryLevelKWh < 0
                || batteryLevelKWh > batteryCapacityKWh) {
            throw new IllegalArgumentException(
                    "Некоректний рівень заряду батареї.");
        }

        this.batteryLevelKWh = batteryLevelKWh;
        this.batteryCapacityKWh = batteryCapacityKWh;
    }

    @Override
    public double getBatteryLevelKWh() {
        return batteryLevelKWh;
    }

    @Override
    public double getBatteryCapacityKWh() {
        return batteryCapacityKWh;
    }

    @Override
    public void charge(double kWh) {
        if (!Double.isFinite(kWh) || kWh <= 0) {
            throw new IllegalArgumentException(
                    "Кількість енергії повинна бути додатною.");
        }

        if (batteryLevelKWh + kWh > batteryCapacityKWh) {
            throw new IllegalStateException(
                    "Перевищено місткість батареї!");
        }

        batteryLevelKWh += kWh;

        System.out.println("Електромобіль " + getLicensePlate()
                + " заряджено на " + kWh
                + " кВт·год. Заряд: "
                + batteryLevelKWh + " кВт·год.");
    }

    @Override
    public void drive(double distanceKm) {
        if (!Double.isFinite(distanceKm) || distanceKm <= 0) {
            throw new IllegalArgumentException(
                    "Відстань повинна бути додатною.");
        }

        double energyConsumed = distanceKm * ENERGY_PER_KM;

        if (energyConsumed > batteryLevelKWh) {
            throw new IllegalStateException(
                    "Недостатньо заряду для поїздки!");
        }

        batteryLevelKWh -= energyConsumed;
        addMileage(distanceKm);

        System.out.println("Електромобіль " + getLicensePlate()
                + " проїхав " + distanceKm
                + " км. Залишок заряду: "
                + batteryLevelKWh + " кВт·год.");
    }
}
