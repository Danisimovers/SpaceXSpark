package sfedu.danil;

import sfedu.danil.service.SpaceXService;
import sfedu.danil.config.SparkConfig;
import sfedu.danil.model.Launch;
import sfedu.danil.repository.LaunchRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class MainCLI {

    private static final LaunchRepository launchRepo = new LaunchRepository();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        // Приветствие и выбор источника данных
        System.out.println("Добро пожаловать в SpaceX-Spark CLI!");
        System.out.println("Выберите источник данных:");
        System.out.println("1. SpaceX API (с загрузкой в Spark)");
        System.out.println("2. Локальный Spark (только CRUD)");
        System.out.print("Введите номер выбора: ");

        String sourceChoice = scanner.nextLine();

        switch (sourceChoice) {
            case "1":
                SpaceXService service = new SpaceXService();
                try {
                    List<Launch> launches = service.getAllLaunches();
                    if (!launches.isEmpty()) {
                        launchRepo.saveAll(launches);
                        System.out.println("Загрузка с API завершена. Всего загружено: " + launches.size());
                    } else {
                        System.out.println("API вернул пустой список запусков.");
                    }
                } catch (IOException | InterruptedException e) {
                    System.err.println("Ошибка при загрузке данных с API: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            case "2":
                System.out.println("Выбрано: Локальный Spark");
                break;
            default:
                System.out.println("Неверный выбор. Программа завершена.");
                SparkConfig.stopSparkSession();
                return;
        }

        while (running) {
            System.out.println("\n=== Главное меню ===");
            System.out.println("1. Добавить запуск");
            System.out.println("2. Просмотреть запуск");
            System.out.println("3. Обновить запуск");
            System.out.println("4. Удалить запуск");
            System.out.println("5. Посмотреть все запуски");
            System.out.println("6. Выйти");
            System.out.print("Выберите опцию: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> addLaunch(scanner);
                case "2" -> viewLaunch(scanner);
                case "3" -> updateLaunch(scanner);
                case "4" -> deleteLaunch(scanner);
                case "5" -> listAllLaunches();
                case "6" -> running = false;
                default -> System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }

        scanner.close();
        SparkConfig.stopSparkSession();
        System.out.println("Программа завершена.");
    }

    private static void addLaunch(Scanner scanner) {
        System.out.print("Введите номер запуска: ");
        int flightNumber = Integer.parseInt(scanner.nextLine());

        if (launchRepo.existsByFlightNumber(flightNumber)) {
            System.out.println("Запуск с таким номером уже существует! Используйте обновление.");
            return;
        }

        System.out.print("Введите дату запуска (UTC, например 2025-10-13T00:00:00Z): ");
        String launchDateUtc = scanner.nextLine();

        Launch launch = new Launch();
        launch.setFlight_number(flightNumber);
        launch.setLaunch_date_utc(launchDateUtc);

        launchRepo.saveByFlightNumber(launch);
        System.out.println("Запуск успешно добавлен.");
    }

    private static void viewLaunch(Scanner scanner) {
        System.out.print("Введите номер запуска для просмотра: ");
        int flightNumber = Integer.parseInt(scanner.nextLine());

        Optional<Launch> launchOpt = launchRepo.readByFlightNumber(flightNumber);
        if (launchOpt.isPresent()) {
            Launch l = launchOpt.get();
            System.out.printf("Flight Number: %d, Launch Date UTC: %s%n",
                    l.getFlight_number(), l.getLaunch_date_utc());
        } else {
            System.out.println("Запуск не найден.");
        }
    }

    private static void updateLaunch(Scanner scanner) {
        System.out.print("Введите номер запуска для обновления: ");
        int flightNumber = Integer.parseInt(scanner.nextLine());

        if (!launchRepo.existsByFlightNumber(flightNumber)) {
            System.out.println("Запуск с таким номером не найден. Сначала добавьте его.");
            return;
        }

        System.out.print("Введите новую дату запуска (UTC): ");
        String launchDateUtc = scanner.nextLine();

        Launch updatedLaunch = new Launch();
        updatedLaunch.setFlight_number(flightNumber);
        updatedLaunch.setLaunch_date_utc(launchDateUtc);

        launchRepo.updateLaunch(flightNumber, updatedLaunch);
        System.out.println("Запуск обновлен.");
    }

    private static void deleteLaunch(Scanner scanner) {
        System.out.print("Введите номер запуска для удаления: ");
        int flightNumber = Integer.parseInt(scanner.nextLine());

        if (!launchRepo.existsByFlightNumber(flightNumber)) {
            System.out.println("Запуск с таким номером не найден.");
            return;
        }

        launchRepo.deleteLaunch(flightNumber);
        System.out.println("Запуск удален.");
    }

    private static void listAllLaunches() {
        List<Launch> allLaunches = launchRepo.readAll();
        if (allLaunches.isEmpty()) {
            System.out.println("Запусков нет.");
        } else {
            for (Launch l : allLaunches) {
                System.out.printf("Flight Number: %d, Launch Date UTC: %s%n",
                        l.getFlight_number(), l.getLaunch_date_utc());
            }
        }
    }
}
