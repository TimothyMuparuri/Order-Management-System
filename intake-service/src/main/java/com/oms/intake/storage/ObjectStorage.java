package com.oms.intake.storage;

import java.io.IOException;

public interface ObjectStorage {
    void store(String objectKey, byte[] bytes, String contentType) throws IOException;
    byte[] load(String objectKey) throws IOException;
}
