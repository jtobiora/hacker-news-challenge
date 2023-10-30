package com.swiftfingers.hackernews;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.swiftfingers.hackernews.controller.AppController;
import com.swiftfingers.hackernews.entity.NewsStory;
import com.swiftfingers.hackernews.service.AppService;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class HackerNewsChallengeTests {

    @Mock
    protected AppService appService;

    @Test
    public void shouldReturnOnlyLastWeekData () {
        List<NewsStory> storyList = Arrays.asList(
               NewsStory.builder()
                       .by("gorpovitch")
                       .descendants(94)
                       .id(38053586L)
                       .kids(null)
                       .score(161)
                       .text(null)
                       .time(Instant.now())
                       .type("story")
                       .build()
        );

        when(appService.findTopStoryByLastWeek()).thenReturn(storyList);

        List<NewsStory> newsStories = appService.findTopStoryByLastWeek();
        LocalDateTime lastWeekDate = LocalDateTime.now().minusWeeks(1); //last week date
        for (NewsStory story: newsStories) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(story.getTime(), ZoneOffset.UTC);
            if (dateTime.isAfter(lastWeekDate) || dateTime.isBefore(lastWeekDate)) {
                Assertions.fail("Date is not exactly last week!");
            }
        }

        Assertions.assertEquals(newsStories.size(), storyList.size());
    }
}
