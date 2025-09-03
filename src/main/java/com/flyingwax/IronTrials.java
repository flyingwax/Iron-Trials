package com.flyingwax;

import com.google.inject.Provides;
import javax.inject.Inject;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.Dimension;

@Slf4j
@PluginDescriptor(
	name = "Iron Trials"
)
public class IronTrials extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private IronTrialsConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Injector injector;

	private NavigationButton navButton;
	private IronTrialsPanel mainPanel;
	private HttpClient httpClient;
	private Map<Skill, Integer> previousLevels = new HashMap<>();
	private List<GameEvent> recentEvents = new ArrayList<>();
	private static final int MAX_EVENTS = 50; // Keep last 50 events
	private MilestoneConfig milestoneConfig;
	private GroupData groupData; // Store group data for roster filtering
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void startUp() throws Exception
	{
		try
		{
			// Initialize HTTP client
			httpClient = new HttpClient();
			
			        // Load milestone configuration
        loadMilestoneConfig();
        
        // Add some test events for demonstration
        addTestEvents();
			
			// Create a simple programmatic icon
			BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = icon.createGraphics();
			g2d.setColor(Color.RED);
			g2d.fillRect(0, 0, 16, 16);
			g2d.setColor(Color.WHITE);
			g2d.drawString("IT", 2, 12);
			g2d.dispose();
			log.info("Created programmatic icon: {}x{}", icon.getWidth(), icon.getHeight());

			// Create the main panel
			mainPanel = new IronTrialsPanel(this);
			mainPanel.init();
			log.info("Main panel created and initialized");

			// Create navigation button
			navButton = NavigationButton.builder()
				.icon(icon)
				.panel(mainPanel)
				.build();
			clientToolbar.addNavigation(navButton);
			log.info("Navigation button added to toolbar");

			// Plugin startup complete
			log.info("Iron Trials plugin started successfully");
		}
		catch (Exception e)
		{
			log.error("Failed to start Iron Trials plugin: {}", e.getMessage(), e);
			throw e; // Re-throw to let RuneLite know the plugin failed to start
		}
	}

	private void loadMilestoneConfig()
	{
		try
		{
			if (config.useRemoteConfig())
			{
				// Download from remote server
				milestoneConfig = downloadRemoteConfig();
				if (milestoneConfig != null)
				{
					log.info("Successfully loaded remote milestone config");
					return;
				}
				else
				{
					log.warn("Failed to download remote config, falling back to defaults");
					milestoneConfig = MilestoneConfig.getDefault();
				}
			}
			else if (config.useExternalConfig())
			{
				String configPath = config.externalConfigPath();
				// Expand ~ to home directory
				if (configPath.startsWith("~"))
				{
					configPath = System.getProperty("user.home") + configPath.substring(1);
				}
				
				Path path = Paths.get(configPath);
				if (Files.exists(path))
				{
					String jsonContent = Files.readString(path);
					milestoneConfig = objectMapper.readValue(jsonContent, MilestoneConfig.class);
					log.info("Loaded external milestone config from: {}", configPath);
				}
				else
				{
					log.warn("External config file not found: {}. Using defaults.", configPath);
					milestoneConfig = MilestoneConfig.getDefault();
					// Create the file with defaults
					createDefaultConfigFile(path);
				}
			}
			else
			{
				// Use plugin config
				milestoneConfig = MilestoneConfig.getDefault();
				// Override with plugin settings
				String[] levels = config.milestoneLevels().split(",");
				List<Integer> levelList = new ArrayList<>();
				for (String level : levels)
				{
					try
					{
						levelList.add(Integer.parseInt(level.trim()));
					}
					catch (NumberFormatException e)
					{
						log.warn("Invalid level in config: {}", level);
					}
				}
				milestoneConfig.setLevelMilestones(levelList);
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to load milestone config, using defaults: {}", e.getMessage());
			milestoneConfig = MilestoneConfig.getDefault();
		}
	}

	private MilestoneConfig downloadRemoteConfig()
	{
		try
		{
			String groupId = config.remoteGroupId().isEmpty() ? config.groupId() : config.remoteGroupId();
			String url = config.remoteConfigUrl();
			
			// Add group ID to URL if it's a parameter
			if (url.contains("?"))
			{
				url += "&groupId=" + groupId;
			}
			else
			{
				url += "?groupId=" + groupId;
			}
			
			log.info("Downloading remote config from: {}", url);
			
			// Use the existing HTTP client to download
			String response = httpClient.downloadConfig(url);
			if (response != null && !response.isEmpty())
			{
				// Try to parse as server response format first
				try
				{
					MilestoneConfigResponse serverResponse = objectMapper.readValue(response, MilestoneConfigResponse.class);
					if (serverResponse.getConfig() != null)
					{
						log.info("Loaded remote config for group: {} (version: {})", 
							serverResponse.getGroupId(), serverResponse.getVersion());
						return serverResponse.getConfig();
					}
				}
				catch (Exception e)
				{
					log.debug("Response is not in server format, trying direct config format");
				}
				
				// Fallback to direct config format
				return objectMapper.readValue(response, MilestoneConfig.class);
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to download remote config: {}", e.getMessage());
		}
		return null;
	}

	    private void addTestEvents()
    {
        // Add some test events for demonstration
        long currentTime = System.currentTimeMillis() / 1000;
        
        GameEvent levelUpEvent = new GameEvent();
        levelUpEvent.setKind(EventKind.LEVEL_UP);
        levelUpEvent.setPlayerName("IronManPro");
        levelUpEvent.setDescription("Reached level 70 Attack");
        levelUpEvent.setPoints(70);
        levelUpEvent.setMetadata("xp:737627");
        levelUpEvent.setTimestamp(currentTime - 300); // 5 minutes ago
        addEventToFeed(levelUpEvent);
        
        GameEvent questEvent = new GameEvent();
        questEvent.setKind(EventKind.QUEST_COMPLETED);
        questEvent.setPlayerName("QuestMaster");
        questEvent.setDescription("Completed Dragon Slayer");
        questEvent.setPoints(25);
        questEvent.setMetadata("quest:dragon_slayer");
        questEvent.setTimestamp(currentTime - 600); // 10 minutes ago
        addEventToFeed(questEvent);
        
        GameEvent bossEvent = new GameEvent();
        bossEvent.setKind(EventKind.BOSS_KILL);
        bossEvent.setPlayerName("BossSlayer99");
        bossEvent.setDescription("Killed Zulrah (KC: 100)");
        bossEvent.setPoints(100);
        bossEvent.setMetadata("boss:zulrah");
        bossEvent.setTimestamp(currentTime - 900); // 15 minutes ago
        addEventToFeed(bossEvent);
        
        GameEvent dropEvent = new GameEvent();
        dropEvent.setKind(EventKind.RARE_DROP);
        dropEvent.setPlayerName("SkillerQueen");
        dropEvent.setDescription("Received Twisted Bow");
        dropEvent.setPoints(50);
        dropEvent.setMetadata("item:twisted_bow");
        dropEvent.setTimestamp(currentTime - 1200); // 20 minutes ago
        addEventToFeed(dropEvent);
        
        log.info("Added {} test events to feed", 4);
    }

    private void createDefaultConfigFile(Path path)
    {
        try
        {
            // Create parent directories if they don't exist
            Files.createDirectories(path.getParent());
            
            // Write default config
            String defaultJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(MilestoneConfig.getDefault());
            Files.writeString(path, defaultJson);
            
            log.info("Created default milestone config file: {}", path);
        }
        catch (IOException e)
        {
            log.warn("Failed to create default config file: {}", e.getMessage());
        }
    }

	@Override
	protected void shutDown() throws Exception
	{
		try
		{
			// Remove navigation button
			if (navButton != null)
			{
				clientToolbar.removeNavigation(navButton);
				navButton = null;
			}
			
			// Clear main panel
			mainPanel = null;
			
			log.info("Iron Trials plugin stopped");
		}
		catch (Exception e)
		{
			log.error("Error during plugin shutdown: {}", e.getMessage(), e);
		}
	}

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Iron Trials plugin loaded! Type ::irontrials to test", null);
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged)
    {
        // Capture milestone level-ups only
        if (config.captureLevelUps())
        {
            Skill skill = statChanged.getSkill();
            int level = statChanged.getLevel();
            int xp = statChanged.getXp();
            
            // Check if this is a level up (level increased)
            if (level > getPreviousLevel(skill))
            {
                // Only capture milestone levels
                if (isMilestoneLevel(level))
                {
                    log.info("Milestone level up detected: {} level {}", skill.getName(), level);
                    sendEvent(EventKind.LEVEL_UP, skill.getName() + " " + level, level, xp);
                }
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        String message = chatMessage.getMessage();
        log.info("Chat message received: {} (type: {})", message, chatMessage.getType());
        
        // Listen for commands
        if (message.toLowerCase().startsWith("::irontrials"))
        {
            log.info("Iron Trials command detected!");
            testPlugin();
            return;
        }

        // Capture quest completions
        if (config.captureQuests() && chatMessage.getType() == ChatMessageType.GAMEMESSAGE)
        {
            if (message.contains("Congratulations! You have completed") || 
                message.contains("You have completed") && message.contains("quest"))
            {
                String questName = extractQuestName(message);
                if (questName != null && isSignificantAchievement(questName))
                {
                    log.info("Significant quest completion detected: {}", questName);
                    sendEvent(EventKind.QUEST_COMPLETED, questName, 0, 0);
                }
            }
        }

        // Capture boss kills
        if (config.captureBossKc() && chatMessage.getType() == ChatMessageType.GAMEMESSAGE)
        {
            if (message.contains("Your kill count is:"))
            {
                String bossName = extractBossName(message);
                int killCount = extractKillCount(message);
                if (bossName != null && killCount > 0)
                {
                    log.info("Boss kill detected: {} (KC: {})", bossName, killCount);
                    sendEvent(EventKind.BOSS_KILL, bossName, killCount, 0);
                }
            }
        }

        // Capture rare drops
        if (config.captureDrops() && chatMessage.getType() == ChatMessageType.GAMEMESSAGE)
        {
            if (message.contains("You have received a") || 
                message.contains("You received") ||
                message.contains("You got"))
            {
                String itemName = extractItemName(message);
                if (itemName != null && isRareDrop(itemName))
                {
                    log.info("Rare drop detected: {}", itemName);
                    sendEvent(EventKind.RARE_DROP, itemName, 0, 0);
                }
            }
        }

        // Capture deaths
        if (message.contains("You have died") || message.contains("Oh dear, you are dead"))
        {
            log.info("Death detected");
            sendEvent(EventKind.DEATH, "Player Death", 0, 0);
        }
    }

    @Subscribe
    public void onCommandExecuted(CommandExecuted commandExecuted)
    {
        log.info("Command executed: {}", commandExecuted.getCommand());
        
        if ("irontrials".equals(commandExecuted.getCommand()))
        {
            log.info("Iron Trials command detected via CommandExecuted!");
            testPlugin();
        }
    }

    private void testPlugin()
    {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Iron Trials test: Plugin is working!", null);
        
        // Test HTTP client
        String serverUrl = config.serverUrl();
        String groupId = config.groupId();
        
        if (serverUrl.isEmpty() || groupId.isEmpty())
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Iron Trials: Please configure Server URL and Group ID", null);
            return;
        }
        
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Iron Trials: Testing connection to " + serverUrl, null);
        
        // Test API call
        httpClient.getGroupData(serverUrl, groupId).thenAccept(groupData -> {
            if (groupData != null)
            {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Iron Trials: Successfully loaded group data for " + groupData.getName(), null);
            }
            else
            {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Iron Trials: Failed to load group data", null);
            }
        });
    }

    @Provides
    IronTrialsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(IronTrialsConfig.class);
    }
    
    // Getters for other components
    public Client getClient() { return client; }
    public IronTrialsConfig getConfig() { return config; }
    public HttpClient getHttpClient() { return httpClient; }

    private Integer getPreviousLevel(Skill skill)
    {
        return previousLevels.getOrDefault(skill, 0);
    }

    private boolean isMilestoneLevel(int level)
    {
        if (milestoneConfig == null || milestoneConfig.getLevelMilestones() == null)
        {
            return false;
        }
        return milestoneConfig.getLevelMilestones().contains(level);
    }

    private boolean isSignificantAchievement(String achievement)
    {
        if (milestoneConfig == null)
        {
            return false;
        }
        
        String lowerAchievement = achievement.toLowerCase();
        
        // Check quest milestones
        if (milestoneConfig.getQuestMilestones() != null)
        {
            for (String quest : milestoneConfig.getQuestMilestones())
            {
                if (lowerAchievement.contains(quest.toLowerCase()))
                {
                    return true;
                }
            }
        }
        
        // Check achievement milestones
        if (milestoneConfig.getAchievementMilestones() != null)
        {
            for (String achievementMilestone : milestoneConfig.getAchievementMilestones())
            {
                if (lowerAchievement.contains(achievementMilestone.toLowerCase()))
                {
                    return true;
                }
            }
        }
        
        return false;
    }

    private boolean isRareDrop(String itemName)
    {
        if (milestoneConfig == null || milestoneConfig.getRareDrops() == null)
        {
            return false;
        }
        
        String lowerName = itemName.toLowerCase();
        
        for (String rareDrop : milestoneConfig.getRareDrops())
        {
            if (lowerName.contains(rareDrop.toLowerCase()))
            {
                return true;
            }
        }
        
        return false;
    }

    private boolean isSignificantBossKill(String bossName)
    {
        if (milestoneConfig == null || milestoneConfig.getBossKills() == null)
        {
            return false;
        }
        
        String lowerName = bossName.toLowerCase();
        
        for (String boss : milestoneConfig.getBossKills())
        {
            if (lowerName.contains(boss.toLowerCase()))
            {
                return true;
            }
        }
        
        return false;
    }

    private void sendEvent(EventKind kind, String name, int value, int xp)
    {
        String serverUrl = config.serverUrl();
        String groupId = config.groupId();

        if (serverUrl.isEmpty() || groupId.isEmpty())
        {
            log.warn("Cannot send event: Server URL or Group ID not configured.");
            return;
        }

        GameEvent event = new GameEvent();
        event.setKind(kind);
        event.setDescription(name);
        event.setPoints(value);
        event.setTimestamp(System.currentTimeMillis() / 1000); // Unix timestamp
        event.setMetadata("xp:" + xp); // Store XP in metadata

        // Store event locally for real-time display
        addEventToFeed(event);

        httpClient.sendEvent(serverUrl, groupId, event).thenAccept(success -> {
            if (success)
            {
                log.info("Event sent successfully: {} - {}", kind, name);
            }
            else
            {
                log.warn("Failed to send event: {} - {}", kind, name);
            }
        });
    }

    private void addEventToFeed(GameEvent event)
    {
        // Add event to the beginning of the list (most recent first)
        recentEvents.add(0, event);
        
        // Keep only the last MAX_EVENTS
        if (recentEvents.size() > MAX_EVENTS)
        {
            recentEvents = recentEvents.subList(0, MAX_EVENTS);
        }

        // Notify the UI to update
        SwingUtilities.invokeLater(() -> {
            if (mainPanel != null)
            {
                mainPanel.updateFeed(event);
            }
        });
    }

    public List<GameEvent> getRecentEvents()
    {
        return new ArrayList<>(recentEvents);
    }

    public GroupData getGroupData()
    {
        return groupData;
    }

    public void setGroupData(GroupData groupData)
    {
        this.groupData = groupData;
    }

    private String extractQuestName(String message)
    {
        // Look for quest completion patterns
        if (message.contains("Congratulations! You have completed"))
        {
            int start = message.indexOf("Congratulations! You have completed") + 33;
            int end = message.indexOf(".", start);
            if (end != -1)
            {
                return message.substring(start, end).trim();
            }
        }
        else if (message.contains("You have completed") && message.contains("quest"))
        {
            int start = message.indexOf("You have completed") + 18;
            int end = message.indexOf("quest", start);
            if (end != -1)
            {
                return message.substring(start, end).trim();
            }
        }
        return null;
    }

    private String extractBossName(String message)
    {
        // Look for boss kill count patterns
        if (message.contains("Your kill count is:"))
        {
            // This is a simplified extraction - in practice, you'd need more context
            // to determine which boss this refers to
            return "Unknown Boss"; // Placeholder
        }
        return null;
    }

    private int extractKillCount(String message)
    {
        // Extract the number from "Your kill count is: X"
        if (message.contains("Your kill count is:"))
        {
            try
            {
                String[] parts = message.split(":");
                if (parts.length > 1)
                {
                    String numberPart = parts[1].replaceAll("[^0-9]", "");
                    return Integer.parseInt(numberPart);
                }
            }
            catch (NumberFormatException e)
            {
                log.warn("Could not parse kill count from message: {}", message);
            }
        }
        return 0;
    }

    private String extractItemName(String message)
    {
        // Look for drop patterns
        if (message.contains("You have received a"))
        {
            int start = message.indexOf("You have received a") + 19;
            int end = message.indexOf(".", start);
            if (end != -1)
            {
                return message.substring(start, end).trim();
            }
        }
        else if (message.contains("You received"))
        {
            int start = message.indexOf("You received") + 12;
            int end = message.indexOf(".", start);
            if (end != -1)
            {
                return message.substring(start, end).trim();
            }
        }
        else if (message.contains("You got"))
        {
            int start = message.indexOf("You got") + 7;
            int end = message.indexOf(".", start);
            if (end != -1)
            {
                return message.substring(start, end).trim();
            }
        }
        return null;
    }
}