package de.softlang.swap.task;

import de.softlang.swap.Utils;
import de.softlang.swap.dataclass.NER;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.util.CoreMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.spark.sql.SparkSession;

public class StanfordNERTask extends StanfordTask {

    public StanfordNERTask() {
        super("stanfordNER");
    }

    @Override
    public void execute(Map<String, String> environment, SparkSession session) {
        Map<Annotation, String> pages = prepare(environment, session);
        Set<NER> ners = new HashSet<>();

        Properties props = new Properties();
        props.setProperty("annotators", "ner");
        int threads = Integer.parseInt(environment.get("threads"));
        StanfordCoreNLPClient stanfordClient = new StanfordCoreNLPClient(props, environment.get("host"), Integer.parseInt(environment.get("port")), threads);

        annotate(stanfordClient, pages.keySet(), a -> get(pages, ners, a));

        Utils.saveAsJson(ners, Utils.getPath(environment, "NERFile"));
    }

    private void get(Map<Annotation, String> pages, Set<NER> ners, Annotation annotion) {
        Set<String> organisations = new HashSet<>();
        Set<String> persons = new HashSet<>();
        List<CoreMap> get = annotion.get(CoreAnnotations.MentionsAnnotation.class);
        for (CoreMap token : get) {
            String type = token.get(CoreAnnotations.EntityTypeAnnotation.class);
            String text = token.get(CoreAnnotations.TextAnnotation.class);
            if (type.equals("ORGANIZATION")) {
                organisations.add(text);
            } else if (type.equals("PERSON")) {
                persons.add(text);
            }
        }
        ners.add(new NER(pages.remove(annotion), organisations, persons));
    }
}
