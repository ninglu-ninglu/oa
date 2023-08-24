package com.atguigu.wechat.service;


import com.atguigu.model.wechat.Menu;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 菜单 服务类
 * </p>
 *
 * @author cheny
 * @since 2023-08-24
 */
public interface MenuService extends IService<Menu> {

    Object findMenuInfo();
}
