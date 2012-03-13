package org.fjtp.util;

public class Pair<F, S> {
    public final F f;
    public final S s;
    
    public Pair(F f, S s) {
        this.f = f;
        this.s = s;
    }
    
    public static <F, S> Pair<F, S> of(F f, S s) {
        return new Pair<F, S>(f, s);
    }
    
    public int hashCode() {
        return f.hashCode() * 37 + s.hashCode();
    }
    
    public boolean equals(Object obj) {
        if(! (obj instanceof Pair))
            return false;
        
        Pair<?, ?> o = (Pair<?, ?>) obj;
        return f.equals(o.f) && s.equals(o.s);
    }
}
