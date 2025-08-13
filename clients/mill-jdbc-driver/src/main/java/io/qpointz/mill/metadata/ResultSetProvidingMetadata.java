package io.qpointz.mill.metadata;

import io.qpointz.mill.vectors.ObjectToVectorProducer;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Abstract class that provides metadata and allows it to be represented as a {@link ResultSet}.
 *
 * @param <T> the type of metadata that this class handles
 */
public abstract class ResultSetProvidingMetadata<T> {

    /**
     * Provides a list of mappers to be used in converting the metadata to vectors.
     *
     * @return a list of mapper information objects
     */
    protected abstract List<ObjectToVectorProducer.MapperInfo<T,?>> getMappers();

    /**
     * Provides the metadata collection.
     *
     * @return a collection of metadata objects
     */
    protected abstract Collection<T> getMetadata();

    /**
     * Converts the metadata into a {@link ResultSet}.
     *
     * @return a {@link ResultSet} representing the metadata
     * @throws SQLException if an SQL error occurs
     */
    public ResultSet asResultSet() throws SQLException {
        return ObjectToVectorProducer.resultSet(this.getMappers(), getMetadata());
    }

    /**
     * Filters a collection of metadata by a given predicate.
     *
     * @param pattern   the pattern to filter by
     * @param source    the source collection of metadata
     * @param expression the predicate to test each metadata object
     * @param <K>       the type of the pattern
     * @return a collection of filtered metadata
     */
    protected <K> Collection<T> filterByPredicate(K pattern, Collection<T> source, BiPredicate<T, K> expression) {
        if (pattern == null) {
            return source;
        }

        return source.stream()
                .filter(k -> expression.test(k, pattern))
                .toList();
    }

    /**
     * Filters a collection of metadata by a given string pattern.
     *
     * @param pattern   the string pattern to filter by
     * @param source    the source collection of metadata
     * @param expression the function to extract a string value from a metadata object
     * @return a collection of filtered metadata
     */
    protected Collection<T> filterByPattern(String pattern, Collection<T> source, Function<T, String> expression) {
        if (pattern == null) {
            return source;
        }

        if (pattern.isEmpty()) {
            return source.stream().filter(k -> {
                val expValue = expression.apply(k);
                return expValue == null || expValue.isEmpty();
            }).toList();
        }

        val predicate = getPredicate(pattern);

        return source.stream().filter(k -> predicate.test(expression.apply(k)))
                .toList();
    }

    private Predicate<String> getPredicate(String pattern) {
        if (!pattern.contains("%")) {
            return pattern::equals;
        }

        val rxPatternString = "^" + pattern.replace("%", ".*") + "$";
        return Pattern.compile(rxPatternString).asPredicate();
    }
}
