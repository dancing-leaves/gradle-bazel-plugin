package org.jetbrains.bazel.utils

import org.gradle.api.provider.MapProperty
import org.jetbrains.bazel.exceptions.BazelOptionOverrideException
import org.jetbrains.bazel.exceptions.BazelOptionUnsupportedValueType

/**
 * Property interfaces provided by gradle don't seem to be easily
 * implementable and come with "not intended for implementation by
 * build script or plugin authors" note. Solution below is the approach
 * to make configuration more flexible, so one can pass integer to
 * parameters that expect numeric values, true/false to boolean ones,
 * and list of strings for multi values. This way does't feel exactly
 * right, but it's best solution i could make work
 */


/**
 * Parent class for all wrappers for types
 * we consider to represent basel options
 *
 * @param <T> wrapped type
 */
abstract class BazelOption<T> {
    def key
    T value

    /**
     * @param key    option name (derived from original bazel command line option)
     * @param value  option value
     */
    BazelOption(String key, T value) {
        this.key = key
        this.value = value
    }

    /**
     * Generate bazel command line arguments.
     *
     * @return list of command line arguments bazel can understand
     */
    String[] getCmdArgs() {
        ["--${key}=${value}".toString()]
    }

    /**
     * Factory for supported bazel options. Implementation is derived based on value type
     *
     * @param key
     * @param value
     * @return BazelOption child object
     */
    static BazelOption factory(key, value) {
        if (value instanceof Boolean)
            return new BazelBooleanOption(key, value as Boolean)
        if (value instanceof Integer)
            return new BazelIntegerOption (key, value as Integer)
        if (value instanceof String)
            return new BazelStringOption(key, value as String)
        if (value instanceof ArrayList)
            return new BazelListOption(key, value as ArrayList)

        throw new BazelOptionUnsupportedValueType(value.class)
    }

    /**
     * Helper to convert MapProperty into BazelOption list
     *
     * @param optionMapProperty
     * @return list of bazel options
     */
    static BazelOption[] getOptions(MapProperty<String,Object> optionMapProperty) {
        optionMapProperty.get().collect { key, value ->
            BazelOption.factory(key, value)
        }
    }

    /**
     * Helper to merge option lists. Order of lists makes sense,
     * e.g. default combine strategy is override so options
     * in first list will override options of all others
     *
     * @param optionLists
     * @return
     */
    static BazelOption[] combine(BazelOption[]... optionLists) {
        def keys = optionLists.collect { it.key }.flatten().unique()

        keys.collect { key ->
            (optionLists.collect { optionList ->
                optionList.find { it.key == key }
            } - null).sum()
        }
    }

    /**
     * Simple override combining for two options
     *
     * @param a prevailing option
     * @param b rejected option
     * @return prevailing option
     */
    static <A extends BazelOption> A defaultPlus(A a, A b) { a }
}

/**
 * Interface to define compliance for options that
 * can produce some combined result, like extend
 * or override one anther
 */
interface CombinableBazelOption<T extends BazelOption> {
    T plus(BazelOption other)
}


/**
 * BazelOption children
 */

class BazelBooleanOption extends BazelOption<Boolean>
        implements CombinableBazelOption<BazelBooleanOption> {

    BazelBooleanOption(String key, Boolean value) {
        super(key, value)
    }

    @Override
    String[] getCmdArgs() {
        ["--${value ? '' : 'no'}${key}".toString()]
    }

    @Override
    BazelBooleanOption plus(BazelOption other) {
        if (getClass() != other.class)
            throw new BazelOptionOverrideException(this, other)

        BazelOption.defaultPlus(this, other)
    }
}


class BazelIntegerOption extends BazelOption<Integer>
        implements CombinableBazelOption<BazelIntegerOption> {

    BazelIntegerOption(String key, Integer value) {
        super(key, value)
    }

    @Override
    BazelIntegerOption plus(BazelOption other) {
        if (getClass() != other.class)
            throw new BazelOptionOverrideException(this, other)

        BazelOption.defaultPlus(this, other)
    }
}


class BazelStringOption extends BazelOption<String>
        implements CombinableBazelOption<BazelStringOption> {

    BazelStringOption(String key, String value) {
        super(key, value)
    }

    @Override
    BazelStringOption plus(BazelOption other) {
        if (getClass() != other.class)
            throw new BazelOptionOverrideException(this, other)

        BazelOption.defaultPlus(this, other)
    }
}


class BazelListOption extends BazelOption<ArrayList>
        implements CombinableBazelOption<BazelListOption> {

    BazelListOption(String key, ArrayList value) {
        super(key, value)
    }

    @Override
    String[] getCmdArgs() {
        value.collect { "--${key}=${it}".toString() }
    }

    @Override
    BazelListOption plus(BazelOption other) {
        if (getClass() != other.class)
            throw new BazelOptionOverrideException(this, other)

        return new BazelListOption(this.key, this.value + other.value)
    }
}
