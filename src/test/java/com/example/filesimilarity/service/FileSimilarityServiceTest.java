package com.example.filesimilarity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FileSimilarityServiceTest {

    private Path fileB;
    private Path poolDir;

    private FileSimilarityService service;

    @BeforeEach
    void setUp() throws IOException {
        fileB = Files.createTempFile("fileB", ".txt");
        poolDir = Files.createTempDirectory("pool");

        try (FileWriter writer = new FileWriter(fileB.toFile())) {
            writer.write("apple banana orange mango grape");
        }

        Path file1 = Files.createFile(poolDir.resolve("fileB1.txt"));
        try (FileWriter writer = new FileWriter(file1.toFile())) {
            writer.write("apple banana cherry");
        }

        Path file2 = Files.createFile(poolDir.resolve("fileB2.txt"));
        try (FileWriter writer = new FileWriter(file2.toFile())) {
            writer.write("apple banana orange mango grape");
        }

        Path file3 = Files.createFile(poolDir.resolve("fileB3.txt"));
        try (FileWriter writer = new FileWriter(file3.toFile())) {
            writer.write("car truck engine");
        }

        ResourceLoader resourceLoader = new DefaultResourceLoader();

        service = new FileSimilarityService(
        	    fileB.toAbsolutePath().toString(),
        	    poolDir.toAbsolutePath().toString(),
        	    resourceLoader
        	);
    }

    @Test
    void testComputeSimilarities() throws IOException {
        Map<String, Double> result = service.computeSimilaritiesParallel();

        assertEquals(3, result.size());
        assertEquals(100.0, result.get("fileB2.txt"), 0.01);
        assertTrue(result.get("fileB1.txt") < 100.0);
        assertEquals(0.0, result.get("fileB3.txt"), 0.01);
    }
}
