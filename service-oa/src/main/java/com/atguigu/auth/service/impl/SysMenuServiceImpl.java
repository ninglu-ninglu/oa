package com.atguigu.auth.service.impl;

import com.atguigu.auth.mapper.SysMenuMapper;
import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysRoleMenuService;
import com.atguigu.auth.utils.MenuHelper;
import com.atguigu.common.config.handler.GuiguException;
import com.atguigu.model.system.SysMenu;
import com.atguigu.model.system.SysRoleMenu;
import com.atguigu.vo.system.AssginMenuVo;
import com.atguigu.vo.system.MetaVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author cheny
 * @since 2023-08-21
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    //菜单列表接口
    @Override
    public List<SysMenu> findNodes() {
        //查询所有菜单
        List<SysMenu> list = baseMapper.selectList(null);
        //构建树形结构
        List<SysMenu> resultList=MenuHelper.buildTree(list);
        return resultList;
    }

    @Override
    public void removeMenuById(Long id) {
        LambdaQueryWrapper<SysMenu> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId,id);
        Integer integer = baseMapper.selectCount(wrapper);
        if (integer>0){
            throw new GuiguException(201,"菜单不能删除");
        }
        baseMapper.deleteById(id);
    }

    //查询所有菜单和角色分配菜单
    @Override
    public List<SysMenu> findMenuByRoleid(Long roleId) {
        //查询所有菜单，status=1是可用菜单
        LambdaQueryWrapper<SysMenu> wrappersysMenu=new LambdaQueryWrapper<>();
        wrappersysMenu.eq(SysMenu::getStatus,1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(wrappersysMenu);
        //根据角色id roleid查询 角色菜单里面的角色id对应的菜单id
        LambdaQueryWrapper<SysRoleMenu> wrappersysRoleMenu=new LambdaQueryWrapper<>();
        wrappersysRoleMenu.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(wrappersysRoleMenu);
        //根据获取的菜单id，获取对应菜单对象
        List<Long> menuList = sysRoleMenuList.stream().map(c -> c.getMenuId()).collect(Collectors.toList());

        //拿着菜单id，和所有菜单集合里面id进行比较，如果相同就封装
        allSysMenuList.stream().forEach(item->{
            if (menuList.contains(item.getId())){
                item.setSelect(true);
            }else {
                item.setSelect(false);
            }
        });
        //返回菜单树形结构格式
        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
        return sysMenuList;
    }

    //为角色分配菜单
    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        //根据角色id删除菜单角色表里面分配的数据
        LambdaQueryWrapper<SysRoleMenu> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId,assginMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);
        //从参数里面获取角色分配的菜单id列表

        List<Long> menuIdList = assginMenuVo.getMenuIdList();
        for (Long meunId:menuIdList){
            if (StringUtils.isEmpty(meunId)){
                continue;
            }
            SysRoleMenu sysRoleMenu=new SysRoleMenu();
            sysRoleMenu.setMenuId(meunId);
            sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
            sysRoleMenuService.save(sysRoleMenu);
        }
    }

    //
    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList=null;
        if (userId.intValue()==1){
            //如果是管理员，则查询所有菜单
            LambdaQueryWrapper<SysMenu> wrapper =new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            //如果不是，查询可操作的菜单
            sysMenuList=baseMapper.findMenuListByUserId(userId);
        }
        //把查询的数据构建成动态路由
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        List<RouterVo> routerList=this.buildRouter(sysMenuTreeList);
        return routerList;
    }

    //构建框架需要的路由结构
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        List<RouterVo> routers=new ArrayList<>();
        for (SysMenu menu:menus) {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            //下一层的数据
            List<SysMenu> children = menu.getChildren();
            if(menu.getType().intValue()==1){
                List<SysMenu> hiddenMenuList = children.stream().filter(item -> 
                        !StringUtils.isEmpty(item.getComponent()))
                        .collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            }else {
                if (!CollectionUtils.isEmpty(children)){
                    if (children.size()>0){
                        router.setAlwaysShow(true);
                    }
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }
    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        List<SysMenu> sysMenuList=null;
        if (userId.intValue()==1){
            //如果是管理员，则查询所有按钮
            LambdaQueryWrapper<SysMenu> wrapper =new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            sysMenuList=baseMapper.findMenuListByUserId(userId);
        }
        //去除perms的值，可以用for循环
        List<String> permsList = sysMenuList.stream()
                //条件筛选
                .filter(item -> item.getType() == 2)
                //取出值
                .map(item -> item.getPerms())
                //以list集合形式返回
                .collect(Collectors.toList());


        return permsList;
    }
}
