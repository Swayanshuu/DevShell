package com.devcli.wrap;

@FunctionalInterface
public interface WrapProgressListener {
    void onProgress(int percentage, String statusMessage);
}
