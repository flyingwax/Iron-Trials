package com.flyingwax;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BingoData
{
    private String id;
    private String name;
    private List<BingoTile> tiles;
    private int size;
    private String status;
} 