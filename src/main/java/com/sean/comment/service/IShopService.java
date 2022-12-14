package com.sean.comment.service;

import com.sean.comment.dto.Result;
import com.sean.comment.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    Result getShopById(Long id);

    Result updateShopById(Shop shop);
}
