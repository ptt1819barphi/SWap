package de.softlang.swap.task;

import de.softlang.swap.Utils;
import de.softlang.swap.dataclass.Page;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SummaryExtractionTask extends BaseTask {

    public SummaryExtractionTask() {
        super("summaryExtract");
    }

    @Override
    public void execute(Map<String, String> environment, SparkSession session) {
        Dataset<Row> data = Utils.read(session, environment, "pageFile");
        Pattern pattern = Pattern.compile("^==", Pattern.MULTILINE);

        Dataset<Page> map = data.map((MapFunction<Row, Page>) (row -> {
            String text = row.<String>getAs("text");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                text = text.substring(0, matcher.start());
            }
            return new Page(row.<String>getAs("page_title"), text);
        }),
                Encoders.bean(Page.class))
                .filter((FilterFunction<Page>) (page -> !page.getText().isEmpty()));

        Utils.saveAsCsv(map, Utils.getPath(environment, "summaryFile"));
    }
}
