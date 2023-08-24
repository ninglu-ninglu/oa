package com.atguigu.wechat.mapper;

import com.atguigu.model.wechat.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 菜单 Mapper 接口
 * </p>
 *
 * @author cheny
 * @since 2023-08-24
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

}
