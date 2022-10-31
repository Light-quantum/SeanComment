package com.sean.comment.service.impl;

import cn.hutool.json.JSONUtil;
import com.sean.comment.dto.Result;
import com.sean.comment.entity.Shop;
import com.sean.comment.mapper.ShopMapper;
import com.sean.comment.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.sean.comment.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.sean.comment.utils.RedisConstants.CACHE_SHOP_TTL;

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
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result getShopById(Long id) {
        String redisKey = CACHE_SHOP_KEY + id;
        // 1. 从 redis 查询缓存
        String cacheShop = redisTemplate.opsForValue().get(redisKey);
        // 2. 判断存在
        if(StringUtils.isNotBlank(cacheShop)){
            Shop shop = JSONUtil.toBean(cacheShop, Shop.class);
            return Result.ok(shop);
        }
        // 3. 不存在,查询数据库
        Shop shop = getById(id);
        // 6. 数据库不存在，返回 404
        if(shop == null){
            return Result.fail("店铺不存在！");
        }
        // 7. 数据库存在，写到 redis
        redisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        // 8. 返回
        return Result.ok(shop);
    }

    @Override
    @Transactional  // 事务保证更新数据和删除缓存操作的一致性。分布式系统需要借助 TCC 实现一致性
    public Result updateShopById(Shop shop) {
        // 参数验证
        if(null == shop || null == shop.getId()){
            return Result.fail("店铺信息错误!");
        }
        // 更新数据库
        System.out.println(shop.getName());
        updateById(shop);
        // 删除缓存
        String redisKey = CACHE_SHOP_KEY + shop.getId();
        redisTemplate.delete(redisKey);
        return Result.ok();
    }
}
