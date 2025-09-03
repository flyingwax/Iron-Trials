package com.flyingwax;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

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
        JLabel titleLabel = new JLabel("Bingo Boards");
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
    }

    public void updateData(List<BingoData> bingoBoards)
    {
        log.info("Updating bingo panel with {} boards", bingoBoards != null ? bingoBoards.size() : 0);
        contentPanel.removeAll();
        
        if (bingoBoards == null || bingoBoards.isEmpty())
        {
            log.warn("No bingo data available");
            contentPanel.add(new JLabel("No bingo data available"));
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        // Get the logged-in player's name
        String loggedInPlayer = plugin.getConfig().playerName();
        
        // Sort boards: logged-in player first, then alphabetically
        List<BingoData> sortedBoards = new ArrayList<>(bingoBoards);
        sortedBoards.sort((a, b) -> {
            String playerA = a.getPlayerName();
            String playerB = b.getPlayerName();
            
            // Logged-in player goes first
            if (playerA.equals(loggedInPlayer)) return -1;
            if (playerB.equals(loggedInPlayer)) return 1;
            
            // Then sort alphabetically
            return playerA.compareToIgnoreCase(playerB);
        });

        // Create board panels
        for (BingoData board : sortedBoards)
        {
            JPanel boardPanel = createBoardPanel(board);
            contentPanel.add(boardPanel);
            contentPanel.add(Box.createVerticalStrut(10)); // Reduced spacing between boards
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createBoardPanel(BingoData board)
    {
        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        boardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.LIGHT_GRAY_COLOR, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        boardPanel.setLayout(new BorderLayout());

        // Board title
        String loggedInPlayer = plugin.getConfig().playerName();
        String playerName = board.getPlayerName();
        String titleText = playerName;
        
        // Highlight if it's the logged-in player
        if (playerName.equals(loggedInPlayer))
        {
            titleText = "★ " + playerName + " (You) ★";
        }
        
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titleLabel.setForeground(playerName.equals(loggedInPlayer) ? new Color(255, 215, 0) : Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        boardPanel.add(titleLabel, BorderLayout.NORTH);

        // Create bingo grid
        List<BingoTile> tiles = board.getTiles();
        int size = board.getSize();
        
        if (size <= 0)
        {
            size = (int) Math.ceil(Math.sqrt(tiles.size()));
        }

        JPanel gridPanel = new JPanel();
        gridPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        gridPanel.setLayout(new GridLayout(size, size, 1, 1));
        gridPanel.setBorder(new EmptyBorder(2, 2, 2, 2));

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
                emptyPanel.setPreferredSize(new Dimension(35, 35));
                emptyPanel.setMinimumSize(new Dimension(35, 35));
                gridPanel.add(emptyPanel);
            }
        }

        boardPanel.add(gridPanel, BorderLayout.CENTER);
        return boardPanel;
    }

    private JPanel createTilePanel(BingoTile tile)
    {
        JPanel panel = new JPanel();
        panel.setBackground(tile.isCompleted() ? Color.GREEN.darker() : ColorScheme.MEDIUM_GRAY_COLOR);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.LIGHT_GRAY_COLOR));
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(35, 35));
        panel.setMinimumSize(new Dimension(35, 35));

        // Tile description (shortened)
        String shortDesc = tile.getDescription();
        if (shortDesc.length() > 15)
        {
            shortDesc = shortDesc.substring(0, 12) + "...";
        }
        
        JLabel descLabel = new JLabel(shortDesc);
        descLabel.setFont(FontManager.getRunescapeSmallFont());
        descLabel.setForeground(tile.isCompleted() ? Color.WHITE : Color.LIGHT_GRAY);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setBorder(new EmptyBorder(1, 1, 1, 1));

        // Points
        JLabel pointsLabel = new JLabel(tile.getPoints() + "p");
        pointsLabel.setFont(FontManager.getRunescapeSmallFont());
        pointsLabel.setForeground(Color.YELLOW);
        pointsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pointsLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Completion info
        if (tile.isCompleted())
        {
            JLabel completedLabel = new JLabel("✓");
            completedLabel.setFont(FontManager.getRunescapeSmallFont());
            completedLabel.setForeground(Color.GREEN);
            completedLabel.setHorizontalAlignment(SwingConstants.CENTER);
            completedLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

            JPanel infoPanel = new JPanel();
            infoPanel.setBackground(panel.getBackground());
            infoPanel.setLayout(new BorderLayout());
            infoPanel.add(pointsLabel, BorderLayout.NORTH);
            infoPanel.add(completedLabel, BorderLayout.SOUTH);

            panel.add(descLabel, BorderLayout.CENTER);
            panel.add(infoPanel, BorderLayout.SOUTH);
        }
        else
        {
            panel.add(descLabel, BorderLayout.CENTER);
            panel.add(pointsLabel, BorderLayout.SOUTH);
        }

        return panel;
    }
} 