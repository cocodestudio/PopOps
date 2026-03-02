package com.cocode.popops.storage;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Low-level encrypted file read/write in app internal storage.
 */
public final class SecureFileStore {
    private static final String TAG = "PopOps";
    private static File file;

    private SecureFileStore() {
    }

    public static void init(Context ctx) {
        file = new File(ctx.getFilesDir(), "popops_sdk_store.bin");
    }

    public static synchronized void write(byte[] data) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            fos.getFD().sync();
        }
    }

    public static synchronized byte[] read() throws Exception {
        if (file == null || !file.exists()) return null;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] b = new byte[(int) file.length()];
            int offset = 0;
            int remaining = b.length;
            while (remaining > 0) {
                int read = fis.read(b, offset, remaining);
                if (read == -1) break;
                offset += read;
                remaining -= read;
            }
            if (remaining > 0) {
                throw new Exception("Could not read entirely");
            }
            return b;
        }
    }
}