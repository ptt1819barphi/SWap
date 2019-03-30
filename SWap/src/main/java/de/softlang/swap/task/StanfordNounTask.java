package de.softlang.swap.task;

import de.softlang.swap.Utils;
import de.softlang.swap.dataclass.Noun;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.spark.sql.SparkSession;

public class StanfordNounTask extends StanfordTask {

    public StanfordNounTask() {
        super("stanfordNoun");
    }

    @Override
    public void execute(Map<String, String> environment, SparkSession session) {
        Map<Annotation, String> pages = prepare(environment, session);
        Set<Noun> nouns = new HashSet<>();

        Properties props = new Properties();
        props.setProperty("annotators", "lemma");

        int threads = Integer.parseInt(environment.get("threads"));
        StanfordCoreNLPClient stanfordClient = new StanfordCoreNLPClient(props, environment.get("host"), Integer.parseInt(environment.get("port")), threads);

        annotate(stanfordClient, pages.keySet(), t -> get(pages, nouns, t));

        Utils.saveAsJson(nouns, Utils.getPath(environment, "nounFile"));
    }

    private void get(Map<Annotation, String> pages, Set<Noun> n, Annotation annotation) {
        Map<String, Integer> nouns = new HashMap<>();
        List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
        if (tokens != null) {
            for (CoreLabel token : tokens) {
                if (token == null || !token.tag().startsWith("NN") || token.lemma().startsWith("http://") || token.lemma().startsWith("https://")) {
                    continue;
                }

                nouns.putIfAbsent(token.lemma(), 0);
                nouns.compute(token.lemma(), (key, value) -> value + 1);
            }

        }
        n.add(new Noun(pages.remove(annotation), nouns));
    }

}
