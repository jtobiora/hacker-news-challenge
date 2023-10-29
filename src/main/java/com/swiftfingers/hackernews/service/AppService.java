package com.swiftfingers.hackernews.service;

import com.swiftfingers.hackernews.entity.NewsStory;
import com.swiftfingers.hackernews.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppService {

    private final RestTemplate restTemplate;

    @Value("${hacker-news.baseurl}")
    private String baseUrl;

    @Value("${hacker-news.newstories.url}")
    private String newStoriesUrl;

    @Value("${hacker-news.topstories.url}")
    private String topStoriesUrl;

    @Value("${hacker-news.user.url}")
    private String userUrl;

    @Value("${hacker-news.story_item.url}")
    private String itemUrl;

    private static final int KARMA_THRESHOLD = 10000;


    public AppService (RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public List<String> findTopTenMostOccuringWordsInTitles (int storyCount) {
        Integer[] storyIds = restTemplate.getForObject(newStoriesUrl, Integer[].class);

        Map<String, Integer> wordCount = new HashMap<>();

        for (int i = 0; i < storyCount; i++) {
            int storyId = storyIds[i];

            NewsStory newsStory = restTemplate.getForObject(itemUrl + storyId + ".json", NewsStory.class);

            if (Objects.nonNull(newsStory) && newsStory.getTitle() != null) {
                String[] wordsArr = newsStory.getTitle().split("\\s+");
                if (wordsArr != null) {
                    for (String word: wordsArr) {
                        wordCount.put(word, wordCount.containsKey(word) ? wordCount.get(word) + 1 : 1);
                    }
                }
            }
        }

              //sort the map by value (frequency of word count)
        return wordCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) //sort by value
                .limit(10) //limit by 10
                .map(Map.Entry::getKey) //return only the keys
                .collect(Collectors.toList());

    }


    public List<String> findMostOccuringWordsInTitlesLastWeek () {
        List<NewsStory> stories = findTopStoryByLastWeek();
        List<String> titles = null;
        if (stories != null) {
            titles = stories.stream().filter(Objects::nonNull).map(NewsStory::getTitle).collect(Collectors.toList());
        }

        Map<String, Integer> wordCount = new LinkedHashMap<>(); //to preserve order of insertion

        if (titles == null)
            throw new RuntimeException("No data was found for last week");

        for (String title : titles) {
            String[] words = title.split("\\s+"); // Split the string by whitespace
            //make sure the title has values
            if (words.length > 0) {
                for (String word : words) {
                    wordCount.put(word, wordCount.containsKey(word) ? wordCount.get(word) + 1 : 1);
                }
            }
        }

        // Sort the words by frequency count
        List<String> topWords = wordCount.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return topWords;
    }

    public List<NewsStory> findTopStoryByLastWeek() {
        List<NewsStory> storyList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Get the start of the last week
        LocalDateTime lastWeekStart = now.minus(1, ChronoUnit.WEEKS);

        int[] topStoryIds = restTemplate.getForObject(topStoriesUrl, int[].class);

        for (int i = 0; i < Math.min(10, topStoryIds.length); i++) {
            String storyUrl = itemUrl + topStoryIds[i] + ".json";

            NewsStory newsStory = restTemplate.getForObject(storyUrl, NewsStory.class);

            if (newsStory != null && Objects.nonNull(newsStory.getTitle())) {
                   //check if the story is last week
                LocalDateTime dateTime = LocalDateTime.ofInstant(newsStory.getTime(), ZoneOffset.UTC);
                if (dateTime.isAfter(lastWeekStart) && dateTime.isBefore(now)) {
                    storyList.add(newsStory);
                }
            }

        }

        return storyList;
    }


    public List<String> findMostOccuringWordsByUsersKarma () {
        // Retrieve default user profile from Hacker News API
        log.debug("Using a default hardcoded user because of permissions issues when trying to" +
                "access the list of users on the API");
        List<User> defaultUserData = User.getDefaultUserData();

        List<String> karmaIdsList = new ArrayList<>();

        if (defaultUserData != null) {
            for (User userData : defaultUserData) {
                int karma = userData.getKarma();
                if (karma >= KARMA_THRESHOLD) {
                    karmaIdsList.add(userData.getId());
                }
            }
        }

        List<String> storyTitles = getStoryTitlesFromUsers(karmaIdsList, 600);

        Map<String, Integer> wordCount = new HashMap<>();

        for (String title : storyTitles) {
           if (title != null) {
               String[] words = title.split("\\s+"); // Split by whitespace
               for (String word : words) {
                   word = word.toLowerCase(); // Convert to lowercase to ensure case insensitivity
                   wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
               }
           }
        }

        // Sort the wordCount map by value in descending order
        return wordCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10) // Select the top 10 words
                .map(Map.Entry::getKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> getStoryTitlesFromUsers(List<String> userIds, int storyCount) {

        List<String> storyTitles = new ArrayList<>();

        for (String userId : userIds) {
            String userUrl = baseUrl + "user/" + userId + ".json?print=pretty";

            // Retrieve user's submissions from Hacker News API
            Map<String, Object> userSubmissions = restTemplate.getForObject(userUrl, Map.class);

            if (userSubmissions != null) {
                List<Integer> submittedStoryIds = (List<Integer>) userSubmissions.get("submitted");
                if (submittedStoryIds != null) {
                    for (int i = 0; i < 10; i++) {
                        String storyUrl = baseUrl + "item/" + submittedStoryIds.get(i) + ".json?print=pretty";
                        log.debug("Story url and user's id {} {} ", storyUrl, userId);

                        // Retrieve story information
                        Map<String, Object> storyInfo = restTemplate.getForObject(storyUrl, Map.class);
                        log.debug("Story information ::::: {}", storyInfo);
                        if (storyInfo != null) {
                            String title = (String) storyInfo.get("title");
                            storyTitles.add(title);
                        }
                    }
                }
            }
        }

        return storyTitles;
    }
    


}
