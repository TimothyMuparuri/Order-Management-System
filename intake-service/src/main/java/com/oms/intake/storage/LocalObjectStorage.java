package com.oms.intake.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class LocalObjectStorage implements ObjectStorage {

    private final Path root;

    public LocalObjectStorage(@Value("${oms.storage.local-dir:./.oms-storage}") String localDir) throws IOException {
        this.root = Path.of(localDir).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    @Override
    public void store(String objectKey, byte[] bytes, String contentType) throws IOException {
        Path target = root.resolve(objectKey).normalize();
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
    }

    @Override
    public byte[] load(String objectKey) throws IOException {
        return Files.readAllBytes(root.resolve(objectKey).normalize());
    }
}
