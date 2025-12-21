package com.lcp.spb.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品信息实体类
 * 
 * <p>表示商品的基本信息，包含商品编号、名称、价格和描述。
 * 
 * <p>字段说明：
 * <ul>
 *   <li>sku：商品编号（Stock Keeping Unit，库存量单位）</li>
 *   <li>name：商品名称</li>
 *   <li>price：商品价格</li>
 *   <li>description：商品描述信息</li>
 * </ul>
 * 
 * @author lcp
 */
@AllArgsConstructor @NoArgsConstructor @Data
public class GoodsInfo {

    /** 商品编号（SKU），用于唯一标识商品 */
    private String sku;
    /** 商品名称 */
    private String name;
    /** 商品价格 */
    private Double price;
    /** 商品描述信息 */
    private String description;
}
