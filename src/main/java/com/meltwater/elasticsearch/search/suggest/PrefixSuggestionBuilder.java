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

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;

import java.io.IOException;

public class PrefixSuggestionBuilder extends SuggestBuilder.SuggestionBuilder<PrefixSuggestionBuilder>{

    private Integer minLength = null;

    public PrefixSuggestionBuilder(String name) {
        super(name, "prefix");
    }

    @Override
    protected XContentBuilder innerToXContent(XContentBuilder builder, Params params) throws IOException {
        if(minLength != null){
            builder.field(PrefixContextParser.MIN_PREFIX_LENGTH_FIELD, minLength);
        }
        return builder;
    }

    public PrefixSuggestionBuilder minPrefixLength(int length){
        this.minLength = length;
        return this;
    }

}
