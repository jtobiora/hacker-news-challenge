package com.swiftfingers.hackernews.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsStory {

    private String by;
    private Integer descendants;
    private Long id;
    private Integer score;
    private String text;
    private Instant time;
    private String title;
    private String type;
    private List<Integer> kids;
}
