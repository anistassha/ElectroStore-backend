package com.bsuir.ElectroStore.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Data
public class DailyRevenue {
    private final long salesCount;
    private final double totalRevenue;
}