package com.flyingwax;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerData
{
    private String name;
    private int points;
    @JsonProperty("isHc")
    private boolean isHc;
    private String status;
    private int totalLevel;
    private int questPoints;
} 