/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.index.query;

import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.opensearch.common.lucene.search.Queries;
import org.opensearch.common.unit.Fuzziness;
import org.opensearch.common.xcontent.LoggingDeprecationHandler;
import org.opensearch.core.ParseField;
import org.opensearch.core.common.ParsingException;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.index.mapper.NumberFieldMapper;
import org.opensearch.index.query.support.QueryParsers;
import org.opensearch.index.search.MatchQuery;
import org.opensearch.index.search.MatchQuery.ZeroTermsQuery;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Match query is a query that analyzes the text and constructs a query as the
 * result of the analysis.
 *
 * @opensearch.internal
 */
public class MatchQueryBuilder extends AbstractQueryBuilder<MatchQueryBuilder> implements ComplementAwareQueryBuilder, WithFieldName {

    private static final String CUTOFF_FREQUENCY_DEPRECATION_MSG = "you can omit this option, "
        + "the [match] query can skip block of documents efficiently if the total number of hits is not tracked";

    public static final ParseField ZERO_TERMS_QUERY_FIELD = new ParseField("zero_terms_query");
    /**
     * @deprecated Since max_optimization optimization landed in 7.0, normal MatchQuery
     *             will achieve the same result without any configuration.
     */
    @Deprecated
    public static final ParseField CUTOFF_FREQUENCY_FIELD = new ParseField("cutoff_frequency").withAllDeprecated(
        CUTOFF_FREQUENCY_DEPRECATION_MSG
    );
    public static final ParseField LENIENT_FIELD = new ParseField("lenient");
    public static final ParseField FUZZY_TRANSPOSITIONS_FIELD = new ParseField("fuzzy_transpositions");
    public static final ParseField FUZZY_REWRITE_FIELD = new ParseField("fuzzy_rewrite");
    public static final ParseField MINIMUM_SHOULD_MATCH_FIELD = new ParseField("minimum_should_match");
    public static final ParseField OPERATOR_FIELD = new ParseField("operator");
    public static final ParseField MAX_EXPANSIONS_FIELD = new ParseField("max_expansions");
    public static final ParseField PREFIX_LENGTH_FIELD = new ParseField("prefix_length");
    public static final ParseField ANALYZER_FIELD = new ParseField("analyzer");
    public static final ParseField QUERY_FIELD = new ParseField("query");
    public static final ParseField GENERATE_SYNONYMS_PHRASE_QUERY = new ParseField("auto_generate_synonyms_phrase_query");

    /** The name for the match query */
    public static final String NAME = "match";

    /** The default mode terms are combined in a match query */
    public static final Operator DEFAULT_OPERATOR = Operator.OR;

    private final String fieldName;

    private final Object value;

    private Operator operator = DEFAULT_OPERATOR;

    private String analyzer;

    private Fuzziness fuzziness = null;

    private int prefixLength = FuzzyQuery.defaultPrefixLength;

    private int maxExpansions = FuzzyQuery.defaultMaxExpansions;

    private boolean fuzzyTranspositions = FuzzyQuery.defaultTranspositions;

    private String minimumShouldMatch;

    private String fuzzyRewrite = null;

    private boolean lenient = MatchQuery.DEFAULT_LENIENCY;

    private MatchQuery.ZeroTermsQuery zeroTermsQuery = MatchQuery.DEFAULT_ZERO_TERMS_QUERY;

    private Float cutoffFrequency = null;

    private boolean autoGenerateSynonymsPhraseQuery = true;

    /**
     * Constructs a new match query.
     */
    public MatchQueryBuilder(String fieldName, Object value) {
        if (fieldName == null) {
            throw new IllegalArgumentException("[" + NAME + "] requires fieldName");
        }
        if (value == null) {
            throw new IllegalArgumentException("[" + NAME + "] requires query value");
        }
        this.fieldName = fieldName;
        this.value = value;
    }

    /**
     * Read from a stream.
     */
    public MatchQueryBuilder(StreamInput in) throws IOException {
        super(in);
        fieldName = in.readString();
        value = in.readGenericValue();
        operator = Operator.readFromStream(in);
        prefixLength = in.readVInt();
        maxExpansions = in.readVInt();
        fuzzyTranspositions = in.readBoolean();
        lenient = in.readBoolean();
        zeroTermsQuery = MatchQuery.ZeroTermsQuery.readFromStream(in);
        // optional fields
        analyzer = in.readOptionalString();
        minimumShouldMatch = in.readOptionalString();
        fuzzyRewrite = in.readOptionalString();
        fuzziness = in.readOptionalWriteable(Fuzziness::new);
        cutoffFrequency = in.readOptionalFloat();
        autoGenerateSynonymsPhraseQuery = in.readBoolean();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(fieldName);
        out.writeGenericValue(value);
        operator.writeTo(out);
        out.writeVInt(prefixLength);
        out.writeVInt(maxExpansions);
        out.writeBoolean(fuzzyTranspositions);
        out.writeBoolean(lenient);
        zeroTermsQuery.writeTo(out);
        // optional fields
        out.writeOptionalString(analyzer);
        out.writeOptionalString(minimumShouldMatch);
        out.writeOptionalString(fuzzyRewrite);
        out.writeOptionalWriteable(fuzziness);
        out.writeOptionalFloat(cutoffFrequency);
        out.writeBoolean(autoGenerateSynonymsPhraseQuery);
    }

    /** Returns the field name used in this query. */
    @Override
    public String fieldName() {
        return this.fieldName;
    }

    /** Returns the value used in this query. */
    public Object value() {
        return this.value;
    }

    /** Sets the operator to use when using a boolean query. Defaults to {@code OR}. */
    public MatchQueryBuilder operator(Operator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("[" + NAME + "] requires operator to be non-null");
        }
        this.operator = operator;
        return this;
    }

    /** Returns the operator to use in a boolean query.*/
    public Operator operator() {
        return this.operator;
    }

    /**
     * Explicitly set the analyzer to use. Defaults to use explicit mapping config for the field, or, if not
     * set, the default search analyzer.
     */
    public MatchQueryBuilder analyzer(String analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    /** Get the analyzer to use, if previously set, otherwise {@code null} */
    public String analyzer() {
        return this.analyzer;
    }

    @Deprecated
    /* Sets the fuzziness used when evaluated to a fuzzy query type. Defaults to "AUTO". */
    public MatchQueryBuilder fuzziness(Object fuzziness) {
        this.fuzziness = Fuzziness.build(fuzziness);
        return this;
    }

    /** Sets the fuzziness used when evaluated to a fuzzy query type. Defaults to "AUTO". */
    public MatchQueryBuilder fuzziness(Fuzziness fuzziness) {
        this.fuzziness = fuzziness;
        return this;
    }

    /**  Gets the fuzziness used when evaluated to a fuzzy query type. */
    public Fuzziness fuzziness() {
        return this.fuzziness;
    }

    /**
     * Sets the length of a length of common (non-fuzzy) prefix for fuzzy match queries
     * @param prefixLength non-negative length of prefix
     * @throws IllegalArgumentException in case the prefix is negative
     */
    public MatchQueryBuilder prefixLength(int prefixLength) {
        if (prefixLength < 0) {
            throw new IllegalArgumentException("[" + NAME + "] requires prefix length to be non-negative.");
        }
        this.prefixLength = prefixLength;
        return this;
    }

    /**
     * Gets the length of a length of common (non-fuzzy) prefix for fuzzy match queries
     */
    public int prefixLength() {
        return this.prefixLength;
    }

    /**
     * When using fuzzy or prefix type query, the number of term expansions to use.
     */
    public MatchQueryBuilder maxExpansions(int maxExpansions) {
        if (maxExpansions <= 0) {
            throw new IllegalArgumentException("[" + NAME + "] requires maxExpansions to be positive.");
        }
        this.maxExpansions = maxExpansions;
        return this;
    }

    /**
     * Get the (optional) number of term expansions when using fuzzy or prefix type query.
     */
    public int maxExpansions() {
        return this.maxExpansions;
    }

    /**
     * Set a cutoff value in [0..1] (or absolute number &gt;=1) representing the
     * maximum threshold of a terms document frequency to be considered a low
     * frequency term.
     *
     * @deprecated see {@link MatchQueryBuilder#CUTOFF_FREQUENCY_FIELD} for more details
     */
    @Deprecated
    public MatchQueryBuilder cutoffFrequency(float cutoff) {
        this.cutoffFrequency = cutoff;
        return this;
    }

    /** Gets the optional cutoff value, can be {@code null} if not set previously */
    public Float cutoffFrequency() {
        return this.cutoffFrequency;
    }

    /** Sets optional minimumShouldMatch value to apply to the query */
    public MatchQueryBuilder minimumShouldMatch(String minimumShouldMatch) {
        this.minimumShouldMatch = minimumShouldMatch;
        return this;
    }

    /** Gets the minimumShouldMatch value */
    public String minimumShouldMatch() {
        return this.minimumShouldMatch;
    }

    /** Sets the fuzzy_rewrite parameter controlling how the fuzzy query will get rewritten */
    public MatchQueryBuilder fuzzyRewrite(String fuzzyRewrite) {
        this.fuzzyRewrite = fuzzyRewrite;
        return this;
    }

    /**
     * Get the fuzzy_rewrite parameter
     * @see #fuzzyRewrite(String)
     */
    public String fuzzyRewrite() {
        return this.fuzzyRewrite;
    }

    /**
     * Sets whether transpositions are supported in fuzzy queries.<p>
     * The default metric used by fuzzy queries to determine a match is the Damerau-Levenshtein
     * distance formula which supports transpositions. Setting transposition to false will
     * switch to classic Levenshtein distance.<br>
     * If not set, Damerau-Levenshtein distance metric will be used.
     */
    public MatchQueryBuilder fuzzyTranspositions(boolean fuzzyTranspositions) {
        this.fuzzyTranspositions = fuzzyTranspositions;
        return this;
    }

    /** Gets the fuzzy query transposition setting. */
    public boolean fuzzyTranspositions() {
        return this.fuzzyTranspositions;
    }

    /**
     * Sets whether format based failures will be ignored.
     */
    public MatchQueryBuilder lenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    /**
     * Gets leniency setting that controls if format based failures will be ignored.
     */
    public boolean lenient() {
        return this.lenient;
    }

    /**
     * Sets query to use in case no query terms are available, e.g. after analysis removed them.
     * Defaults to {@link MatchQuery.ZeroTermsQuery#NONE}, but can be set to
     * {@link MatchQuery.ZeroTermsQuery#ALL} instead.
     */
    public MatchQueryBuilder zeroTermsQuery(MatchQuery.ZeroTermsQuery zeroTermsQuery) {
        if (zeroTermsQuery == null) {
            throw new IllegalArgumentException("[" + NAME + "] requires zeroTermsQuery to be non-null");
        }
        this.zeroTermsQuery = zeroTermsQuery;
        return this;
    }

    /**
     * Returns the setting for handling zero terms queries.
     */
    public MatchQuery.ZeroTermsQuery zeroTermsQuery() {
        return this.zeroTermsQuery;
    }

    public MatchQueryBuilder autoGenerateSynonymsPhraseQuery(boolean enable) {
        this.autoGenerateSynonymsPhraseQuery = enable;
        return this;
    }

    /**
     * Whether phrase queries should be automatically generated for multi terms synonyms.
     * Defaults to {@code true}.
     */
    public boolean autoGenerateSynonymsPhraseQuery() {
        return autoGenerateSynonymsPhraseQuery;
    }

    @Override
    public void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.startObject(fieldName);

        builder.field(QUERY_FIELD.getPreferredName(), value);
        builder.field(OPERATOR_FIELD.getPreferredName(), operator.toString());
        if (analyzer != null) {
            builder.field(ANALYZER_FIELD.getPreferredName(), analyzer);
        }
        if (fuzziness != null) {
            fuzziness.toXContent(builder, params);
        }
        builder.field(PREFIX_LENGTH_FIELD.getPreferredName(), prefixLength);
        builder.field(MAX_EXPANSIONS_FIELD.getPreferredName(), maxExpansions);
        if (minimumShouldMatch != null) {
            builder.field(MINIMUM_SHOULD_MATCH_FIELD.getPreferredName(), minimumShouldMatch);
        }
        if (fuzzyRewrite != null) {
            builder.field(FUZZY_REWRITE_FIELD.getPreferredName(), fuzzyRewrite);
        }
        // LUCENE 4 UPGRADE we need to document this & test this
        builder.field(FUZZY_TRANSPOSITIONS_FIELD.getPreferredName(), fuzzyTranspositions);
        builder.field(LENIENT_FIELD.getPreferredName(), lenient);
        builder.field(ZERO_TERMS_QUERY_FIELD.getPreferredName(), zeroTermsQuery.toString());
        if (cutoffFrequency != null) {
            builder.field(CUTOFF_FREQUENCY_FIELD.getPreferredName(), cutoffFrequency);
        }
        builder.field(GENERATE_SYNONYMS_PHRASE_QUERY.getPreferredName(), autoGenerateSynonymsPhraseQuery);
        printBoostAndQueryName(builder);
        builder.endObject();
        builder.endObject();
    }

    @Override
    protected Query doToQuery(QueryShardContext context) throws IOException {
        // validate context specific fields
        if (analyzer != null && context.getIndexAnalyzers().get(analyzer) == null) {
            throw new QueryShardException(context, "[" + NAME + "] analyzer [" + analyzer + "] not found");
        }

        MatchQuery matchQuery = new MatchQuery(context);
        matchQuery.setOccur(operator.toBooleanClauseOccur());
        if (analyzer != null) {
            matchQuery.setAnalyzer(analyzer);
        }
        matchQuery.setFuzziness(fuzziness);
        matchQuery.setFuzzyPrefixLength(prefixLength);
        matchQuery.setMaxExpansions(maxExpansions);
        matchQuery.setTranspositions(fuzzyTranspositions);
        matchQuery.setFuzzyRewriteMethod(QueryParsers.parseRewriteMethod(fuzzyRewrite, null, LoggingDeprecationHandler.INSTANCE));
        matchQuery.setLenient(lenient);
        matchQuery.setCommonTermsCutoff(cutoffFrequency);
        matchQuery.setZeroTermsQuery(zeroTermsQuery);
        matchQuery.setAutoGenerateSynonymsPhraseQuery(autoGenerateSynonymsPhraseQuery);

        Query query = matchQuery.parse(MatchQuery.Type.BOOLEAN, fieldName, value);
        return Queries.maybeApplyMinimumShouldMatch(query, minimumShouldMatch);
    }

    @Override
    protected boolean doEquals(MatchQueryBuilder other) {
        return Objects.equals(fieldName, other.fieldName)
            && Objects.equals(value, other.value)
            && Objects.equals(operator, other.operator)
            && Objects.equals(analyzer, other.analyzer)
            && Objects.equals(fuzziness, other.fuzziness)
            && Objects.equals(prefixLength, other.prefixLength)
            && Objects.equals(maxExpansions, other.maxExpansions)
            && Objects.equals(minimumShouldMatch, other.minimumShouldMatch)
            && Objects.equals(fuzzyRewrite, other.fuzzyRewrite)
            && Objects.equals(lenient, other.lenient)
            && Objects.equals(fuzzyTranspositions, other.fuzzyTranspositions)
            && Objects.equals(zeroTermsQuery, other.zeroTermsQuery)
            && Objects.equals(cutoffFrequency, other.cutoffFrequency)
            && Objects.equals(autoGenerateSynonymsPhraseQuery, other.autoGenerateSynonymsPhraseQuery);
    }

    @Override
    protected int doHashCode() {
        return Objects.hash(
            fieldName,
            value,
            operator,
            analyzer,
            fuzziness,
            prefixLength,
            maxExpansions,
            minimumShouldMatch,
            fuzzyRewrite,
            lenient,
            fuzzyTranspositions,
            zeroTermsQuery,
            cutoffFrequency,
            autoGenerateSynonymsPhraseQuery
        );
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    public static MatchQueryBuilder fromXContent(XContentParser parser) throws IOException {
        String fieldName = null;
        Object value = null;
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String minimumShouldMatch = null;
        String analyzer = null;
        Operator operator = MatchQueryBuilder.DEFAULT_OPERATOR;
        Fuzziness fuzziness = null;
        int prefixLength = FuzzyQuery.defaultPrefixLength;
        int maxExpansion = FuzzyQuery.defaultMaxExpansions;
        boolean fuzzyTranspositions = FuzzyQuery.defaultTranspositions;
        String fuzzyRewrite = null;
        boolean lenient = MatchQuery.DEFAULT_LENIENCY;
        Float cutOffFrequency = null;
        ZeroTermsQuery zeroTermsQuery = MatchQuery.DEFAULT_ZERO_TERMS_QUERY;
        boolean autoGenerateSynonymsPhraseQuery = true;
        String queryName = null;
        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, currentFieldName);
                fieldName = currentFieldName;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentFieldName = parser.currentName();
                    } else if (token.isValue()) {
                        if (QUERY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            value = parser.objectText();
                        } else if (ANALYZER_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            analyzer = parser.text();
                        } else if (AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            boost = parser.floatValue();
                        } else if (Fuzziness.FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            fuzziness = Fuzziness.parse(parser);
                        } else if (PREFIX_LENGTH_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            prefixLength = parser.intValue();
                        } else if (MAX_EXPANSIONS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            maxExpansion = parser.intValue();
                        } else if (OPERATOR_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            operator = Operator.fromString(parser.text());
                        } else if (MINIMUM_SHOULD_MATCH_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            minimumShouldMatch = parser.textOrNull();
                        } else if (FUZZY_REWRITE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            fuzzyRewrite = parser.textOrNull();
                        } else if (FUZZY_TRANSPOSITIONS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            fuzzyTranspositions = parser.booleanValue();
                        } else if (LENIENT_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            lenient = parser.booleanValue();
                        } else if (CUTOFF_FREQUENCY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            cutOffFrequency = parser.floatValue();
                        } else if (ZERO_TERMS_QUERY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            String zeroTermsValue = parser.text();
                            if ("none".equalsIgnoreCase(zeroTermsValue)) {
                                zeroTermsQuery = MatchQuery.ZeroTermsQuery.NONE;
                            } else if ("all".equalsIgnoreCase(zeroTermsValue)) {
                                zeroTermsQuery = MatchQuery.ZeroTermsQuery.ALL;
                            } else {
                                throw new ParsingException(
                                    parser.getTokenLocation(),
                                    "Unsupported zero_terms_query value [" + zeroTermsValue + "]"
                                );
                            }
                        } else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            queryName = parser.text();
                        } else if (GENERATE_SYNONYMS_PHRASE_QUERY.match(currentFieldName, parser.getDeprecationHandler())) {
                            autoGenerateSynonymsPhraseQuery = parser.booleanValue();
                        } else {
                            throw new ParsingException(
                                parser.getTokenLocation(),
                                "[" + NAME + "] query does not support [" + currentFieldName + "]"
                            );
                        }
                    } else {
                        throw new ParsingException(
                            parser.getTokenLocation(),
                            "[" + NAME + "] unknown token [" + token + "] after [" + currentFieldName + "]"
                        );
                    }
                }
            } else {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, parser.currentName());
                fieldName = parser.currentName();
                value = parser.objectText();
            }
        }

        if (value == null) {
            throw new ParsingException(parser.getTokenLocation(), "No text specified for text query");
        }

        MatchQueryBuilder matchQuery = new MatchQueryBuilder(fieldName, value);
        matchQuery.operator(operator);
        matchQuery.analyzer(analyzer);
        matchQuery.minimumShouldMatch(minimumShouldMatch);
        matchQuery.fuzziness(fuzziness);
        matchQuery.fuzzyRewrite(fuzzyRewrite);
        matchQuery.prefixLength(prefixLength);
        matchQuery.fuzzyTranspositions(fuzzyTranspositions);
        matchQuery.maxExpansions(maxExpansion);
        matchQuery.lenient(lenient);
        if (cutOffFrequency != null) {
            matchQuery.cutoffFrequency(cutOffFrequency);
        }
        matchQuery.zeroTermsQuery(zeroTermsQuery);
        matchQuery.autoGenerateSynonymsPhraseQuery(autoGenerateSynonymsPhraseQuery);
        matchQuery.queryName(queryName);
        matchQuery.boost(boost);
        return matchQuery;
    }

    @Override
    public List<QueryBuilder> getComplement(QueryShardContext context) {
        // If this is a match query on a numeric field, we can provide the complement using RangeQueryBuilder.
        NumberFieldMapper.NumberFieldType nft = ComplementHelperUtils.getNumberFieldType(context, fieldName);
        if (nft == null) return null;
        Number numberValue = nft.parse(value);
        return ComplementHelperUtils.numberValueToComplement(fieldName, numberValue);
    }
}
