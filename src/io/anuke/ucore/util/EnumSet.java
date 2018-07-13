package io.anuke.ucore.util;

import java.util.Iterator;

public class EnumSet<T extends Enum<T>> implements Iterable<T>{
    private int i;
    private T[] set;

    private EnumSet(){
    }

    public static <T extends Enum<T>> EnumSet<T> of(T... arr){
        EnumSet<T> set = new EnumSet<>();
        set.set = arr;
        for(T t : arr){
            set.i |= (1 << t.ordinal());
        }
        return set;
    }

    public boolean contains(T t){
        return (i & Mathf.pow2(t.ordinal())) != 0;
    }

    @Override
    public Iterator<T> iterator(){
        return new Iterator<T>(){
            int index = 0;

            @Override
            public boolean hasNext(){
                return index < set.length;
            }

            @Override
            public T next(){
                return set[index++];
            }
        };
    }
}
