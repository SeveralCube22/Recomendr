package com.vmanam.recomendr.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEvent {
    private String userId;
    private String movieName;
    private String releaseDate;
    private float eventWeight;
}
