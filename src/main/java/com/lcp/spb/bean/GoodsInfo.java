package com.lcp.spb.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GoodsInfo {

    private String sku;
    private String name;
    private Double price;
    private String description;
}
