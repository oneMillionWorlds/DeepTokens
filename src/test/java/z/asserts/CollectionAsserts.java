/* [2017] (C) Richard Tingle */
package z.asserts;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CollectionAsserts{
    public static <T> void assertEqualContents(Collection<T> expected, Collection<T> actual){
        
        Collection<T> extra = new ArrayList<>(actual);
        extra.removeAll(expected);
        
        Collection<T> missing = new ArrayList<>(expected);
        missing.removeAll(actual);

        String message = "";
        if (!extra.isEmpty()){
            message+="Contained extra [" + extra + "] but shouldn't have.";
        }

        if (!missing.isEmpty()){
            message+="Didn't contain ["  + missing + "]";
        }

        assertTrue(extra.isEmpty() && missing.isEmpty(), message );

        assertEquals(expected.size(), actual.size(), "Although they contained the same contents they were different sizes (so had duplicates), dups: " + countDuplicates(actual));
    }

    private static Map<Object,Integer> countDuplicates(Collection<?> expected){
        Map<Object,Integer> count = new HashMap<>();
        for(Object o: expected ){
            count.put(o, count.getOrDefault(o, 0)+1);
        }
        
        //remove non duplicates
        for(Object o:new ArrayList<>(count.keySet())){
            if(count.get(o) == 1){
                count.remove(o);
            }
        }
        
        return count;
        
    }
    
    
}
