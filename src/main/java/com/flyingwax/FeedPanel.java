package com.flyingwax;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class FeedPanel extends JPanel
{
    private final IronTrials plugin;
    private final JPanel contentPanel;
    private final List<GameEvent> events = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MMM dd, HH:mm");
    private static final int MAX_DISPLAY_EVENTS = 20;

    public FeedPanel(IronTrials plugin)
    {
        this.plugin = plugin;
        
        setBorder(new EmptyBorder(10, 0, 10, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Live Feed");
        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Content panel
        contentPanel = new JPanel();
        contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        add(scrollPane, BorderLayout.CENTER);

        // Load initial events
        loadEvents();
    }

    public void addEvent(GameEvent event)
    {
        SwingUtilities.invokeLater(() -> {
            events.add(0, event); // Add to beginning
            
            // Keep only the most recent events
            if (events.size() > MAX_DISPLAY_EVENTS)
            {
                events.remove(events.size() - 1);
            }
            
            updateDisplay();
        });
    }

    public void updateData(GroupData groupData)
    {
        // This method is called from the main panel refresh
        // We'll keep the existing events but could add group-wide events here
        loadEvents();
    }

    private void loadEvents()
    {
        // Load events from the plugin
        List<GameEvent> pluginEvents = plugin.getRecentEvents();
        events.clear();
        events.addAll(pluginEvents);
        updateDisplay();
    }

    private void updateDisplay()
    {
        contentPanel.removeAll();
        
        if (events.isEmpty())
        {
            JLabel noEventsLabel = new JLabel("No events yet");
            noEventsLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
            noEventsLabel.setFont(FontManager.getRunescapeSmallFont());
            noEventsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(noEventsLabel);
        }
        else
        {
            for (GameEvent event : events)
            {
                JPanel eventPanel = createEventPanel(event);
                contentPanel.add(eventPanel);
                contentPanel.add(Box.createVerticalStrut(5));
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createEventPanel(GameEvent event)
    {
        JPanel panel = new JPanel();
        panel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.setLayout(new BorderLayout());

        // Player name and timestamp header
        String playerName = event.getPlayerName();
        JLabel nameLabel = new JLabel(playerName != null ? playerName : "Unknown Player");
        nameLabel.setFont(FontManager.getRunescapeFont());
        nameLabel.setForeground(playerName != null ? getPlayerColor(playerName) : Color.WHITE);
        
        JLabel timeLabel = new JLabel(formatTimestamp(event.getTimestamp()));
        timeLabel.setFont(FontManager.getRunescapeSmallFont());
        timeLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(nameLabel, BorderLayout.WEST);
        headerPanel.add(timeLabel, BorderLayout.EAST);

        // Event description and details
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Event description
        JLabel descriptionLabel = new JLabel(event.getDescription());
        descriptionLabel.setFont(FontManager.getRunescapeFont());
        descriptionLabel.setForeground(Color.WHITE);

        // Event details (points, XP, etc.)
        JLabel detailsLabel = new JLabel(getEventDetails(event));
        detailsLabel.setFont(FontManager.getRunescapeSmallFont());
        detailsLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        contentPanel.add(descriptionLabel);
        contentPanel.add(detailsLabel);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.SOUTH);

        return panel;
    }

    private String getEventIcon(EventKind kind)
    {
        switch (kind)
        {
            case LEVEL_UP: return "📈";
            case QUEST_COMPLETED: return "📜";
            case BOSS_KILL: return "⚔️";
            case RARE_DROP: return "💎";
            case DEATH: return "💀";
            default: return "📝";
        }
    }

    private Color getEventColor(EventKind kind)
    {
        switch (kind)
        {
            case LEVEL_UP: return new Color(76, 175, 80); // Green
            case QUEST_COMPLETED: return new Color(33, 150, 243); // Blue
            case BOSS_KILL: return new Color(255, 152, 0); // Orange
            case RARE_DROP: return new Color(156, 39, 176); // Purple
            case DEATH: return new Color(244, 67, 54); // Red
            default: return Color.WHITE;
        }
    }

    private Color getPlayerColor(String playerName)
    {
        // Assign consistent colors to players based on their name
        int hash = playerName.hashCode();
        switch (Math.abs(hash) % 6)
        {
            case 0: return new Color(255, 87, 34); // Orange
            case 1: return new Color(156, 39, 176); // Purple
            case 2: return new Color(76, 175, 80); // Green
            case 3: return new Color(33, 150, 243); // Blue
            case 4: return new Color(255, 193, 7); // Yellow
            case 5: return new Color(233, 30, 99); // Pink
            default: return Color.WHITE;
        }
    }

    private String getEventDetails(GameEvent event)
    {
        switch (event.getKind())
        {
            case LEVEL_UP:
                String xpStr = event.getMetadata() != null && event.getMetadata().startsWith("xp:") 
                    ? event.getMetadata().substring(3) : "0";
                return "Level " + event.getPoints() + " • " + formatXP(Integer.parseInt(xpStr)) + " XP";
            case QUEST_COMPLETED:
                return "Quest completed";
            case BOSS_KILL:
                return "Kill count: " + event.getPoints();
            case RARE_DROP:
                return "Rare drop!";
            case DEATH:
                return "Player died";
            default:
                return "";
        }
    }

    private String formatXP(int xp)
    {
        if (xp >= 1000000)
        {
            return String.format("%.1fM", xp / 1000000.0);
        }
        else if (xp >= 1000)
        {
            return String.format("%.1fK", xp / 1000.0);
        }
        else
        {
            return String.valueOf(xp);
        }
    }

    private String formatTimestamp(long timestamp)
    {
        Date date = new Date(timestamp * 1000);
        return timeFormat.format(date);
    }
} 