package com.customersupport.controller;

import com.customersupport.dto.ReviewDTO;
import com.customersupport.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ReviewController.class)
@Import(ReviewControllerTest.MockConfig.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateReview() throws Exception {
        ReviewDTO reviewToCreate = new ReviewDTO();
        reviewToCreate.setTicketId(1L);
        reviewToCreate.setCustomerId(1L);
        reviewToCreate.setRating(5);
        reviewToCreate.setFeedback("Excellent!");

        ReviewDTO savedReview = new ReviewDTO();
        savedReview.setId(1L);
        savedReview.setTicketId(1L);
        savedReview.setCustomerId(1L);
        savedReview.setRating(5);
        savedReview.setFeedback("Excellent!");

        when(reviewService.createReview(any(ReviewDTO.class))).thenReturn(savedReview);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.feedback").value("Excellent!"));
    }

    @Test
    void testGetReviewById() throws Exception {
        ReviewDTO review = new ReviewDTO();
        review.setId(1L);
        review.setFeedback("Test feedback");

        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(review));

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.feedback").value("Test feedback"));
    }

    @Test
    void testGetReviewById_NotFound() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateReview() throws Exception {
        ReviewDTO payload = new ReviewDTO();
        payload.setCustomerId(1L);
        payload.setRating(4);
        payload.setFeedback("Updated feedback");

        ReviewDTO updated = new ReviewDTO();
        updated.setId(2L);
        updated.setCustomerId(1L);
        updated.setRating(4);
        updated.setFeedback("Updated feedback");

        when(reviewService.updateReview(eq(2L), any(ReviewDTO.class), eq(1L))).thenReturn(updated);

        mockMvc.perform(put("/api/reviews/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedback").value("Updated feedback"));
    }

    @Test
    void testDeleteReviewAsCustomer() throws Exception {
        doNothing().when(reviewService).deleteReviewByCustomer(3L, 1L);

        mockMvc.perform(delete("/api/reviews/3")
                        .param("customerId", "1"))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        ReviewService reviewService() {
            return Mockito.mock(ReviewService.class);
        }
    }
}