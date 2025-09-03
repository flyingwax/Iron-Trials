package com.flyingwax;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class IronTrialsPanel extends PluginPanel
{
    private final IronTrials plugin;
    private final JPanel display = new JPanel();
    private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);
    private final RosterPanel rosterPanel;
    private final FeedPanel feedPanel;
    private final BingoPanel bingoPanel;

    @Inject
    public IronTrialsPanel(IronTrials plugin)
    {
        super(true); // Use the proper wrapper to ensure panel opens
        this.plugin = plugin;
        
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Create panels
        rosterPanel = new RosterPanel(plugin);
        feedPanel = new FeedPanel(plugin);
        bingoPanel = new BingoPanel(plugin);

        // Setup display panel
        display.setBorder(new EmptyBorder(5, 5, 5, 5));
        display.setBackground(ColorScheme.DARK_GRAY_COLOR);
        display.setLayout(new BorderLayout());

        // Setup tab group
        tabGroup.setLayout(new GridLayout(0, 3, 7, 7));
        tabGroup.setBorder(new EmptyBorder(5, 5, 0, 5));

        add(tabGroup, BorderLayout.NORTH);
        add(display, BorderLayout.CENTER);

        // Add tabs
        addTab("Roster", "roster.png", rosterPanel);
        addTab("Feed", "feed.png", feedPanel);
        addTab("Bingo", "bingo.png", bingoPanel);

        // Add refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> refreshData());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
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

    private void addTab(String name, String iconName, JPanel panel)
    {
        // Load icon from resources
        BufferedImage iconImage = ImageUtil.loadImageResource(getClass(), iconName);
        MaterialTab materialTab = new MaterialTab(new ImageIcon(iconImage), tabGroup, panel);
        materialTab.setPreferredSize(new Dimension(30, 27));
        materialTab.setName(name);
        materialTab.setToolTipText(name);

        materialTab.setOnSelectEvent(() ->
        {
            // Update the panel when selected
            SwingUtilities.invokeLater(panel::revalidate);
            return true;
        });

        tabGroup.addTab(materialTab);
    }

    public void init()
    {
        // Initial data load
        refreshData();
    }

    public void updateFeed(GameEvent newEvent)
    {
        SwingUtilities.invokeLater(() -> {
            feedPanel.addEvent(newEvent);
        });
    }

    private void refreshData()
    {
        CompletableFuture.runAsync(() -> {
            try
            {
                log.info("Refreshing data...");
                
                // Get group data
                GroupData groupData = plugin.getHttpClient().getGroupData(plugin.getConfig().serverUrl(), plugin.getConfig().groupId()).get();
                if (groupData != null)
                {
                    SwingUtilities.invokeLater(() -> {
                        rosterPanel.updateData(groupData);
                        feedPanel.updateData(groupData);
                        log.info("Data refreshed successfully");
                    });
                }
                else
                {
                    log.warn("Failed to get group data");
                }
            }
            catch (Exception e)
            {
                log.error("Error refreshing data", e);
            }
        });
    }
} 