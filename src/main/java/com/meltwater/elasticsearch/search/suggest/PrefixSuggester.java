/*
Copyright 2015 Meltwater Group

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */


package com.meltwater.elasticsearch.search.suggest;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.search.PrefixTermsEnum;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.CharsRefBuilder;
import org.apache.lucene.util.PriorityQueue;
import org.elasticsearch.common.text.StringText;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestContextParser;
import org.elasticsearch.search.suggest.SuggestUtils;
import org.elasticsearch.search.suggest.Suggester;
import org.elasticsearch.search.suggest.SuggestionSearchContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrefixSuggester extends Suggester<PrefixSuggestionContext> {
    @Override

    protected Suggest.Suggestion<Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option>> innerExecute(String name, PrefixSuggestionContext suggestion, IndexReader indexReader, CharsRefBuilder spare) throws IOException {
        List<Term> analyzed = queryTerms(suggestion, spare);

        //If there is more than one parameter, return it. We can't do accurate phrase suggestions cheaply, so we don't.
        if(analyzed.size() != 1){
            return null;
        }

        Term text = analyzed.get(0);
        //E.g. "a" or "" (empty string) would list more or less all terms in the dictionary. Don't wanna do that.
        if(text.bytes().length < suggestion.getMinPrefixLength()){
            return null;
        }
        PrefixTermsEnum prefixTermsEnum = terms(indexReader, text);
        //E.g. there is no field or no prefix like this
        if (prefixTermsEnum == null)
            return null;
        final int size = suggestion.getSize();
        return singleEntrySuggestion(name, size, getOptions(text, prefixTermsEnum, indexReader, size));
    }

    private Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option> getOptions(Term text, PrefixTermsEnum prefixTermsEnum, IndexReader indexReader, final int size) throws IOException {
        OptionQueue collectionQueue =  new OptionQueue(size);
        BytesRef ref;
        while ((ref = prefixTermsEnum.next()) != null) {
            Term term = new Term(text.field(), BytesRef.deepCopyOf(ref));
            collectionQueue.insertWithOverflow(new Suggest.Suggestion.Entry.Option(new StringText(term.bytes().utf8ToString()), indexReader.docFreq(term)));
        }

        Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option> entry = new Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option>(new StringText(text.text()),0, text.bytes().length );
        while(collectionQueue.size() > 0){
            entry.addOption(collectionQueue.pop());
        }
        return entry;
    }

    private static class OptionQueue extends PriorityQueue<Suggest.Suggestion.Entry.Option> {

        public OptionQueue(int maxSize) {
            super(maxSize);
        }

        @Override
        protected boolean lessThan(Suggest.Suggestion.Entry.Option a, Suggest.Suggestion.Entry.Option b) {
            return a.getScore() < b.getScore();
        }
    }

    private PrefixTermsEnum terms(IndexReader indexReader, Term text) throws IOException {
        Terms terms = MultiFields.getTerms(indexReader, text.field());
        if(terms == null){
            return null;
        }
        return new PrefixTermsEnum(
                terms.iterator(null),
                text.bytes());
    }

    @Override
    public String[] names() {
        return new String[]{"prefix"};
    }

    @Override
    public SuggestContextParser getContextParser() {
        return new PrefixContextParser(this);
    }

    private List<Term> queryTerms(SuggestionSearchContext.SuggestionContext suggestion, CharsRefBuilder spare) throws IOException {
        final String field = suggestion.getField();
        final List<Term> ret = new ArrayList<Term>();
        SuggestUtils.analyze(suggestion.getAnalyzer(), suggestion.getText(), field, new SuggestUtils.TokenConsumer() {
            @Override
            public void nextToken() {
                ret.add(new Term(field, BytesRef.deepCopyOf(fillBytesRef(new BytesRefBuilder()))));
            }
        }, spare);
        return ret;
    }

    private Suggest.Suggestion<Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option>> singleEntrySuggestion(String name, int size, Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option> entry) {
        Suggest.Suggestion<Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option>> o =  new Suggest.Suggestion<Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option>>(name, size);
        o.addTerm(entry);
        return o;
    }
}
