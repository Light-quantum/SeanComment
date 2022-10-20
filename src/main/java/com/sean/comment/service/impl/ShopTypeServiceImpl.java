package com.sean.comment.service.impl;

import cn.hutool.json.JSONUtil;
import com.sean.comment.dto.Result;
import com.sean.comment.entity.ShopType;
import com.sean.comment.mapper.ShopTypeMapper;
import com.sean.comment.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sean.comment.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;
import static com.sean.comment.utils.RedisConstants.CACHE_SHOP_TYPE_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@SuppressWarnings("all")
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result getShopTypeList() {
        // 1. 查询缓存
        List<String> cacheShopType = redisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        // 2. 缓存命中
        if(!cacheShopType.isEmpty()){
            List<ShopType> typeList = cacheShopType.stream().map(i->JSONUtil.toBean(i, ShopType.class)).collect(Collectors.toList());
            return Result.ok(typeList);
        }
        // 3. 缓存未命中，查询数据库
        List<ShopType> typeList = query().orderByAsc("sort").list();
        // 4. 数据库不存在
        if(typeList.isEmpty()){
            return Result.fail("不存在任何商铺类型！");
        }
        // 5. 数据库存在，写缓存
        redisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY,
                typeList.stream().map(i-> JSONUtil.toJsonStr(i)).collect(Collectors.toList()));
        redisTemplate.expire(CACHE_SHOP_TYPE_KEY, CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
        // 6. 返回
        return Result.ok(typeList);
    }
}
