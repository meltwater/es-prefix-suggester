package com.meltwater.elasticsearch.search.suggest;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.hamcrest.CoreMatchers;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThat;

public class PrefixSuggesterTest {

    private static String BASE_DIR = "test-temp";
    private static String TEST_INDEX = "test";
    private static String FIELD = "test";
    private static Map<String, String> settings = new HashMap<String, String>();
    static {
        settings.put("node.master", "true");
        settings.put("node.data", "true");
        settings.put("node.http.enabled", "false");
        settings.put("index.gateway.type", "none");
        settings.put("index.store.type", "memory");
        settings.put("index.number_of_shards", "1");
        settings.put("index.number_of_replicas", "0");
        settings.put("node.local", "true");
        settings.put("path.data", BASE_DIR + "/es/data");
        settings.put("path.logs", BASE_DIR + "es/logs");
        settings.put("path.work", BASE_DIR + "/es/work");
    }
    private static Node node;

    @BeforeClass
    public static void setup(){
        node = NodeBuilder.nodeBuilder().settings(ImmutableSettings.builder().put(settings).build()).build().start();
        node.client().prepareIndex(TEST_INDEX, "type").setSource(FIELD, "Someone from The Office called.").execute().actionGet();
        node.client().admin().indices().prepareRefresh().execute().actionGet();
    }

    @AfterClass
    public static void tearDown(){
        node.close();
        delete(new File(BASE_DIR));
    }

    private static void delete(File file){
        if(file.isDirectory()){
            for(File sub:file.listFiles()){
                delete(sub);
            }
        }
        file.delete();
    }

    @Test
    public void shouldSuggestRegularTerm(){
        assertThat(getSuggestions("offi"), CoreMatchers.hasItem("office"));
    }

    @Test
    public void shouldApplyAnalyzer(){
        assertThat(getSuggestions("Offic", "simple"), CoreMatchers.hasItem("office"));
    }

    @Test
    public void shouldNotReturnSuggestionsForMultipleTerms(){
        assertThat(getSuggestions("The Offic", "simple"), new EmptyMatcher<List<String>>());
    }

    @Test
    public void shouldNotSuggestAnyForTooGeneral(){
        assertThat(getSuggestions("of"), new EmptyMatcher<List<String>>());
    }

    @Test
    public void shouldRespectMinLength(){
        assertThat(getSuggestions("of", 2), CoreMatchers.hasItem("office"));
    }


    private List<String> getSuggestions(String input) {
        return getSuggestions(new PrefixSuggestionBuilder("sugg").text(input));
    }

    private List<String> getSuggestions(String input, int minLength) {
        return getSuggestions(new PrefixSuggestionBuilder("sugg").minPrefixLength(minLength).text(input));
    }

    private List<String> getSuggestions(String input, String analyzer){
        return getSuggestions(new PrefixSuggestionBuilder("sugg").text(input).analyzer(analyzer));
    }

    private List<String> getSuggestions(PrefixSuggestionBuilder sugg) {
        Suggest suggest = node.client().prepareSuggest(TEST_INDEX).addSuggestion(sugg.field(FIELD)).execute().actionGet().getSuggest();
        List<String> found= new ArrayList<String>();
        for(Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion:suggest){
            for(Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry:suggestion){
                for(Suggest.Suggestion.Entry.Option option:entry){
                    found.add(option.getText().string());
                }
            }
        }
        return found;
    }

    private static class EmptyMatcher<T extends Collection> extends CustomTypeSafeMatcher<T>{

        public EmptyMatcher() {
            super("Expected collection to be empty.");
        }

        @Override
        protected boolean matchesSafely(T item) {
            return item.isEmpty();
        }
    }


}
