package com.flyingwax;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupData
{
    private String id;
    private String name;
    private List<PlayerData> players;
    private SeasonData currentSeason;
    private List<GameEvent> recentEvents;
} 