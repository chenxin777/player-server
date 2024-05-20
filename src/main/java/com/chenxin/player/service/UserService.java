package com.chenxin.player.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.chenxin.player.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author fangchenxin
 * @description 针对表【user】的数据库操作Service
 * @createDate 2024-04-16 19:54:04
 */
public interface UserService extends IService<User> {


    /**
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return long
     * @description 用户注册
     * @author fangchenxin
     * @date 2024/4/17 00:12
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return com.chenxin.player.model.domain.User
     * @description 用户登陆
     * @author fangchenxin
     * @date 2024/4/17 12:14
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * @param originUser
     * @return com.chenxin.player.model.domain.User
     * @description 获取脱敏用户信息
     * @author fangchenxin
     * @date 2024/4/17 23:32
     */
    User getSafeUser(User originUser);

    /**
     * @param request
     * @return int
     * @description 注销账号
     * @author fangchenxin
     * @date 2024/4/19 20:43
     */
    int userLogout(HttpServletRequest request);

    /**
     * @param tagNameList
     * @return int
     * @description 根据标签搜索用户
     * @author fangchenxin
     * @date 2024/4/24 22:23
     */
    List<User> searchUserByTags(List<String> tagNameList);

    /**
     * @param user
     * @return java.lang.Integer
     * @description 更新用户信息
     * @author fangchenxin
     * @date 2024/4/27 20:52
     */
    Integer updateUser(User user, User loginUser);

    /**
     * @return com.chenxin.player.model.domain.User
     * @description 获取当前用户登录信息
     * @author fangchenxin
     * @date 2024/4/27 21:18
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * @param request
     * @return boolean
     * @description 判断是否为管理员
     * @author fangchenxin
     * @date 2024/4/27 21:43
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * @param loginUser 当前登录用户
     * @return boolean
     * @description 判断是否为管理员
     * @author fangchenxin
     * @date 2024/4/27 21:47
     */
    boolean isAdmin(User loginUser);

    boolean areFieldsNullExceptId(User user);

    List<User> matchUsers(long num, User loginUser);

}
