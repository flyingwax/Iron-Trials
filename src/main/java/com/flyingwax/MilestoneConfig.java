package com.flyingwax;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MilestoneConfig
{
    private List<Integer> levelMilestones;
    private List<String> questMilestones;
    private List<String> achievementMilestones;
    private List<String> rareDrops;
    private List<String> bossKills;
    private Map<String, Integer> customMilestones;
    
    // Default constructor with sensible defaults
    public static MilestoneConfig getDefault()
    {
        MilestoneConfig config = new MilestoneConfig();
        config.setLevelMilestones(List.of(50, 70, 80, 90, 99));
        config.setQuestMilestones(List.of(
            "Quest Point Cape",
            "Achievement Diary Cape",
            "Fire Cape",
            "Infernal Cape",
            "Max Cape"
        ));
        config.setAchievementMilestones(List.of(
            "Achievement Diary",
            "Diary Cape",
            "Max Cape"
        ));
        config.setRareDrops(List.of(
            "bandos", "armadyl", "saradomin", "zamorak", "guthix",
            "abyssal", "dragon warhammer", "twisted bow", "scythe", "rapier", "blade",
            "dragon axe", "dragon pickaxe", "dragon harpoon",
            "pet", "baby"
        ));
        config.setBossKills(List.of(
            "Kraken", "Zulrah", "Vorkath", "Hydra", "Gauntlet", "Corrupted Gauntlet"
        ));
        config.setCustomMilestones(Map.of(
            "First Fire Cape", 100,
            "Quest Cape", 200,
            "Max Cape", 500
        ));
        return config;
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class MilestoneConfigResponse
{
    private String groupId;
    private String version;
    private String lastUpdated;
    private MilestoneConfig config;
} 