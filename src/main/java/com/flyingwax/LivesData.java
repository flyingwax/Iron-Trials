package com.flyingwax;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LivesData
{
    private int current;
    private int total;
    private List<String> lostLives;
} 