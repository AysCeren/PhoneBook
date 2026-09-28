package com.project.contactsdemo.core.cache;

/** Names of the Hazelcast maps used as caches, shared by the services that read and evict them. */
public final class CacheNames {
    /** All persons, under the single key {@link #ALL}. */
    public static final String PERSON_RESPONSE_ALL = "personResponseAll";
    /** All contacts, under the single key {@link #ALL}. */
    public static final String CONTACT_RESPONSE_ALL = "contactResponseAll";
    /** Active contacts of one person, keyed by person id. */
    public static final String PERSON_WITH_CONTACTS = "personWithContacts";

    /** Key for maps that hold one "everything" entry. */
    public static final String ALL = "all";

    private CacheNames() {
    }
}
