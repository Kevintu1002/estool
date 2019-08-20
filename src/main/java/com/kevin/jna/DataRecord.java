package com.kevin.inte;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

public class DataRecord extends Structure {
//
//    public int size;
//    public int ts;
//    public int as;
//    public int cs;
//    public int ds;
//    public String title;
//    public String abs;
//    public String claim;
//    public String desc;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("size", "ts", "as", "cs", "ds", "title", "abs", "claim", "desc");
    }
    //    public DataRecord() {
//        super();
//    }
//
//    public DataRecord(Pointer dr) {
//        super(dr);
//    }
//
    public static class ByReference extends DataRecord implements Structure.ByReference {
        public int size;
        public int ts;
        public int as;
        public int cs;
        public int ds;
        public Pointer title;
//        public String title;
//        public String abs;
//        public String claim;
//        public String desc;
    }

    public static class ByValue extends DataRecord implements Structure.ByValue {
        public int size;
        public int ts;
        public int as;
        public int cs;
        public int ds;
        public String title;
        public String abs;
        public String claim;
        public String desc;
    }
}
