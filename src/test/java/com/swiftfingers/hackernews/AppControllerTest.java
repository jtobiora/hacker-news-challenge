package com.swiftfingers.hackernews;

import com.swiftfingers.hackernews.controller.AppController;
import com.swiftfingers.hackernews.service.AppService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
@WebMvcTest(AppController.class)
public class AppControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    private AppService appService;

    @Test
    public void shouldReturnSizeofMostOccuringWordsInTitles() throws Exception {
        when(appService.findTopTenMostOccuringWordsInTitles(anyInt()))
                .thenReturn(Arrays.asList("of","review"));

        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/api/findWords/25"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2));
    }

}
