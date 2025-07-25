package com.example.filesimilarity.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;


@Service
public class FileSimilarityService {

    private final ResourceLoader resourceLoader;
    private final String fileAPath;
    private final String directoryPath;

    public FileSimilarityService(
    		
	    @Value("${app.fileA.path}") String fileAPath,
	    @Value("${app.directory.path}") String directoryPath,
	    ResourceLoader resourceLoader) {
        this.fileAPath = fileAPath;
        this.directoryPath = directoryPath;
        this.resourceLoader = resourceLoader;
    }

    public Map<String, Double> computeSimilaritiesSequential() throws IOException {
        Set<String> wordsInA = readWordsFromClasspath(fileAPath);

        Resource dirResource = (Resource) resourceLoader.getResource(directoryPath);
        File dir =  dirResource.getFile();

        Map<String, Double> scores = new HashMap<>();

        for (File file : Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(".txt")))) {
            Set<String> wordsInPoolFile = readWordsFromFile(file.toPath());
            long common = wordsInPoolFile.stream().filter(wordsInA::contains).count();
            double score = (100.0 * common) / wordsInA.size();
            scores.put(file.getName(), score);
        }

        return scores;
    }
    
    public Map<String, Double> computeSimilaritiesParallel() throws IOException {
        Set<String> wordsInA = readWordsFromClasspath(fileAPath);

        File dir;
        if (directoryPath.startsWith("classpath:")) {
            Resource dirResource = resourceLoader.getResource(directoryPath);
            dir = dirResource.getFile();
        } else {
        	// for the unit testing
            dir = new File(directoryPath);
        }

        return Arrays.stream(Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(".txt"))))
                .parallel()
                .collect(Collectors.toMap(
                        File::getName,
                        file -> {
                            try {
                                Set<String> wordsInPoolFile = readWordsFromClasspath(file.getAbsolutePath());
                                long common = wordsInPoolFile.stream().filter(wordsInA::contains).count();
                                return (100.0 * common) / wordsInA.size();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return 0.0;
                            }
                        }
                ));
    }


    private Set<String> readWordsFromClasspath(String path) throws IOException {
        Path filePath;

        if (path.startsWith("classpath:")) {
            Resource resource = resourceLoader.getResource(path);
            filePath = resource.getFile().toPath();
        } else {
        	// for the unit testing
            filePath = Path.of(path);
        }

        return Files.lines(filePath)
                .flatMap(line -> Arrays.stream(line.split("\\W+")))
                .filter(word -> word.matches("[a-zA-Z]+"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }



    private Set<String> readWordsFromFile(Path path) throws IOException {
        return Files.lines(path)
                .flatMap(line -> Arrays.stream(line.split("\\W+")))
                .filter(word -> word.matches("[a-zA-Z]+"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
    
    public void comparePerformance() throws IOException {
        long start1 = System.nanoTime();
        Map<String, Double> sequential = computeSimilaritiesSequential();
        long end1 = System.nanoTime();
        long timeSequential = (end1 - start1) / 1_000_000;

        long start2 = System.nanoTime();
        Map<String, Double> parallel = computeSimilaritiesParallel();
        long end2 = System.nanoTime();
        long timeParallel = (end2 - start2) / 1_000_000;

        System.out.println("Sequential execution time: " + timeSequential + " ms");
        System.out.println("Parallel execution time:   " + timeParallel + " ms");

        System.out.println("Results match? " + sequential.equals(parallel));
    }

}
