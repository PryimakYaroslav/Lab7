package Yarik;

// Головний клас: демонстрація роботи транспортних засобів
public class Task3 {
    public static void main(String[] args) {
        PetrolVehicle car = new PetrolVehicle("AA1234BB", 20, 60);
        ElectricVehicle electricCar = new ElectricVehicle("BC5678EE", 50, 80);

        car.refuel(10);
        electricCar.charge(15);

        car.drive(100);
        electricCar.drive(100);

        System.out.println("\nПробіг бензинового авто: "
                + car.getMileageKm() + " км");
        System.out.println("Пробіг електромобіля: "
                + electricCar.getMileageKm() + " км");
    }
}

// Базовий абстрактний клас із властивостями та спільною поведінкою
abstract class Vehicle {
    private final String licensePlate;
    private double mileageKm;

    public Vehicle(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank())
            throw new IllegalArgumentException("Номерний знак обов'язковий.");

        this.licensePlate = licensePlate;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public double getMileageKm() {
        return mileageKm;
    }

    protected void addMileage(double distance) {
        if (!Double.isFinite(distance) || distance <= 0)
            throw new IllegalArgumentException("Некоректна відстань.");

        mileageKm += distance;
    }
    public abstract void drive(double distance);
}

// Окремий інтерфейс для транспортних засобів, які заправляються паливом
interface Refuelable {
    void refuel(double liters);
    double getFuelLevelLiters();
    double getTankCapacityLiters();
}

// Окремий інтерфейс для транспортних засобів, які заряджаються
interface Chargeable {
    void charge(double kWh);
    double getBatteryLevelKWh();
    double getBatteryCapacityKWh();
}

// Бензиновий автомобіль: успадковує спільні властивості Vehicle
// та реалізує лише інтерфейс заправлення Refuelable
class PetrolVehicle extends Vehicle implements Refuelable {
    private double fuel;
    private final double tankCapacity;
    private static final double FUEL_PER_KM = 0.08;

    // Конструктор перевіряє початковий рівень палива та місткість бака
    public PetrolVehicle(String plate, double fuel, double capacity) {
        super(plate);
        if (!Double.isFinite(capacity) || capacity <= 0
                || !Double.isFinite(fuel) || fuel < 0 || fuel > capacity)
            throw new IllegalArgumentException("Некоректні параметри бака.");

        this.fuel = fuel;
        this.tankCapacity = capacity;
    }

    @Override
    public double getFuelLevelLiters() {
        return fuel;
    }

    @Override
    public double getTankCapacityLiters() {
        return tankCapacity;
    }

    // Додає паливо, якщо не перевищено місткість бака
    @Override
    public void refuel(double liters) {
        if (!Double.isFinite(liters) || liters <= 0)
            throw new IllegalArgumentException("Некоректна кількість палива.");
        if (fuel + liters > tankCapacity)
            throw new IllegalStateException("Перевищено місткість бака.");

        fuel += liters;
        System.out.println("Авто заправлено на " + liters
                + " л. Залишок: " + fuel + " л.");
    }
    @Override
    public void drive(double distance) {
        if (!Double.isFinite(distance) || distance <= 0)
            throw new IllegalArgumentException("Некоректна відстань.");

        double consumed = distance * FUEL_PER_KM;
        if (consumed > fuel)
            throw new IllegalStateException("Недостатньо палива.");

        fuel -= consumed;
        addMileage(distance);
        System.out.println("Авто проїхало " + distance
                + " км. Палива залишилось: " + fuel + " л.");
    }
}

// Електромобіль: успадковує Vehicle та реалізує інтерфейс Chargeable
class ElectricVehicle extends Vehicle implements Chargeable {
    private double battery;
    private final double batteryCapacity;
    private static final double ENERGY_PER_KM = 0.18;

    // Конструктор перевіряє початковий заряд і місткість батареї
    public ElectricVehicle(String plate, double battery, double capacity) {
        super(plate);
        if (!Double.isFinite(capacity) || capacity <= 0
                || !Double.isFinite(battery)
                || battery < 0 || battery > capacity)
            throw new IllegalArgumentException("Некоректні параметри батареї.");

        this.battery = battery;
        this.batteryCapacity = capacity;
    }

    @Override
    public double getBatteryLevelKWh() {
        return battery;
    }

    @Override
    public double getBatteryCapacityKWh() {
        return batteryCapacity;
    }

    @Override
    public void charge(double kWh) {
        if (!Double.isFinite(kWh) || kWh <= 0)
            throw new IllegalArgumentException("Некоректна кількість енергії.");
        if (battery + kWh > batteryCapacity)
            throw new IllegalStateException("Перевищено місткість батареї.");

        battery += kWh;
        System.out.println("Електромобіль заряджено на " + kWh
                + " кВт·год. Заряд: " + battery + " кВт·год.");
    }

    @Override
    public void drive(double distance) {
        if (!Double.isFinite(distance) || distance <= 0)
            throw new IllegalArgumentException("Некоректна відстань.");

        double consumed = distance * ENERGY_PER_KM;
        if (consumed > battery)
            throw new IllegalStateException("Недостатньо заряду.");

        battery -= consumed;
        addMileage(distance);
        System.out.println("Електромобіль проїхав " + distance
                + " км. Заряд залишився: " + battery + " кВт·год.");
    }
}