package com.exemplo.metas.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class MetaAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertMetaAllPropertiesEquals(Meta expected, Meta actual) {
        assertMetaAutoGeneratedPropertiesEquals(expected, actual);
        assertMetaAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertMetaAllUpdatablePropertiesEquals(Meta expected, Meta actual) {
        assertMetaUpdatableFieldsEquals(expected, actual);
        assertMetaUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertMetaAutoGeneratedPropertiesEquals(Meta expected, Meta actual) {
        assertThat(actual)
            .as("Verify Meta auto generated properties")
            .satisfies(a -> assertThat(a.getId()).as("check id").isEqualTo(expected.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertMetaUpdatableFieldsEquals(Meta expected, Meta actual) {
        assertThat(actual)
            .as("Verify Meta relevant properties")
            .satisfies(a -> assertThat(a.getValor()).as("check valor").isEqualTo(expected.getValor()))
            .satisfies(a -> assertThat(a.getArea()).as("check area").isEqualTo(expected.getArea()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertMetaUpdatableRelationshipsEquals(Meta expected, Meta actual) {
        assertThat(actual)
            .as("Verify Meta relationships")
            .satisfies(a -> assertThat(a.getAluno()).as("check aluno").isEqualTo(expected.getAluno()));
    }
}
