package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {
    private final ImageStorageService imageStorageService;

    @PostMapping("/images")
    public Result<Map<String, String>> image(@RequestParam("file") MultipartFile file,
                                              @RequestParam("kind") String kind) {
        return Result.ok(Map.of("url", imageStorageService.save(file, kind)));
    }
}
