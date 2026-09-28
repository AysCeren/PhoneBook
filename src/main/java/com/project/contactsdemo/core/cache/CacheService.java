package com.project.contactsdemo.core.cache;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class CacheService<T> {

    private final HazelcastInstance hazelcastInstance;

    public CacheService() {
        hazelcastInstance = Hazelcast.newHazelcastInstance(); // Instantiation
    }

    public boolean saveToCache(T saveDTO, String key, String mapName) {
        try {
            IMap<String, T> map = hazelcastInstance.getMap(mapName);//map name
            map.putIfAbsent(key,saveDTO); //key parametre olacak
            return true;
        } catch (Exception e) { //buranın exception'ını handle'layacağız
            return false;
        }
    }

    public T getFromCache(String key, String mapName) {
        try {
            IMap<String, T> map = hazelcastInstance.getMap(mapName); //map adı
            return map.get(key);//liste olmasa key daha anlamlı olurdu, liste için all
        } catch (Exception e) {
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
            hazelcastInstance.getMap(mapName).clear();
        }
    }
}
