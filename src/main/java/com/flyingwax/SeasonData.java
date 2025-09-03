package com.flyingwax;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeasonData
{
    private String id;
    private String name;
    private String status;
    private long startDate;
    private long endDate;
} 