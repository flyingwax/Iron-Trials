package com.flyingwax;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameEvent
{
    private String id;
    private String playerName;
    private EventKind kind;
    private String description;
    private long timestamp;
    private int points;
    private String metadata;
} 