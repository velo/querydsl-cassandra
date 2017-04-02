/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marvinformatics.querydsl;

import static com.google.common.collect.ImmutableSet.copyOf;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;

/**
 * Defines reserved keywords for the supported dialects
 */
final class Keywords {

    private Keywords() { }

    private static Set<String> readLines(String path) {
        try {
            return copyOf(Resources.readLines(
                    Keywords.class.getResource("/keywords/" + path), Charsets.UTF_8,
                    new CommentDiscardingLineProcessor()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Set<String> CQL = readLines("cql");

    private static class CommentDiscardingLineProcessor implements LineProcessor<Collection<String>> {

        private final Collection<String> result = Sets.newHashSet();

        @Override
        public boolean processLine(String line) throws IOException {
            if (!line.isEmpty() && !line.startsWith("#")) {
                result.add(line);
            }
            return true;
        }

        @Override
        public Collection<String> getResult() {
            return result;
        }

    }
}
