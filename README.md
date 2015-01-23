# Prefix Suggester

This plugin for [Elasticsearch](http://www.elasticsearch.org/) providers an additional suggester, based on prefixes. 
For learning how to use suggesters in general, see the official 
[Suggester Documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-suggesters.html).

For many ES users, the official [completion suggester](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-suggesters-completion.html)
is probably a better fit. It does however that you index your data in a specific way. This plugin does not, so it might
be useful if changing the index structure is hard for you, regardless if it's because of data control, index size or 
disk usage.

This plugin will give suggestions for terms in the index which starts with a prefix, which is input as the `text` 
parameter. The input text is analysed (as other suggest modules, defaulting to the default_search analyser for the field), 
so e.g. "Oba" would suggest "obama" if your analysis includes lower-casing. If the input text is analysed to more than
one token, ***no suggestions will be returned***. For example, "The Pres" would return empty, even if the tokens "the"
and "president" occurred next to each other in an indexed document. This is because of performance reasons, which the 
completion suggester avoids by having extra index structures. 

The returned tokens follow the same structure as other suggesters, and are sorted according to the Document Frequency
(how many documents contained the term), the most common first. By default, it will not try to expand the term if it is 
shorter than 3 characters, in order to prevent very expensive requests. You can change this using the `min_prefix_length`
parameter. This can be useful if you know that there are very few terms in a field (e.g. a field listing all countries),
in which case it is safe to lower the value.

Parameters:

 - ***text***: The word to use as a prefix.
 - ***field***: The field to use when looking for terms.
 - ***min_prefix_length***: The minimum numbers of characters in the prefix.

Example usage:

    curl localhost:9200/_suggest -d '{"my-suggestion":{"text":"Obam","prefix":{"min_prefix_length":2, "field":"text"}}}'
Example response:
```
    {
      "_shards": {
        "total": 2,
        "successful": 2,
        "failed": 0
      },
      "my-suggestion": [
        {
          "text": "Obam",
          "offset": 0,
          "length": 4,
          "options": [
            {
              "text": "obama",
              "score": 1778
            },
            {
              "text": "obamacare",
              "score": 852
            }
          ]
        }
      ]
    }
```

# License

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
