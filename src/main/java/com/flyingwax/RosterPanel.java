package com.flyingwax;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class RosterPanel extends PluginPanel
{
    private final IronTrials plugin;
    private JPanel mainPanel;
    private JPanel rosterView;
    private JPanel playerDetailView;
    private JPanel contentPanel;
    private JPanel healthPanel; // Store reference to health panel
    private List<PlayerData> players = new ArrayList<>();
    private PlayerData selectedPlayer;
    
    public RosterPanel(IronTrials plugin)
    {
        super(true);
        this.plugin = plugin;
        
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Initialize views
        createRosterView();
        createPlayerDetailView();
        
        // Start with roster view
        showRosterView();
    }
    
    private void createRosterView()
    {
        rosterView = new JPanel();
        rosterView.setLayout(new BorderLayout());
        rosterView.setBackground(ColorScheme.DARK_GRAY_COLOR);
        
        // Cool Squad title with HCIM helmet icons on both sides
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
        titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        titlePanel.setBorder(new EmptyBorder(10, 0, 15, 0));
        
        // Load HCIM helmet icon
        try {
            BufferedImage hcimIcon = ImageUtil.loadImageResource(getClass(), "hardcore_ironman.png");
            if (hcimIcon != null) {
                // Resize icon to appropriate size for title
                BufferedImage resizedIcon = ImageUtil.resizeImage(hcimIcon, 20, 20);
                ImageIcon icon = new ImageIcon(resizedIcon);
                
                // Left icon
                JLabel leftIcon = new JLabel(icon);
                leftIcon.setBackground(ColorScheme.DARK_GRAY_COLOR);
                
                // Text label
                JLabel textLabel = new JLabel("SQUAD LEADERBOARD");
                textLabel.setFont(FontManager.getRunescapeBoldFont());
                textLabel.setForeground(new Color(255, 215, 0));
                textLabel.setBackground(ColorScheme.DARK_GRAY_COLOR);
                
                // Right icon
                JLabel rightIcon = new JLabel(icon);
                rightIcon.setBackground(ColorScheme.DARK_GRAY_COLOR);
                
                // Add components to title panel
                titlePanel.add(leftIcon);
                titlePanel.add(textLabel);
                titlePanel.add(rightIcon);
            } else {
                // Fallback if icon loading fails
                JLabel fallbackTitle = new JLabel("SQUAD");
                fallbackTitle.setFont(FontManager.getRunescapeBoldFont());
                fallbackTitle.setForeground(new Color(255, 215, 0));
                fallbackTitle.setHorizontalAlignment(SwingConstants.CENTER);
                titlePanel.add(fallbackTitle);
            }
        } catch (Exception e) {
            log.warn("Could not load HCIM icon", e);
            // Fallback if icon loading fails
            JLabel fallbackTitle = new JLabel("SQUAD");
            fallbackTitle.setFont(FontManager.getRunescapeBoldFont());
            fallbackTitle.setForeground(new Color(255, 215, 0));
            fallbackTitle.setHorizontalAlignment(SwingConstants.CENTER);
            titlePanel.add(fallbackTitle);
        }
        
        // Add health bar at the very top
        healthPanel = createHealthPanel();
        rosterView.add(healthPanel, BorderLayout.NORTH);
        
        // Add title panel below health
        rosterView.add(titlePanel, BorderLayout.CENTER);
        
        // Content panel for player buttons
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        
        // Add content panel directly without scroll pane
        rosterView.add(contentPanel, BorderLayout.SOUTH);
        
        // No refresh button needed - handled by main plugin panel
    }
    
    private void createPlayerDetailView()
    {
        playerDetailView = new JPanel();
        playerDetailView.setLayout(new BorderLayout());
        playerDetailView.setBackground(ColorScheme.DARK_GRAY_COLOR);
        
        // Player name header (centered, no back button)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        headerPanel.setBorder(new EmptyBorder(10, 10, 15, 10));
        
        JLabel playerNameLabel = new JLabel();
        playerNameLabel.setFont(FontManager.getRunescapeBoldFont());
        playerNameLabel.setForeground(new Color(255, 215, 0));
        playerNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        headerPanel.add(playerNameLabel, BorderLayout.CENTER);
        
        playerDetailView.add(headerPanel, BorderLayout.NORTH);
        
        // Player details content
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        detailsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Stats section
        JPanel statsPanel = createStatsPanel();
        detailsPanel.add(statsPanel);
        
        // Add some spacing
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Recent achievements section
        JPanel achievementsPanel = createAchievementsPanel();
        detailsPanel.add(achievementsPanel);
        
        JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);
        detailsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailsScrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        detailsScrollPane.setBorder(null);
        
        playerDetailView.add(detailsScrollPane, BorderLayout.CENTER);
        
        // Back button at bottom
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton backButton = createSlayerAssistantButton("Back to Squad");
        backButton.addActionListener(e -> {
            // Reset button to normal state immediately after click
            backButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            backButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            showRosterView();
        });
        
        bottomPanel.add(backButton, BorderLayout.CENTER);
        
        playerDetailView.add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createStatsPanel()
    {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        statsPanel.setPreferredSize(new Dimension(225, 120));
        statsPanel.setMaximumSize(new Dimension(225, 120));
        
        // Title
        JLabel statsTitle = new JLabel("Player Statistics");
        statsTitle.setFont(FontManager.getRunescapeBoldFont());
        statsTitle.setForeground(Color.WHITE);
        statsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsTitle.setBorder(new EmptyBorder(0, 8, 10, 8));
        statsPanel.add(statsTitle);
        
        // Stats in vertical layout to prevent truncation
        JPanel statsContainer = new JPanel();
        statsContainer.setLayout(new BoxLayout(statsContainer, BoxLayout.Y_AXIS));
        statsContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statsContainer.setBorder(new EmptyBorder(0, 8, 0, 8));
        
        // Points
        JLabel pointsLabel = new JLabel("Points: " + (selectedPlayer != null ? selectedPlayer.getPoints() : "0"));
        pointsLabel.setFont(FontManager.getRunescapeFont());
        pointsLabel.setForeground(new Color(255, 215, 0));
        pointsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pointsLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
        
        // Total Level
        JLabel levelLabel = new JLabel("Total Level: " + (selectedPlayer != null ? selectedPlayer.getTotalLevel() : "0"));
        levelLabel.setFont(FontManager.getRunescapeFont());
        levelLabel.setForeground(Color.LIGHT_GRAY);
        levelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        levelLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
        
        // Quest Points
        JLabel questLabel = new JLabel("Quest Points: " + (selectedPlayer != null ? selectedPlayer.getQuestPoints() : "0"));
        questLabel.setFont(FontManager.getRunescapeFont());
        questLabel.setForeground(Color.LIGHT_GRAY);
        questLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        questLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
        
        // Status
        String statusText = "Status: ";
        if (selectedPlayer != null) {
            statusText += selectedPlayer.isHc() ? "Hardcore Ironman" : "Regular Ironman";
            if ("dead".equals(selectedPlayer.getStatus())) {
                statusText += " (Dead)";
            }
        } else {
            statusText += "Unknown";
        }
        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(FontManager.getRunescapeFont());
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
        
        statsContainer.add(pointsLabel);
        statsContainer.add(levelLabel);
        statsContainer.add(questLabel);
        statsContainer.add(statusLabel);
        
        statsPanel.add(statsContainer);
        
        return statsPanel;
    }
    
    private JPanel createAchievementsPanel()
    {
        JPanel achievementsPanel = new JPanel();
        achievementsPanel.setLayout(new BoxLayout(achievementsPanel, BoxLayout.Y_AXIS));
        achievementsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        achievementsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        achievementsPanel.setPreferredSize(new Dimension(225, 150));
        achievementsPanel.setMaximumSize(new Dimension(225, 150));
        
        // Title
        JLabel achievementsTitle = new JLabel("Recent Achievements");
        achievementsTitle.setFont(FontManager.getRunescapeBoldFont());
        achievementsTitle.setForeground(Color.WHITE);
        achievementsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        achievementsTitle.setBorder(new EmptyBorder(0, 8, 10, 8));
        achievementsPanel.add(achievementsTitle);
        
        // Container for achievements with padding
        JPanel achievementsContainer = new JPanel();
        achievementsContainer.setLayout(new BoxLayout(achievementsContainer, BoxLayout.Y_AXIS));
        achievementsContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        achievementsContainer.setBorder(new EmptyBorder(0, 8, 0, 8));
        
        // Sample achievements (we'll populate this with real data later)
        String[] sampleAchievements = {
            "Reached 99 Attack",
            "Defeated Zulrah (50 KC)",
            "Obtained Dragon Warhammer",
            "Completed Dragon Slayer II",
            "Achieved 2000 Total Level"
        };
        
        for (String achievement : sampleAchievements) {
            JLabel achievementLabel = new JLabel(achievement);
            achievementLabel.setFont(FontManager.getRunescapeFont());
            achievementLabel.setForeground(Color.LIGHT_GRAY);
            achievementLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            achievementLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
            achievementsContainer.add(achievementLabel);
        }
        
        achievementsPanel.add(achievementsContainer);
        
        return achievementsPanel;
    }
    
    private void showRosterView()
    {
        removeAll();
        add(rosterView, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    private void showPlayerDetailView(PlayerData player)
    {
        selectedPlayer = player;
        
        // Update player name in header
        JPanel headerPanel = (JPanel) playerDetailView.getComponent(0);
        JLabel playerNameLabel = (JLabel) headerPanel.getComponent(0); // Changed to getComponent(0)
        playerNameLabel.setText(player.getName());
        
        // Update stats panel with player data
        updateStatsPanel(player);
        
        removeAll();
        add(playerDetailView, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    private void updateStatsPanel(PlayerData player)
    {
        // Find and update the stats panel
        JScrollPane scrollPane = (JScrollPane) playerDetailView.getComponent(1);
        JPanel detailsPanel = (JPanel) scrollPane.getViewport().getView();
        
        // Remove old stats panel
        detailsPanel.removeAll();
        
        // Add updated stats panel
        JPanel statsPanel = createStatsPanel();
        detailsPanel.add(statsPanel);
        
        // Add achievements panel
        JPanel achievementsPanel = createAchievementsPanel();
        detailsPanel.add(achievementsPanel);
        
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    // Override to remove fixed width constraint
    @Override
    public Dimension getPreferredSize()
    {
        // Allow the panel to expand to use available width
        return new Dimension(0, super.getPreferredSize().height);
    }

    @Override
    public Dimension getMinimumSize()
    {
        // Allow the panel to expand to use available width
        return new Dimension(0, super.getMinimumSize().height);
    }

    private void refreshData()
    {
        try
        {
            GroupData groupData = plugin.getHttpClient().getGroupData(plugin.getConfig().serverUrl(), plugin.getConfig().remoteGroupId()).get();
            if (groupData != null && groupData.getPlayers() != null)
            {
                players = groupData.getPlayers();
                updateList(players);
            }
        }
        catch (Exception e)
        {
            log.error("Error refreshing data", e);
        }
    }
    
    private JButton createSlayerAssistantButton(String text)
    {
        JButton button = new JButton(text);
        button.setFont(FontManager.getRunescapeFont());
        button.setForeground(Color.WHITE);
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 30));
        
        button.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                button.setBackground(new Color(70, 70, 70));
                button.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120), 1));
            }
            
            @Override
            public void mouseExited(MouseEvent e)
            {
                button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            }
        });
        
        return button;
    }

    private void goBack()
    {
        // Go back to main panel or previous view
        // For now, this method is kept for future functionality
    }

    // filterPlayers() method is removed

    public void updateData(GroupData groupData)
    {
        if (groupData == null || groupData.getPlayers() == null)
        {
            log.warn("GroupData or players is null");
            return;
        }

        log.info("Updating roster with {} players", groupData.getPlayers().size());
        
        // Store the group data in the plugin for filtering
        plugin.setGroupData(groupData);
        
        // Update the health panel with new lives data
        updateHealthPanel();
        
        // Update the list with all players
        List<PlayerData> sortedPlayers = groupData.getPlayers().stream()
            .sorted(Comparator.comparing(PlayerData::getPoints).reversed())
            .collect(Collectors.toList());
        
        updateList(sortedPlayers);
        log.info("Roster panel updated successfully");
    }

    private void updateList(List<PlayerData> players)
    {
        contentPanel.removeAll();
        
        // Sort players by points (highest first)
        List<PlayerData> sortedPlayers = players.stream()
            .sorted(Comparator.comparing(PlayerData::getPoints).reversed())
            .collect(Collectors.toList());
        
        for (int i = 0; i < sortedPlayers.size(); i++)
        {
            PlayerData player = sortedPlayers.get(i);
            int rank = i + 1; // Leaderboard position (1-based)
            
            // Create player button with rank
            JButton playerButton = createPlayerButton(player, rank);
            contentPanel.add(playerButton);
            
            // Add spacing between buttons
            if (i < sortedPlayers.size() - 1)
            {
                contentPanel.add(Box.createVerticalStrut(5));
            }
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private JButton createPlayerButton(PlayerData player, int rank)
    {
        String displayText = rank + ". " + player.getName() + " (" + player.getPoints() + ")";
        JButton button = new JButton(displayText);
        
        button.setFont(FontManager.getRunescapeFont());
        button.setForeground(Color.WHITE);
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            new EmptyBorder(0, 8, 0, 8)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setPreferredSize(new Dimension(220, 28));
        button.setMaximumSize(new Dimension(220, 28));
        button.setMinimumSize(new Dimension(0, 28));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        
        // Hover effects
        button.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                button.setBackground(new Color(70, 70, 70));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(120, 120, 120), 1),
                    new EmptyBorder(0, 8, 0, 8)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e)
            {
                button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 1),
                    new EmptyBorder(0, 8, 0, 8)
                ));
            }
        });
        
        // Click action - switch to player detail view and reset hover state
        button.addActionListener(e -> {
            // Reset button to normal state immediately after click
            button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                new EmptyBorder(0, 8, 0, 8)
            ));
            showPlayerDetailView(player);
        });
        
        return button;
    }
    
    private JPanel createHealthPanel()
    {
        JPanel healthPanel = new JPanel();
        healthPanel.setLayout(new BoxLayout(healthPanel, BoxLayout.Y_AXIS));
        healthPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        healthPanel.setBorder(new EmptyBorder(10, 0, 15, 0));
        
        // Health title with hearts
        JPanel healthTitlePanel = new JPanel();
        healthTitlePanel.setLayout(new BorderLayout());
        healthTitlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        
        // Center panel for the title and hearts
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        centerPanel.setBorder(new EmptyBorder(0, 10, 0, 0)); // Add left margin to center the title
        
        // Left heart
        JLabel leftHeart = new JLabel("❤️");
        leftHeart.setFont(new Font("Arial", Font.BOLD, 16));
        leftHeart.setForeground(new Color(255, 50, 50));
        
        // Health title
        JLabel healthTitle = new JLabel("SQUAD HEALTH");
        healthTitle.setFont(FontManager.getRunescapeBoldFont());
        healthTitle.setForeground(new Color(255, 215, 0));
        
        // Right heart
        JLabel rightHeart = new JLabel("❤️");
        rightHeart.setFont(new Font("Arial", Font.BOLD, 16));
        rightHeart.setForeground(new Color(255, 50, 50));
        rightHeart.setBorder(new EmptyBorder(0, 16, 0, 0)); // Add more left padding to match left heart spacing
        
        centerPanel.add(leftHeart);
        centerPanel.add(healthTitle);
        centerPanel.add(rightHeart);
        
        healthTitlePanel.add(centerPanel, BorderLayout.CENTER);
        
        healthPanel.add(healthTitlePanel);
        
        // Add spacing between title and health bar
        healthPanel.add(Box.createVerticalStrut(15));
        
        // Health bar
        JPanel healthBarPanel = new JPanel();
        healthBarPanel.setLayout(new BorderLayout());
        healthBarPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        healthBarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Calculate squad health based on lives
        int currentLives = 4; // Default fallback
        int totalLives = 4;   // Default fallback
        
        if (plugin != null && plugin.getGroupData() != null && plugin.getGroupData().getLives() != null) {
            currentLives = plugin.getGroupData().getLives().getCurrent();
            totalLives = plugin.getGroupData().getLives().getTotal();
        }
        
        int healthPercentage = totalLives > 0 ? (currentLives * 100) / totalLives : 100;
        
        // Health bar
        JProgressBar healthBar = new JProgressBar(0, 100);
        healthBar.setValue(healthPercentage);
        healthBar.setStringPainted(true);
        healthBar.setString(currentLives + "/" + totalLives);
        healthBar.setFont(FontManager.getRunescapeBoldFont());
        healthBar.setForeground(new Color(255, 215, 0)); // Golden color like titles
        
        // Set health bar color based on percentage
        if (healthPercentage >= 75) {
            healthBar.setBackground(new Color(50, 50, 50));
            healthBar.setForeground(new Color(0, 255, 0));
        } else if (healthPercentage >= 50) {
            healthBar.setBackground(new Color(50, 50, 50));
            healthBar.setForeground(new Color(255, 255, 0));
        } else {
            healthBar.setBackground(new Color(50, 50, 50));
            healthBar.setForeground(new Color(255, 50, 50));
        }
        
        healthBarPanel.add(healthBar, BorderLayout.CENTER);
        
        healthPanel.add(healthBarPanel);
        
        return healthPanel;
    }
    
    private void updateHealthPanel()
    {
        if (rosterView == null || healthPanel == null) return;
        
        // Remove the old health panel
        rosterView.remove(healthPanel);
        
        // Create and add the new health panel
        healthPanel = createHealthPanel();
        rosterView.add(healthPanel, BorderLayout.NORTH);
        
        // Revalidate and repaint
        rosterView.revalidate();
        rosterView.repaint();
    }
} 