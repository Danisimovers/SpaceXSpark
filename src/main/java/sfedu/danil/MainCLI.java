package sfedu.danil;

import sfedu.danil.entity.*;
import sfedu.danil.model.Payload;
import sfedu.danil.model.LaunchSite;
import sfedu.danil.model.SecondStage;
import sfedu.danil.model.Rocket;
import sfedu.danil.service.SpaceXService;
import sfedu.danil.config.SparkConfig;
import sfedu.danil.model.Launch;
import sfedu.danil.repository.LaunchRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import sfedu.danil.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


public class MainCLI {
    static {
        System.setProperty("hadoop.home.dir", System.getProperty("user.dir"));
        System.setProperty("spark.hadoop.fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");
        System.setProperty("spark.hadoop.fs.file.impl.disable.cache", "true");
    }


    private static final LaunchRepository launchRepo = new LaunchRepository();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

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
            System.out.println("6. Сохранить данные в CSV");
            System.out.println("7. Сохранить данные в PostgreSQL");
            System.out.println("8. Выйти");
            System.out.print("Выберите опцию: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> addLaunch(scanner);
                case "2" -> viewLaunch(scanner);
                case "3" -> updateLaunch(scanner);
                case "4" -> deleteLaunch(scanner);
                case "5" -> listAllLaunches();
                case "6" -> exportToCsv();
                case "7" -> saveToPostgreSQL(scanner);
                case "8" -> running = false;
                default -> System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }

        scanner.close();
        SparkConfig.stopSparkSession();
        System.out.println("Программа завершена.");
    }


    private static void saveToPostgreSQL(Scanner scanner) {
        System.out.println("\n=== Сохранение в PostgreSQL ===");

        try {
            List<Launch> launches = launchRepo.readAll();

            if (launches.isEmpty()) {
                System.out.println("Нет данных для сохранения в PostgreSQL.");
                return;
            }

            System.out.println("Найдено " + launches.size() + " записей для сохранения.");
            System.out.print("Сохранить все записи? (y/n): ");
            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("y")) {
                SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
                int savedCount = 0;

                for (Launch launch : launches) {
                    Session session = sessionFactory.openSession();
                    Transaction transaction = null;

                    try {
                        transaction = session.beginTransaction();

                        // Конвертируем Launch в LaunchEntity
                        LaunchEntity launchEntity = new LaunchEntity();
                        launchEntity.setFlight_number(launch.getFlight_number());
                        launchEntity.setLaunch_date_utc(launch.getLaunch_date_utc());
                        launchEntity.setLaunch_success(launch.isLaunch_success());

                        // Конвертируем LaunchSite
                        if (launch.getLaunch_site() != null) {
                            LaunchSiteEntity siteEntity = new LaunchSiteEntity();
                            siteEntity.setSite_id(launch.getLaunch_site().getSite_id());
                            siteEntity.setSite_name(launch.getLaunch_site().getSite_name());
                            siteEntity.setSite_name_long(launch.getLaunch_site().getSite_name_long());
                            launchEntity.setLaunch_site(siteEntity);
                        }

                        // Конвертируем Rocket
                        if (launch.getRocket() != null) {
                            RocketEntity rocketEntity = new RocketEntity();
                            rocketEntity.setRocket_id(launch.getRocket().getRocket_id());
                            rocketEntity.setRocket_name(launch.getRocket().getRocket_name());
                            rocketEntity.setRocket_type(launch.getRocket().getRocket_type());

                            // Конвертируем SecondStage если есть
                            if (launch.getRocket().getSecond_stage() != null) {
                                SecondStageEntity stageEntity = new SecondStageEntity();
                                stageEntity.setBlock(launch.getRocket().getSecond_stage().getBlock());

                                // СОХРАНЯЕМ PAYLOADS
                                if (launch.getRocket().getSecond_stage().getPayloads() != null) {
                                    List<PayloadEntity> payloadEntities = new ArrayList<>();

                                    for (Payload payload : launch.getRocket().getSecond_stage().getPayloads()) {
                                        PayloadEntity payloadEntity = new PayloadEntity();
                                        payloadEntity.setPayload_id(payload.getPayload_id());
                                        payloadEntity.setNationality(payload.getNationality());
                                        payloadEntity.setPayload_type(payload.getPayload_type());
                                        payloadEntity.setOrbit(payload.getOrbit());
                                        payloadEntity.setSecond_stage(stageEntity);

                                        payloadEntities.add(payloadEntity);
                                    }

                                    stageEntity.setPayloads(payloadEntities);
                                }

                                rocketEntity.setSecond_stage(stageEntity);
                            }

                            launchEntity.setRocket(rocketEntity);
                        }

                        session.merge(launchEntity);
                        transaction.commit();
                        savedCount++;
                        System.out.println("Сохранен запуск #" + launch.getFlight_number());

                    } catch (Exception e) {
                        if (transaction != null) transaction.rollback();
                        System.err.println("Ошибка при сохранении запуска #" + launch.getFlight_number());
                        e.printStackTrace();
                    } finally {
                        session.close();
                    }
                }

                System.out.println("Успешно сохранено записей в PostgreSQL: " + savedCount + "/" + launches.size());
            } else {
                System.out.println("Сохранение отменено.");
            }

        } catch (Exception e) {
            System.err.println("Ошибка при сохранении в PostgreSQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void exportToCsv() {
        launchRepo.exportToCsv();
        System.out.println("Данные сохранены в CSV");
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

        System.out.print("Введите место запуска: ");
        String launchSiteName = scanner.nextLine();

        // === Ракета ===
        System.out.println("\n--- Данные ракеты ---");
        System.out.print("Введите ID ракеты: ");
        String rocketId = scanner.nextLine();

        System.out.print("Введите название ракеты: ");
        String rocketName = scanner.nextLine();

        System.out.print("Введите тип ракеты: ");
        String rocketType = scanner.nextLine();

        System.out.println("\n--- Данные полезной нагрузки ---");
        System.out.print("Введите количество полезных нагрузок: ");
        int payloadCount = Integer.parseInt(scanner.nextLine());

        List<Payload> payloads = new ArrayList<>();
        for (int i = 0; i < payloadCount; i++) {
            System.out.println("\nНагрузка #" + (i + 1));
            System.out.print("Введите ID нагрузки: ");
            String payloadId = scanner.nextLine();
            System.out.print("Введите страну происхождения: ");
            String nationality = scanner.nextLine();
            System.out.print("Введите тип нагрузки: ");
            String payloadType = scanner.nextLine();
            System.out.print("Введите орбиту: ");
            String orbit = scanner.nextLine();
            payloads.add(new Payload(payloadId, nationality, payloadType, orbit));
        }

        // === Сборка связей ===
        SecondStage secondStage = new SecondStage(1, payloads);
        Rocket rocket = new Rocket(rocketId, rocketName, rocketType, secondStage);
        LaunchSite launchSite = new LaunchSite("manual", launchSiteName, launchSiteName);

        // === Создание объекта Launch ===
        Launch launch = new Launch();
        launch.setFlight_number(flightNumber);
        launch.setLaunch_date_utc(launchDateUtc);
        launch.setLaunch_site(launchSite);
        launch.setRocket(rocket);
        launch.setLaunch_success(false);

        launchRepo.saveByFlightNumber(launch);
        System.out.println("Запуск успешно добавлен!");
    }

        private static void viewLaunch(Scanner scanner) {
        System.out.print("Введите номер запуска для просмотра: ");
        int flightNumber = Integer.parseInt(scanner.nextLine());

        Optional<Launch> launchOpt = launchRepo.readByFlightNumber(flightNumber);
        if (launchOpt.isEmpty()) {
            System.out.println("Запуск не найден.");
            return;
        }

        Launch l = launchOpt.get();

        System.out.println("\n=== Информация о запуске ===");
        System.out.println("Номер запуска: " + l.getFlight_number());
        System.out.println("Дата (UTC): " + l.getLaunch_date_utc());
        System.out.println("Место запуска: " + (l.getLaunch_site() != null ? l.getLaunch_site().getSite_name() : "неизвестно"));
        System.out.println("Успешен: " + (l.isLaunch_success() ? "Да" : "Нет"));

        // === Ракета ===
        Rocket rocket = l.getRocket();
        if (rocket != null) {
            System.out.println("\n--- Ракета ---");
            System.out.println("ID: " + rocket.getRocket_id());
            System.out.println("Название: " + rocket.getRocket_name());
            System.out.println("Тип: " + rocket.getRocket_type());
        } else {
            System.out.println("\n(Данные о ракете отсутствуют)");
        }

        // === Полезная нагрузка ===
        List<Payload> payloads = l.getPayloads();
        if (payloads != null && !payloads.isEmpty()) {
            System.out.println("\n--- Полезная нагрузка ---");
            for (int i = 0; i < payloads.size(); i++) {
                Payload p = payloads.get(i);
                System.out.printf("  #%d ID: %s | Страна: %s | Тип: %s | Орбита: %s%n",
                        i + 1,
                        p.getPayload_id(),
                        p.getNationality(),
                        p.getPayload_type(),
                        p.getOrbit());
            }
        } else {
            System.out.println("\n(Полезная нагрузка отсутствует)");
        }
    }



    private static void updateLaunch(Scanner scanner) {
        System.out.print("Введите номер запуска для обновления: ");
        int flightNumber = Integer.parseInt(scanner.nextLine());

        Optional<Launch> existingOpt = launchRepo.readByFlightNumber(flightNumber);
        if (existingOpt.isEmpty()) {
            System.out.println("Запуск с таким номером не найден. Сначала добавьте его.");
            return;
        }

        Launch existing = existingOpt.get();

        System.out.println("\n=== Обновление запуска #" + flightNumber + " ===");

        System.out.printf("Текущая дата запуска (UTC): %s%nВведите новую (или Enter, чтобы оставить): ", existing.getLaunch_date_utc());
        String launchDateUtc = scanner.nextLine();
        if (launchDateUtc.isBlank()) launchDateUtc = existing.getLaunch_date_utc();

        String currentSite = existing.getLaunch_site() != null ? existing.getLaunch_site().getSite_name() : "неизвестно";
        System.out.printf("Текущее место запуска: %s%nВведите новое (или Enter, чтобы оставить): ", currentSite);
        String newSiteName = scanner.nextLine();
        if (newSiteName.isBlank()) newSiteName = currentSite;

        LaunchSite launchSite = new LaunchSite("manual", newSiteName, newSiteName);

        System.out.printf("Текущее состояние (успешен: %s)%nИзменить? (y/n): ", existing.isLaunch_success() ? "Да" : "Нет");
        String successChoice = scanner.nextLine();
        boolean launchSuccess = existing.isLaunch_success();
        if (successChoice.equalsIgnoreCase("y")) {
            System.out.print("Был ли запуск успешным? (y/n): ");
            String newSuccess = scanner.nextLine();
            launchSuccess = newSuccess.equalsIgnoreCase("y");
        }

        // === Ракета ===
        Rocket rocket = existing.getRocket();
        if (rocket == null) rocket = new Rocket();

        System.out.println("\n--- Обновление данных ракеты ---");
        System.out.printf("Текущий ID ракеты: %s%nВведите новый (или Enter, чтобы оставить): ", rocket.getRocket_id());
        String rocketId = scanner.nextLine();
        if (!rocketId.isBlank()) rocket.setRocket_id(rocketId);

        System.out.printf("Текущее название ракеты: %s%nВведите новое (или Enter, чтобы оставить): ", rocket.getRocket_name());
        String rocketName = scanner.nextLine();
        if (!rocketName.isBlank()) rocket.setRocket_name(rocketName);

        System.out.printf("Текущий тип ракеты: %s%nВведите новый (или Enter, чтобы оставить): ", rocket.getRocket_type());
        String rocketType = scanner.nextLine();
        if (!rocketType.isBlank()) rocket.setRocket_type(rocketType);

        // === Полезная нагрузка ===
        List<Payload> payloads = existing.getPayloads() != null ? new ArrayList<>(existing.getPayloads()) : new ArrayList<>();
        System.out.println("\n--- Обновление полезных нагрузок ---");
        System.out.printf("Текущее количество нагрузок: %d%nИзменить? (y/n): ", payloads.size());
        String payloadChange = scanner.nextLine();

        if (payloadChange.equalsIgnoreCase("y")) {
            payloads.clear();
            System.out.print("Введите новое количество нагрузок: ");
            int payloadCount = Integer.parseInt(scanner.nextLine());

            for (int i = 0; i < payloadCount; i++) {
                System.out.println("\nНагрузка #" + (i + 1));
                System.out.print("Введите ID нагрузки: ");
                String payloadId = scanner.nextLine();

                System.out.print("Введите страну происхождения: ");
                String nationality = scanner.nextLine();

                System.out.print("Введите тип нагрузки: ");
                String payloadType = scanner.nextLine();

                System.out.print("Введите орбиту: ");
                String orbit = scanner.nextLine();

                payloads.add(new Payload(payloadId, nationality, payloadType, orbit));
            }
        }

        // === Сборка обновлённого объекта ===
        SecondStage secondStage = new SecondStage(1, payloads);
        rocket.setSecond_stage(secondStage);

        Launch updated = new Launch();
        updated.setFlight_number(flightNumber);
        updated.setLaunch_date_utc(launchDateUtc);
        updated.setLaunch_site(launchSite);
        updated.setLaunch_success(launchSuccess);
        updated.setRocket(rocket);

        launchRepo.updateLaunch(flightNumber, updated);
        System.out.println("Запуск успешно обновлён!");

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
            return;
        }

        System.out.println("\n=== Все запуски ===");

        for (Launch l : allLaunches) {
            System.out.println("\n----------------------------------------");
            System.out.println("Номер запуска: " + l.getFlight_number());
            System.out.println("Дата (UTC): " + l.getLaunch_date_utc());
            System.out.println("Место запуска: " +
                    (l.getLaunch_site() != null ? l.getLaunch_site().getSite_name() : "неизвестно"));
            System.out.println("Успешен: " + (l.isLaunch_success() ? "Да" : "Нет"));

            // === Ракета ===
            Rocket rocket = l.getRocket();
            if (rocket != null) {
                System.out.println("\n--- Ракета ---");
                System.out.println("ID: " + rocket.getRocket_id());
                System.out.println("Название: " + rocket.getRocket_name());
                System.out.println("Тип: " + rocket.getRocket_type());
            } else {
                System.out.println("\n(Данные о ракете отсутствуют)");
            }

            // === Полезная нагрузка ===
            List<Payload> payloads = l.getPayloads();
            if (payloads != null && !payloads.isEmpty()) {
                System.out.println("\n--- Полезная нагрузка ---");
                for (int i = 0; i < payloads.size(); i++) {
                    Payload p = payloads.get(i);
                    System.out.printf("  #%d ID: %s | Страна: %s | Тип: %s | Орбита: %s%n",
                            i + 1,
                            p.getPayload_id(),
                            p.getNationality(),
                            p.getPayload_type(),
                            p.getOrbit());
                }
            } else {
                System.out.println("\n(Полезная нагрузка отсутствует)");
            }
        }

        System.out.println("\n----------------------------------------");
        System.out.println("Всего запусков: " + allLaunches.size());
    }


}
