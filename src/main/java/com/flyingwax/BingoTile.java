package com.flyingwax;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BingoTile
{
    private int x;
    private int y;
    private String description;
    private int points;
    private boolean completed;
    private String completedBy;
    private long completedAt;
} 