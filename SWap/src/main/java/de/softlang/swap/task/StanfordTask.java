package de.softlang.swap.task;

import de.softlang.swap.Utils;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public abstract class StanfordTask extends BaseTask {

    private final Semaphore semaphore = new Semaphore(0);

    public StanfordTask(String name) {
        super(name);
    }

    protected Map<Annotation, String> prepare(Map<String, String> environment, SparkSession session) {
        Dataset<Row> data = Utils.read(session, environment, "pageFile");

        Map<Annotation, String> pages = new HashMap<>();
        List<Row> collectAsList = data.collectAsList();
        for (Row row : collectAsList) {
            pages.put(new NAnnotation(row.<String>getAs("text")), row.getAs("page_title"));
        }

        return pages;
    }

    protected void annotate(StanfordCoreNLPClient client, Collection<Annotation> annotations, Consumer<Annotation> consumer) {
        client.annotate(annotations, a -> next(a, consumer));
        semaphore.acquireUninterruptibly(annotations.size());
    }

    private synchronized void next(Annotation annotation, Consumer<Annotation> consumer) {
        consumer.accept(annotation);
        semaphore.release();
    }

    static class NAnnotation extends Annotation {

        private final int hash;

        public NAnnotation(String text) {
            super(text);
            hash = text.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
