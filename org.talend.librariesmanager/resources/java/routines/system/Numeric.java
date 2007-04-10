package routines;

public class Numeric {
    private static final java.util.Map<String, Integer> seq_Hash = new java.util.HashMap<String, Integer>();
    /**
     * return an incremented numeric id
     * 
     * {talendTypes} int | Integer
     * {param} string("s1") sequence identifier
     * {param} int(1) start value
     * {param} int(1) step
     *
     * {example} sequence("s1", 1, 1) # 1, 2, 3, ...
     * {example} sequence("s2", 100, -2) # 100, 98, 96, ...
     * 
     */
    public static Integer sequence(String seqName, int startValue, int step) {
        if (seq_Hash.containsKey(seqName)) {
            seq_Hash.put(seqName, seq_Hash.get(seqName)+step);
            return seq_Hash.get(seqName);
        } else {
            seq_Hash.put(seqName, startValue);
            return startValue;
        }
    }
}