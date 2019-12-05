package com.foo;

import com.foo.dto.ItemInfoVO;
import com.foo.service.ItemService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class ExamTest {

    private static List<String> skuIds;

    /**
     * 构造100个 skuid 作为测试条件
     */
    @BeforeClass
    public static void setUp() {
        skuIds = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            skuIds.add(String.valueOf(i));
        }
    }

    @AfterClass
    public static void tearDown() {
        skuIds = null;
    }

    @Test
    public void test() {
        ItemService itemService = ItemService.getInstance();
        List<ItemInfoVO> itemInfoVOS = itemService.getItemInfos(skuIds);

        itemInfoVOS.forEach(System.out::println);
    }

}
