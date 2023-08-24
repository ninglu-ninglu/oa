package com.atguigu.auth.utils;

import com.atguigu.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

/*
@time 2023/8/21-17:05
@authon cheny
@name 哈哈
@version 1.0
*/
public class MenuHelper {
    public static List<SysMenu> buildTree(List<SysMenu> list) {
        //创建一个list集合
        List<SysMenu> tree=new ArrayList<>();

        for (SysMenu sysMenu:list) {
            if (sysMenu.getParentId().longValue()==0){
                tree.add(getChildren(sysMenu,list));
            }
        }
        return tree;
    }

    private static SysMenu getChildren(SysMenu sysMenu, List<SysMenu> list) {
        sysMenu.setChildren(new ArrayList<SysMenu>());
        //遍历所有的菜单数据
        for (SysMenu it: list){
            if(sysMenu.getId().longValue()==it.getParentId().longValue()){
                if (sysMenu.getChildren()==null){
                    sysMenu.setChildren(new ArrayList<>());
                }
                sysMenu.getChildren().add(getChildren(it,list));
            }
        }
        return sysMenu;
    }
}
