package de.softlang.swap.task;

import de.softlang.swap.Utils;
import de.softlang.swap.dataclass.Depth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class CategoryDepthTask extends BaseTask {

    public CategoryDepthTask() {
        super("depth");
    }

    @Override
    public void execute(Map<String, String> environment, SparkSession session) {
        Dataset<Row> data = session.read()
                .option("header", true)
                .option("multiline", true)
                .option("escape", "\"")
                .csv(Utils.getPath(environment, "categoriesFile"));

        List<Depth> calcDepth = calcDepth(environment.get("startCategory"), data);
        Dataset<Depth> depthData = session.createDataset(calcDepth, Encoders.bean(Depth.class));
        Utils.saveAsCsv(depthData.toDF("category", "depth"), Utils.getPath(environment, "depthFile"));
    }

    private static List<Depth> calcDepth(String startCategory, Dataset<Row> data) {
        if (data.columns().length != 2 || !data.columns()[0].equals("category")
                || !data.columns()[1].equals("subcategory")) {
            throw new IllegalArgumentException("Incompatible data.");
        }

        List<Depth> depthList = new ArrayList<>();
        depthList.add(new Depth(startCategory, 0));
        Set<String> categories = Set.of(startCategory);
        calcDepth(depthList, categories, data, 1);

        return depthList;
    }

    private static void calcDepth(List<Depth> depthList, Set<String> categories, Dataset<Row> data, int depth) {
        if (categories.isEmpty()) {
            return;
        }

        List<String> list = data
                .filter((FilterFunction<Row>) (row -> categories.contains(row.<String>getAs("category"))
                && !depthList.stream().anyMatch(l -> l.getCategory().equals(row.<String>getAs("subcategory")))))
                .map((MapFunction<Row, String>) (row -> row.getAs("subcategory")), Encoders.STRING())
                .collectAsList();

        Set<String> newCategories = new HashSet<>(list);
        for (String newCategory : newCategories) {
            depthList.add(new Depth(newCategory, depth));
        }
        calcDepth(depthList, newCategories, data, depth + 1);
    }
}
