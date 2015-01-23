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

import org.elasticsearch.search.suggest.Suggester;
import org.elasticsearch.search.suggest.SuggestionSearchContext;

public class PrefixSuggestionContext extends SuggestionSearchContext.SuggestionContext{

    private int minPrefixLength = 3;

    public PrefixSuggestionContext(Suggester suggester) {
        super(suggester);
    }

    public int getMinPrefixLength() {
        return minPrefixLength;
    }

    public void setMinPrefixLength(int minPrefixLength) {
        this.minPrefixLength = minPrefixLength;
    }
}
