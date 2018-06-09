package com.hackathon.eagleeye.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recoknizer/image")
@Slf4j
public class ImageRecoknizerService {

  @GetMapping("/process")
  public void process(String imageLocation) {
    // Explore Recoknition API
  }
}
