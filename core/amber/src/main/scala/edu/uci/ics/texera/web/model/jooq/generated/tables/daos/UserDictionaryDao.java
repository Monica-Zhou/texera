/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.daos;


import edu.uci.ics.texera.web.model.jooq.generated.tables.UserDictionary;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.UserDictionaryRecord;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.Record2;
import org.jooq.impl.DAOImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserDictionaryDao extends DAOImpl<UserDictionaryRecord, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserDictionary, Record2<UInteger, String>> {

    /**
     * Create a new UserDictionaryDao without any configuration
     */
    public UserDictionaryDao() {
        super(UserDictionary.USER_DICTIONARY, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserDictionary.class);
    }

    /**
     * Create a new UserDictionaryDao with an attached configuration
     */
    public UserDictionaryDao(Configuration configuration) {
        super(UserDictionary.USER_DICTIONARY, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserDictionary.class, configuration);
    }

    @Override
    public Record2<UInteger, String> getId(edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserDictionary object) {
        return compositeKeyRecord(object.getUid(), object.getKey());
    }

    /**
     * Fetch records that have <code>uid BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserDictionary> fetchRangeOfUid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(UserDictionary.USER_DICTIONARY.UID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>uid IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserDictionary> fetchByUid(UInteger... values) {
        return fetch(UserDictionary.USER_DICTIONARY.UID, values);
    }

    /**
     * Fetch records that have <code>key BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserDictionary> fetchRangeOfKey(String lowerInclusive, String upperInclusive) {
        return fetchRange(UserDictionary.USER_DICTIONARY.KEY, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>key IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserDictionary> fetchByKey(String... values) {
        return fetch(UserDictionary.USER_DICTIONARY.KEY, values);
    }

    /**
     * Fetch records that have <code>value BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserDictionary> fetchRangeOfValue(String lowerInclusive, String upperInclusive) {
        return fetchRange(UserDictionary.USER_DICTIONARY.VALUE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>value IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserDictionary> fetchByValue(String... values) {
        return fetch(UserDictionary.USER_DICTIONARY.VALUE, values);
    }
}
