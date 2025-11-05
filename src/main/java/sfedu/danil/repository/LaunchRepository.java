package sfedu.danil.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.*;
import sfedu.danil.config.SparkConfig;
import sfedu.danil.model.Launch;
import sfedu.danil.model.Rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LaunchRepository implements LaunchRepositoryInterface {

    private static final Logger logger = LogManager.getLogger(LaunchRepository.class);

    private final SparkSession spark;
    private Dataset<Launch> launchDS;
    private Dataset<Rocket> rocketDS;

    public LaunchRepository() {
        this.spark = SparkConfig.getSparkSession();
        this.launchDS = spark.createDataset(List.of(), Encoders.bean(Launch.class));
        this.rocketDS = spark.createDataset(List.of(), Encoders.bean(Rocket.class));
        logger.info("LaunchRepository инициализирован. Пустой Dataset создан.");
    }

    @Override
    public void saveAll(List<Launch> launches) {
        logger.info("Сохраняем список запусков, размер: {}", launches.size());
        Dataset<Launch> newDS = spark.createDataset(launches, Encoders.bean(Launch.class));
        launchDS = launchDS.union(newDS);
        logger.info("Список запусков добавлен. Текущее количество запусков: {}", launchDS.count());
    }

    @Override
    public void saveByFlightNumber(Launch launch) {
        logger.info("Сохраняем запуск с flight_number: {}", launch.getFlight_number());
        if (existsByFlightNumber(launch.getFlight_number())) {
            logger.info("Запуск с flight_number {} уже существует. Обновляем его.", launch.getFlight_number());
            updateLaunch(launch.getFlight_number(), launch);
        } else {
            Dataset<Launch> newDS = spark.createDataset(List.of(launch), Encoders.bean(Launch.class));
            launchDS = launchDS.union(newDS);
            logger.info("Запуск добавлен. Текущее количество запусков: {}", launchDS.count());
        }
    }

    @Override
    public Optional<Launch> readByFlightNumber(int flightNumber) {
        logger.info("Читаем запуск с flight_number: {}", flightNumber);
        List<Launch> resultList = new ArrayList<>();
        List<Launch> allLaunches = launchDS.collectAsList();
        for (Launch l : allLaunches) {
            if (l.getFlight_number() == flightNumber) {
                resultList.add(l);
            }
        }
        if (resultList.isEmpty()) {
            logger.warn("Запуск с flight_number {} не найден", flightNumber);
            return Optional.empty();
        } else {
            logger.info("Запуск с flight_number {} найден", flightNumber);
            return Optional.of(resultList.get(0));
        }
    }

    @Override
    public List<Launch> readAll() {
        logger.info("Читаем все запуски. Всего: {}", launchDS.count());
        return launchDS.collectAsList();
    }

    @Override
    public boolean existsByFlightNumber(int flightNumber) {
        logger.info("Проверяем существование запуска с flight_number: {}", flightNumber);
        boolean exists = false;
        for (Launch l : launchDS.collectAsList()) {
            if (l.getFlight_number() == flightNumber) {
                exists = true;
                break;
            }
        }
        logger.info("Запуск с flight_number {} существует: {}", flightNumber, exists);
        return exists;
    }

    @Override
    public boolean existsByRocketNumber(String rocketNumber) {
        logger.info("Проверяем существование запуска с rocket_id: {}", rocketNumber);
        boolean exists = false;
        for (Rocket r : rocketDS.collectAsList()) {
            if (r.getRocket_id() == rocketNumber) {
                exists = true;
                break;
            }
        }
        logger.info("Запуск с rocket_id {} существует: {}", rocketNumber, exists);
        return exists;
    }

    @Override
    public void updateLaunch(int flightNumber, Launch updatedLaunch) {
        logger.info("Обновляем запуск с flight_number: {}", flightNumber);
        List<Launch> updatedList = new ArrayList<>();
        for (Launch l : launchDS.collectAsList()) {
            if (l.getFlight_number() == flightNumber) {
                updatedList.add(updatedLaunch);
                logger.info("Запуск с flight_number {} обновлен", flightNumber);
            } else {
                updatedList.add(l);
            }
        }
        launchDS = spark.createDataset(updatedList, Encoders.bean(Launch.class));
    }

    @Override
    public void deleteLaunch(int flightNumber) {
        logger.info("Удаляем запуск с flight_number: {}", flightNumber);
        List<Launch> remaining = new ArrayList<>();
        for (Launch l : launchDS.collectAsList()) {
            if (l.getFlight_number() != flightNumber) {
                remaining.add(l);
            }
        }
        launchDS = spark.createDataset(remaining, Encoders.bean(Launch.class));
        logger.info("Удаление выполнено. Текущее количество запусков: {}", launchDS.count());
    }

    @Override
    public void deleteAll() {
        logger.info("Удаляем все запуски");
        launchDS = spark.createDataset(List.of(), Encoders.bean(Launch.class));
        logger.info("Все запуски удалены. Текущее количество: {}", launchDS.count());
    }

    @Override
    public void exportToCsv() {
        String path = System.getProperty("user.dir") + "/data/launches_csv";

        try {
            long count = launchDS.count();
            if (count == 0) {
                logger.warn("launchDS пуст — нечего сохранять.");
                System.out.println("Нет данных для экспорта.");
                return;
            }

            java.io.File dir = new java.io.File(path);
            if (!dir.exists()) dir.mkdirs();

            Dataset<Row> df = launchDS.toDF();

            df = df
                    // из Rocket
                    .withColumn("rocket_id", functions.col("rocket.rocket_id"))
                    .withColumn("rocket_name", functions.col("rocket.rocket_name"))
                    .withColumn("rocket_type", functions.col("rocket.rocket_type"))
                    .withColumn("second_stage_block", functions.col("rocket.second_stage.block"))
                    .withColumn("payloads_str", functions.expr("CAST(rocket.second_stage.payloads AS STRING)"))
                    .drop("rocket")
                    .drop("payloads")
                    .drop("launch_site")
            ;

            df.coalesce(1)
                    .write()
                    .mode(SaveMode.Overwrite)
                    .option("header", "true")
                    .csv(path);

            logger.info("CSV сохранён по пути: {}", path);
            System.out.println("Данные сохранены в CSV (папка " + path + ")");

        } catch (Exception e) {
            logger.error("Ошибка при сохранении CSV: {}", e.getMessage(), e);
            System.out.println("Ошибка при сохранении CSV: " + e.getMessage());
        }
    }




}
