package com.flyingwax;

import net.runelite.client.config.*;

@ConfigGroup("irontrials")
public interface IronTrialsConfig extends Config
{
    @ConfigSection(
        name = "Server Configuration",
        description = "Server connection settings",
        position = 1
    )
    String serverSection = "server";

    @ConfigItem(
        keyName = "serverUrl",
        name = "Server URL",
        description = "The URL of the Iron Trials server",
        section = serverSection,
        position = 1
    )
    default String serverUrl()
    {
        return "http://localhost:8080";
    }

    @ConfigItem(
        keyName = "apiKey",
        name = "API Key",
        description = "Your HMAC API key for authentication",
        section = serverSection,
        position = 2,
        secret = true
    )
    default String apiKey()
    {
        return "";
    }

    @ConfigItem(
        keyName = "keyId",
        name = "Key ID",
        description = "Your API key ID",
        section = serverSection,
        position = 3
    )
    default String keyId()
    {
        return "";
    }

    @ConfigSection(
        name = "Group Settings",
        description = "Group and player settings",
        position = 2
    )
    String groupSection = "group";

    @ConfigItem(
        keyName = "groupId",
        name = "Group ID",
        description = "Your Ironman group ID",
        section = groupSection,
        position = 1
    )
    default String groupId()
    {
        return "test-group";
    }

    @ConfigItem(
        keyName = "playerName",
        name = "Player Name",
        description = "Your in-game player name",
        section = groupSection,
        position = 2
    )
    default String playerName()
    {
        return "";
    }

    @ConfigSection(
        name = "Event Settings",
        description = "Event capture settings",
        position = 3
    )
    String eventSection = "events";

    @ConfigItem(
        keyName = "captureLevelUps",
        name = "Capture Level Ups",
        description = "Automatically capture level up events",
        section = eventSection,
        position = 1
    )
    default boolean captureLevelUps()
    {
        return true;
    }

    @ConfigItem(
        keyName = "milestoneLevels",
        name = "Milestone Levels",
        description = "Comma-separated list of milestone levels to track (e.g., 50,70,80,90,99)",
        section = eventSection,
        position = 2
    )
    default String milestoneLevels()
    {
        return "50,70,80,90,99";
    }

    @ConfigItem(
        keyName = "useExternalConfig",
        name = "Use External Config",
        description = "Load milestones from external file instead of plugin settings",
        section = eventSection,
        position = 3
    )
    default boolean useExternalConfig()
    {
        return false;
    }

    @ConfigItem(
        keyName = "externalConfigPath",
        name = "External Config Path",
        description = "Path to external milestones configuration file",
        section = eventSection,
        position = 4
    )
    default String externalConfigPath()
    {
        return "~/.runelite/iron-trials-milestones.json";
    }

    @ConfigItem(
        keyName = "useRemoteConfig",
        name = "Use Remote Config",
        description = "Download milestone configuration from remote server",
        section = eventSection,
        position = 5
    )
    default boolean useRemoteConfig()
    {
        return true;
    }

    @ConfigItem(
        keyName = "remoteConfigUrl",
        name = "Remote Config URL",
        description = "URL to download milestone configuration from",
        section = eventSection,
        position = 6
    )
    default String remoteConfigUrl()
    {
        return "http://localhost:8080/v1/milestones";
    }

    @ConfigItem(
        keyName = "remoteGroupId",
        name = "Remote Group ID",
        description = "Group ID for remote configuration (if different from main group ID)",
        section = eventSection,
        position = 7
    )
    default String remoteGroupId()
    {
        return "test-group";
    }

    @ConfigItem(
        keyName = "captureQuests",
        name = "Capture Quests",
        description = "Automatically capture quest completion events",
        section = eventSection,
        position = 3
    )
    default boolean captureQuests()
    {
        return true;
    }

    @ConfigItem(
        keyName = "captureBossKc",
        name = "Capture Boss KC",
        description = "Automatically capture boss kill count events",
        section = eventSection,
        position = 3
    )
    default boolean captureBossKc()
    {
        return true;
    }

    @ConfigItem(
        keyName = "captureDrops",
        name = "Capture Drops",
        description = "Automatically capture rare drop events",
        section = eventSection,
        position = 4
    )
    default boolean captureDrops()
    {
        return true;
    }

    @ConfigSection(
        name = "Display Settings",
        description = "UI display settings",
        position = 4
    )
    String displaySection = "display";

    @ConfigItem(
        keyName = "feedMaxItems",
        name = "Feed Max Items",
        description = "Maximum number of items to show in the feed",
        section = displaySection,
        position = 1
    )
    default int feedMaxItems()
    {
        return 20;
    }

    @ConfigItem(
        keyName = "refreshInterval",
        name = "Refresh Interval",
        description = "How often to refresh data (in seconds)",
        section = displaySection,
        position = 2
    )
    default int refreshInterval()
    {
        return 60;
    }

    @ConfigItem(
        keyName = "showNotifications",
        name = "Show Notifications",
        description = "Show desktop notifications for events",
        section = displaySection,
        position = 3
    )
    default boolean showNotifications()
    {
        return true;
    }
} 