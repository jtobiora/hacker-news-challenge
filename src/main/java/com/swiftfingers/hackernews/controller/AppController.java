package com.swiftfingers.hackernews.controller;


import com.swiftfingers.hackernews.service.AppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api")
public class AppController {

    private final AppService appService;

    public AppController (AppService appService) {
        this.appService = appService;
    }


    @GetMapping("/findWords/{storyCount}")
    public ResponseEntity findMostOccuringWordsInStories (@PathVariable int storyCount) {

        log.info("Finding the top 10 most occurring words in the titles of the last 25 stories");
        return ResponseEntity.ok(appService.findTopTenMostOccuringWordsInTitles(storyCount));
    }

    @GetMapping("/findWordsInTitlesByWeek")
    public ResponseEntity findMostOccuringWordsInTitlesLastWeek () {

        log.info("Finding the top 10 most occurring words in the titles of the post of exactly the last week");
        return ResponseEntity.ok(appService.findMostOccuringWordsInTitlesLastWeek());
    }

    @GetMapping("/findWordsByUsersKarma")
    public ResponseEntity findMostOccuringWordsByUsersKarma () {
        log.info("Finding the top 10 most occurring words in titles of the last 600 stories of users with at least 10.000 karma");
        return ResponseEntity.ok(appService.findMostOccuringWordsByUsersKarma());
    }
}
