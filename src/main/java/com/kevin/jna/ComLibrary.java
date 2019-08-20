package com.kevin.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public  class ComLibrary {

    public static class CQuerylibrary {

        //此方法为链接库中的方法
        public static native Pointer DatabaseQueryManager_new(String indexFilename, String dataPath);
        public static native void DatabaseQueryManager_getAllId(Pointer dqmPtr, PointerByReference pids, PointerByReference aids, IntByReference size);
        //        public static native boolean DatabaseQueryManager_getContentById(Pointer dqmPtr,
//                                                                         String id,
//                                                                         DataRecord res);
        public static native com.kevin.inte.DataRecord.ByValue DatabaseQueryManager_getContentById(Pointer dqmPtr, String id);

        static {
            Native.register("DatabaseQuerySelector");
        }

    }



}
