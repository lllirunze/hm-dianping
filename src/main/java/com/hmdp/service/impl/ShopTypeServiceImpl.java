package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getTypeList() {
        String key = CACHE_TYPE_KEY;
        // 从redis中查询
        Long typeListSize = stringRedisTemplate.opsForList().size(key);
        // 如果redis存在数据
        if (typeListSize != null && typeListSize != 0) {
            List<String> typeJsonList = stringRedisTemplate.opsForList().range(key, 0, typeListSize-1);
            List<ShopType> typeList = new ArrayList<>();
            for (String typeJson : typeJsonList) {
                typeList.add(JSONUtil.toBean(typeJson, ShopType.class));
            }
            return Result.ok();
        }
        // 如果redis不存在数据，查询数据库
        List<ShopType> typeList = query().orderByAsc("sort").list();
        if (typeList == null) {
            // 数据库不存在数据
            return Result.fail("发生错误");
        }
        // 转换成JSON数据
        List<String> typeJsonList = new ArrayList<>();
        for (ShopType shopType : typeList) {
            typeJsonList.add(JSONUtil.toJsonStr(shopType));
        }
        // 数据库存在数据，写入redis
        stringRedisTemplate.opsForList().rightPushAll(key, typeJsonList);

        return Result.ok(typeList);
    }
}
