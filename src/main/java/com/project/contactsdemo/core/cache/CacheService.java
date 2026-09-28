package com.project.contactsdemo.core.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Cache-aside helper on top of the Hazelcast client.
 * <p>
 * The cache is an optimization, not a source of truth: if Hazelcast is unreachable, reads miss and writes are
 * skipped (with a warning), and the services fall back to the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService<T> {

    // The client bean from HazelcastClientConfig. Previously an explicit no-arg constructor also existed;
    // with two constructors Spring used the no-arg one, which started a separate embedded Hazelcast member.
    private final HazelcastInstance hazelcastInstance;

    public boolean saveToCache(T saveDTO, String key, String mapName) {
        try {
            IMap<String, T> map = hazelcastInstance.getMap(mapName);//map name
            map.putIfAbsent(key,saveDTO); //key parametre olacak
            return true;
        } catch (Exception e) { //e.g. Hazelcast unreachable, or the value isn't serializable
            log.warn("Could not cache {} in map {}: {}", key, mapName, e.toString());
            return false;
        }
    }

    public T getFromCache(String key, String mapName) {
        try {
            IMap<String, T> map = hazelcastInstance.getMap(mapName); //map adı
            return map.get(key);//liste olmasa key daha anlamlı olurdu, liste için all
        } catch (Exception e) {
            log.warn("Could not read {} from cache map {}: {}", key, mapName, e.toString());
            return null;
        }
    }

    /**
     * Empties the given cache maps once the current transaction has committed (immediately if there is none).
     * <p>
     * Clearing before the commit would leave a gap: another request could read the old rows from the
     * database in that moment and put them back into the cache, so the stale data would return.
     */
    public void clearAfterCommit(String... mapNames) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    clear(mapNames);
                }
            });
        } else {
            clear(mapNames);
        }
    }

    private void clear(String... mapNames) {
        for (String mapName : mapNames) {
            try {
                hazelcastInstance.getMap(mapName).clear();
            } catch (Exception e) {
                // The data change is already committed, so don't fail the request; the entry may stay stale.
                log.warn("Could not clear cache map {}: {}", mapName, e.toString());
            }
        }
    }
}
