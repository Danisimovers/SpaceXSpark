package sfedu.danil.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.SparkSession;

import java.util.Optional;

public class SparkConfig {

    private static final Logger logger = LogManager.getLogger(SparkConfig.class);

    private static Optional<SparkSession> sparkSession = Optional.empty();

    public static SparkSession getSparkSession() {
        if (sparkSession.isEmpty()) {
            logger.info("Инициализация SparkSession...");

            SparkSession spark = SparkSession.builder()
                    .appName("SpaceX-Spark-App")
                    .master("local[*]")
                    .config("spark.sql.shuffle.partitions", "4")
                    .getOrCreate();

            logger.info("SparkSession успешно создан!");
            logger.info("Имя приложения: {}", spark.sparkContext().appName());
            logger.info("Режим выполнения (master): {}", spark.sparkContext().master());
            logger.info("Версия Spark: {}", spark.version());

            sparkSession = Optional.of(spark);
        }

        return sparkSession.get();
    }

    public static void stopSparkSession() {
        sparkSession.ifPresent(spark -> {
            spark.stop();
            logger.info("SparkSession остановлен.");
            sparkSession = Optional.empty();
        });
    }
}
