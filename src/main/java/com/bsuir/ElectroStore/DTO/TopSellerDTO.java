package com.bsuir.ElectroStore.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Data
public class TopSellerDTO {
    private final String name;
    private final String surname;
    private final Long salesCount;
}