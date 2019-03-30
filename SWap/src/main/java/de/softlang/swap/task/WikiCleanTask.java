package de.softlang.swap.task;

import de.softlang.swap.Utils;
import de.softlang.swap.WikiClean;
import de.softlang.swap.dataclass.Page;
import java.util.Map;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class WikiCleanTask extends BaseTask {

    public WikiCleanTask() {
        super("wikiClean");
    }

    @Override
    public void execute(Map<String, String> environment, SparkSession session) {
        Dataset<Row> data = Utils.read(session, environment, "pageFile");
        WikiClean wikiClean = new WikiClean(false, false);

        Dataset<Page> map = data.map((MapFunction<Row, Page>) (row -> new Page(row.<String>getAs("page_title"), wikiClean.clean(row.<String>getAs("text")))),
                Encoders.bean(Page.class))
                .filter((FilterFunction<Page>) (page -> !page.getText().isEmpty()));

        Utils.saveAsCsv(map, Utils.getPath(environment, "cleanPageFile"));
    }

}
