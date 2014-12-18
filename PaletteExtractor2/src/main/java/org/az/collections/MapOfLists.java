package org.az.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Artem Zaborskiy
 *
 * @param <X>
 * @param <T>
 */
public class MapOfLists<X, T> {
    private final Map<X, List<T>> map = new HashMap<X, List<T>>();

    final Comparator<Entry<X, List<T>>> comparatorAsc = new Comparator<Entry<X, List<T>>>() {
        @Override
        public int compare(final Entry<X, List<T>> e1, final Entry<X, List<T>> e2) {
            return e1.getValue().size() - e2.getValue().size();
        }
    };

    final Comparator<Entry<X, List<T>>> comparatorDesc = new Comparator<Entry<X, List<T>>>() {
        @Override
        public int compare(final Entry<X, List<T>> e1, final Entry<X, List<T>> e2) {
            return e2.getValue().size() - e1.getValue().size();
        }
    };

    public List<T> get(final X listKey) {
        return map.get(listKey);
    }

    public List<T> getLongest() {
        return sortByListSize(false).get(0).getValue();
    }

    public Map<X, List<T>> getMap() {
        return map;
    }

    public Set<X> keySet() {
        return map.keySet();
    }

    public void put(final X listKey, final T valueForList) {
        List<T> list = map.get(listKey);
        if (list == null) {
            list = new ArrayList<T>();
            map.put(listKey, list);
        }
        list.add(valueForList);
    }

    public boolean remove(final X listKey, final T value) {
        return map.get(listKey).remove(value);
    }

    public void removeKey(final X listKey) {
        map.remove(listKey);
    }

    public int size() {
        return map.size();
    }

    public List<Entry<X, List<T>>> sortByListSize(final boolean asc) {
        final Set<Entry<X, List<T>>> entrySet = map.entrySet();
        final ArrayList<Entry<X, List<T>>> x = new ArrayList<Entry<X, List<T>>>(entrySet);

        Collections.sort(x, asc ? comparatorAsc : comparatorDesc);

        return x;
    }

}
