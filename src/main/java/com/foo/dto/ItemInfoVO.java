package com.foo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Item展示信息类
 *
 * @author zc
 * @date 2019-11-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInfoVO implements Serializable {

    private static final long serialVersionUID = -4854548358459432432L;

    /**
     * 商品名称
     */
    private String name;
    /**
     * 货号
     */
    private String artNo;
    /**
     * itemid
     */
    private String spuId;
    /**
     * 全渠道库存汇总
     */
    private Integer inventory;
}
