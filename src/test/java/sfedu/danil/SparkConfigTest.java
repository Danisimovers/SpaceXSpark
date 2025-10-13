package sfedu.danil;

import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import sfedu.danil.config.SparkConfig;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SparkConfigTest {

    @Test
    public void testSparkSessionLifecycle() throws NoSuchFieldException, IllegalAccessException {
        // Получаем SparkSession
        SparkSession spark = SparkConfig.getSparkSession();
        assertNotNull(spark, "SparkSession должен быть создан");

        assertEquals("SpaceX-Spark-App", spark.sparkContext().appName());

        SparkConfig.stopSparkSession();

        Field field = SparkConfig.class.getDeclaredField("sparkSession");
        field.setAccessible(true);
        Optional<SparkSession> optional = (Optional<SparkSession>) field.get(null);
        assertTrue(optional.isEmpty(), "SparkSession Optional должен быть пуст после остановки");
    }
}
