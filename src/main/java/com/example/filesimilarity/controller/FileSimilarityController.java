package com.example.filesimilarity.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.filesimilarity.service.FileSimilarityService;

@RestController
@RequestMapping("/similarity")
public class FileSimilarityController {

    private final FileSimilarityService similarityService;

    public FileSimilarityController(FileSimilarityService similarityService) {
        this.similarityService = similarityService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Double>> getSimilarityScores() {
        try {
            return ResponseEntity.ok(similarityService.computeSimilaritiesParallel());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> benchmark() throws IOException {
    	long start1 = System.nanoTime();
        Map<String, Double> sequential = similarityService.computeSimilaritiesSequential();
        long end1 = System.nanoTime();

        long start2 = System.nanoTime();
        Map<String, Double> parallel = similarityService.computeSimilaritiesParallel();
        long end2 = System.nanoTime();

        Map<String, Object> result = new HashMap<>();
        result.put("sequentialTimeMs", (end1 - start1) / 1_000_000);
        result.put("parallelTimeMs", (end2 - start2) / 1_000_000);
//        result.put("resultsMatch", sequential.equals(parallel));

        return ResponseEntity.ok(result);
    }
}
