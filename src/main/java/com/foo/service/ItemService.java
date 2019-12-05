package com.foo.service;

import com.foo.*;
import com.foo.dto.ItemInfoVO;
import java.util.*;
import java.util.stream.Collectors;
import static com.foo.common.Constants.*;

/**
 * Item服务
 *
 * @author zc
 * @date 2019-11-15
 */
public class ItemService {
    private ItemService() {}

    private static volatile ItemService itemService = null;

    /**
     * 不确定题目给的getServiceBean方法是否可该，这里自行实现单例
     * @return
     */
    public static ItemService getInstance() {
        if (itemService == null) {
            synchronized (ItemService.class) {
                if (itemService == null) {
                    itemService = new ItemService();
                }
            }
        }
        return itemService;
    }

    private static SkuService skuService = ServiceBeanFactory.getInstance().getServiceBean(SkuService.class);
    private static InventoryService inventoryService = ServiceBeanFactory.getInstance().getServiceBean(InventoryService.class);

    /**
     * 获取Item信息
     *
     * @param skuIds
     * @return
     */
    public List<ItemInfoVO> getItemInfos(List<String> skuIds) {
        List<ItemInfoVO> itemInfoVOS = new ArrayList<>();
        if (skuIds == null) {
            throw new RuntimeException("输入的skuId集合为空");
        } else if (skuIds.size() > SKU_ID_LIMIT) {
            throw new RuntimeException("输入不能超过100个 skuId");
        } else {
            List<SkuInfoDTO> skuInfoDTOS = getSkuInfoDTOs(skuIds);
            if (skuInfoDTOS != null && skuInfoDTOS.size() > 0) {
                itemInfoVOS = aggregate(skuInfoDTOS);
            }
        }
        return itemInfoVOS;
    }

    /**
     * 对于 SKU 类型为原始商品(ORIGIN)的, 按货号聚合成 ITEM; 对于 SKU 类型为数字化商品(DIGITAL)的, 按 SPUId 聚合成 ITEM
     * @param skuInfoDTOS
     */
    private List<ItemInfoVO> aggregate(List<SkuInfoDTO> skuInfoDTOS) {
        List<ItemInfoVO> itemInfoDTOS = new ArrayList<>();
        // SKU类型分组
        Map<String, List<SkuInfoDTO>> skuTypeMap =
                skuInfoDTOS.stream().collect(Collectors.groupingBy(SkuInfoDTO::getSkuType));

        for (String skuType : skuTypeMap.keySet()) {
            switch (skuType) {
                case ORIGIN:
                    // 按artNo分组并统计总库存
                    Map<String, Integer> artNoInvtMap =
                            skuTypeMap.get(skuType).stream()
                                    .collect(Collectors.groupingBy(SkuInfoDTO::getArtNo,
                                            Collectors.summingInt(this::getInventoryCount)));
                    // 从ORIGIN分组选择任意一个SKU商品名称作为Item商品名称
                    String originSkuName = skuTypeMap.get(skuType).get(0).getName();
                    for (String artNo : artNoInvtMap.keySet()) {
                        ItemInfoVO itemInfoDTO = new ItemInfoVO(originSkuName, artNo, null, artNoInvtMap.get(artNo));
                        itemInfoDTOS.add(itemInfoDTO);
                    }
                    break;
                case DIGITAL:
                    // 按spuId分组并统计总库存
                    Map<String, Integer> spuIdInvtMap =
                            skuTypeMap.get(skuType).stream()
                                    .collect(Collectors.groupingBy(SkuInfoDTO::getSpuId,
                                            Collectors.summingInt(this::getInventoryCount)));
                    // 从DIGITAL分组选择任意一个SKU商品名称作为Item商品名称
                    String digitalSkuName = skuTypeMap.get(skuType).get(0).getName();
                    for (String spuId : spuIdInvtMap.keySet()) {
                        ItemInfoVO itemInfoDTO = new ItemInfoVO(digitalSkuName, null, spuId, spuIdInvtMap.get(spuId));
                        itemInfoDTOS.add(itemInfoDTO);
                    }
                    break;
                default:
                    break;
            }
        }
        return itemInfoDTOS;
    }

    /**
     * 计算各渠道库存汇总
     * @param skuInfoDTO
     * @return
     */
    private int getInventoryCount(SkuInfoDTO skuInfoDTO) {
        List<ChannelInventoryDTO> channelInventoryDTOS = inventoryService.getBySkuId(skuInfoDTO.getId());
        if (channelInventoryDTOS == null) {
            return 0;
        } else {
            return channelInventoryDTOS
                    .stream()
                    .map(inventory -> inventory.getInventory().intValue())
                    .reduce(0, Integer::sum);

        }
    }

    /**
     * 批量获取SKU信息
     * @param skuIds
     * @return
     */
    private List<SkuInfoDTO> getSkuInfoDTOs(List<String> skuIds) {
        List<SkuInfoDTO> skuInfoDTOS = new ArrayList<>();
        int size = skuIds.size();
        int batch = size / 20;
        int remainder = size % 20;
        int fromIndex;

        for (int i = 0; i < batch; i++) {
            List<String> subSkuList;
            fromIndex = i * 20;
            // 如果下个区间不满20，则加上余数
            if ((fromIndex + 20) > size) {
                subSkuList = skuIds.subList(fromIndex, fromIndex + remainder);
            } else {
                subSkuList = skuIds.subList(fromIndex, fromIndex + 20);
            }
            List<SkuInfoDTO> subSkuInfoDTOS = skuService.findByIds(subSkuList);
            if (subSkuInfoDTOS != null) {
                skuInfoDTOS.addAll(subSkuInfoDTOS);
            }
        }
        return skuInfoDTOS;
    }

}
