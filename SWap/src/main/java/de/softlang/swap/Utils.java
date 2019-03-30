package de.softlang.swap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

public class Utils {

    public static String getPath(Map<String, String> environment, String name) {
        if (name.startsWith("/") || name.indexOf(":") == 1) {
            return environment.get(name);
        }

        String workspace = environment.get("workspace");
        if (!workspace.endsWith("/")) {
            workspace += "/";
        }

        return workspace + environment.get(name);
    }

    public static void saveAsCsv(Dataset set, String path) {
        set.coalesce(1).write()
                .mode(SaveMode.Overwrite)
                .option("header", true)
                .option("multiline", true)
                .option("quoteAll", true)
                .option("escape", "\"")
                .csv(path);

        File file = new File(path);

        File[] listFiles = file.listFiles();

        if (listFiles.length != 4) {
            throw new IllegalStateException();
        }

        File csv = null;

        for (File listFile : listFiles) {
            if (listFile.getName().endsWith(".csv")) {
                csv = listFile;
            } else {
                listFile.delete();
            }
        }

        try {
            File move = new File(file.getParentFile(), csv.getName());
            csv.renameTo(move);
            file.delete();
            Files.move(move.toPath(), new File(path).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void saveAsJson(Object object, String path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter fw = new FileWriter(path)) {

            gson.toJson(object, fw);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Dataset<Row> read(SparkSession session, Map<String, String> environment, String key) {
        return session.read()
                .option("header", true)
                .option("multiline", true)
                .option("escape", "\"")
                .csv(Utils.getPath(environment, key));
    }

}
