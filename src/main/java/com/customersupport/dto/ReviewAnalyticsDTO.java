package com.customersupport.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewAnalyticsDTO {
  private Double averageRating = 0.0;
  private Double serviceQualityAverage = 0.0;
  private Double responseTimeAverage = 0.0;
  private Double professionalismAverage = 0.0;
  private Double recommendationRate = 0.0;
  private Long totalReviews = 0L;
  private Map<Integer, Long> ratingDistribution = new HashMap<>();
  private List<ReviewDTO> recentReviews = new ArrayList<>();
}
