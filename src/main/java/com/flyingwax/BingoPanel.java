package com.flyingwax;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

@Slf4j
public class BingoPanel extends JPanel
{
    private final IronTrials plugin;
    private final JPanel contentPanel;

    public BingoPanel(IronTrials plugin)
    {
        this.plugin = plugin;
        
        setBorder(new EmptyBorder(10, 0, 10, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Bingo Board");
        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Content panel
        contentPanel = new JPanel();
        contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentPanel.setLayout(new BorderLayout());
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void updateData(BingoData bingoData)
    {
        contentPanel.removeAll();
        
        if (bingoData == null || bingoData.getTiles() == null)
        {
            contentPanel.add(new JLabel("No bingo data available"));
            return;
        }

        List<BingoTile> tiles = bingoData.getTiles();
        int size = bingoData.getSize();
        
        if (size <= 0)
        {
            size = (int) Math.ceil(Math.sqrt(tiles.size()));
        }

        // Create bingo grid
        JPanel gridPanel = new JPanel();
        gridPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        gridPanel.setLayout(new GridLayout(size, size, 2, 2));
        gridPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        for (int i = 0; i < size * size; i++)
        {
            if (i < tiles.size())
            {
                BingoTile tile = tiles.get(i);
                JPanel tilePanel = createTilePanel(tile);
                gridPanel.add(tilePanel);
            }
            else
            {
                // Empty tile
                JPanel emptyPanel = new JPanel();
                emptyPanel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
                emptyPanel.setBorder(BorderFactory.createLineBorder(ColorScheme.LIGHT_GRAY_COLOR));
                gridPanel.add(emptyPanel);
            }
        }

        contentPanel.add(gridPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createTilePanel(BingoTile tile)
    {
        JPanel panel = new JPanel();
        panel.setBackground(tile.isCompleted() ? Color.GREEN.darker() : ColorScheme.MEDIUM_GRAY_COLOR);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.LIGHT_GRAY_COLOR));
        panel.setLayout(new BorderLayout());

        // Tile description
        JTextArea descArea = new JTextArea(tile.getDescription());
        descArea.setFont(FontManager.getRunescapeSmallFont());
        descArea.setForeground(tile.isCompleted() ? Color.WHITE : Color.LIGHT_GRAY);
        descArea.setBackground(panel.getBackground());
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Points
        JLabel pointsLabel = new JLabel(tile.getPoints() + " pts");
        pointsLabel.setFont(FontManager.getRunescapeSmallFont());
        pointsLabel.setForeground(Color.YELLOW);
        pointsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pointsLabel.setBorder(new EmptyBorder(2, 0, 2, 0));

        // Completion info
        if (tile.isCompleted())
        {
            JLabel completedLabel = new JLabel("✓ " + tile.getCompletedBy());
            completedLabel.setFont(FontManager.getRunescapeSmallFont());
            completedLabel.setForeground(Color.GREEN);
            completedLabel.setHorizontalAlignment(SwingConstants.CENTER);
            completedLabel.setBorder(new EmptyBorder(2, 0, 2, 0));

            JPanel infoPanel = new JPanel();
            infoPanel.setBackground(panel.getBackground());
            infoPanel.setLayout(new BorderLayout());
            infoPanel.add(pointsLabel, BorderLayout.NORTH);
            infoPanel.add(completedLabel, BorderLayout.SOUTH);

            panel.add(descArea, BorderLayout.CENTER);
            panel.add(infoPanel, BorderLayout.SOUTH);
        }
        else
        {
            panel.add(descArea, BorderLayout.CENTER);
            panel.add(pointsLabel, BorderLayout.SOUTH);
        }

        return panel;
    }
} 