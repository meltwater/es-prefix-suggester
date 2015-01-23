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

import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.search.suggest.SuggestContextParser;
import org.elasticsearch.search.suggest.SuggestUtils;

import java.io.IOException;

public class PrefixContextParser implements SuggestContextParser {

    private PrefixSuggester suggester;
    public static final String MIN_PREFIX_LENGTH_FIELD = "min_prefix_length";

    public PrefixContextParser(PrefixSuggester suggester) {
        this.suggester = suggester;
    }

    public PrefixSuggestionContext parse(XContentParser parser, MapperService mapperService) throws IOException {
        XContentParser.Token token;
        String fieldName = null;
        PrefixSuggestionContext suggestion = new PrefixSuggestionContext(suggester);
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                fieldName = parser.currentName();
            } else if (token.isValue()) {
                parseTokenValue(parser, mapperService, fieldName, suggestion);
            } else {
                throw new ElasticsearchIllegalArgumentException("suggester[term]  doesn't support field [" + fieldName + "]");
            }
        }
        return suggestion;
    }

    private void parseTokenValue(XContentParser parser, MapperService mapperService, String fieldName, PrefixSuggestionContext suggestion) throws IOException {
        if (MIN_PREFIX_LENGTH_FIELD.equals(fieldName)) {
            suggestion.setMinPrefixLength(parser.intValue());

        } else if (!(SuggestUtils.parseSuggestContext(parser, mapperService, fieldName, suggestion))) {
            throw new ElasticsearchIllegalArgumentException("suggester[term] doesn't support [" + fieldName + "]");

        }
    }
}
