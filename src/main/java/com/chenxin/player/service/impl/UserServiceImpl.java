package com.chenxin.player.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxin.player.common.ErrorCode;
import com.chenxin.player.exception.BusinesssException;
import com.chenxin.player.mapper.UserMapper;
import com.chenxin.player.model.domain.User;
import com.chenxin.player.service.UserService;
import com.chenxin.player.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.chenxin.player.constant.UserConstant.*;


/**
 * @author fangchenxin
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2024-04-16 19:54:04
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Resource
    private UserMapper userMapper;

    /**
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @param planetCode
     * @return long
     * @description 注册
     * @author fangchenxin
     * @date 2024/4/19 20:44
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1、校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不包含特殊字符
        String validPattern = "^[A-Za-z0-9]*$";
        if (!userAccount.matches(validPattern)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "用户账号包含特殊字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "密码和校验密码不相同");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "用户账号重复");
        }

        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "星球编号重复");
        }

        // 2、加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3、插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean res = save(user);
        if (!res) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * @param userAccount
     * @param userPassword
     * @param request
     * @return com.chenxin.player.model.domain.User
     * @description 登陆
     * @author fangchenxin
     * @date 2024/4/19 20:44
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1、校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "登录参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不包含特殊字符
        String validPattern = "^[A-Za-z0-9]*$";
        if (!userAccount.matches(validPattern)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "用户账号包含特殊字符");
        }
        // 2、加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询账户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3、用户脱敏(返回前端能看到的数据)
        User safeUser = getSafeUser(user);
        // 4、记录用户登陆态
        request.getSession().setAttribute(USER_LOGIN_STATE, safeUser);
        return safeUser;

    }

    /**
     * @param originUser
     * @return com.chenxin.player.model.domain.User
     * @description 获取脱敏用户
     * @author fangchenxin
     * @date 2024/4/19 20:45
     */
    @Override
    public User getSafeUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safeUser = new User();
        safeUser.setId(originUser.getId());
        safeUser.setUsername(originUser.getUsername());
        safeUser.setUserAccount(originUser.getUserAccount());
        safeUser.setAvatarUrl(originUser.getAvatarUrl());
        safeUser.setGender(originUser.getGender());
        safeUser.setPhone(originUser.getPhone());
        safeUser.setEmail(originUser.getEmail());
        safeUser.setUserStatus(originUser.getUserStatus());
        safeUser.setUserRole(originUser.getUserRole());
        safeUser.setPlanetCode(originUser.getPlanetCode());
        safeUser.setCreateTime(originUser.getCreateTime());
        safeUser.setTags(originUser.getTags());
        return safeUser;
    }

    /**
     * @param request
     * @return int
     * @description 注销用户
     * @author fangchenxin
     * @date 2024/4/19 20:44
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登陆态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * @param tagNameList
     * @return int
     * @description 根据标签搜索用户 （内存过滤版）
     * @author fangchenxin
     * @date 2024/4/24 22:22
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 查所有用户
        List<User> userList = userMapper.selectList(queryWrapper);
        // 在内存中判断是否包含标签
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            // 集合判空
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafeUser).collect(Collectors.toList());
    }

    /**
     * @param user
     * @return java.lang.Integer
     * @description 更新用户
     * @author fangchenxin
     * @date 2024/4/27 20:52
     */
    @Override
    public Integer updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只能更新自己
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinesssException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinesssException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * @param request
     * @return com.chenxin.player.model.domain.User
     * @description 获取当前登录用户
     * @author fangchenxin
     * @date 2024/4/27 21:21
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinesssException(ErrorCode.NOT_LOGIN);
        }
        return User.class.cast(userObj);
    }

    /**
     * @param request
     * @return boolean
     * @description 判断是否为管理员
     * @author fangchenxin
     * @date 2024/4/19 21:04
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = User.class.cast(userObject);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * @param loginUser
     * @return boolean
     * @description 判断是否为管理员
     * @author fangchenxin
     * @date 2024/5/12 16:15
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * @param user
     * @return boolean
     * @description 判断user对象是否有非空字段
     * @author fangchenxin
     * @date 2024/4/28 15:41
     */
    @Override
    public boolean areFieldsNullExceptId(User user) {
        try {
            for (Field field : User.class.getDeclaredFields()) {
                // 设置访问权限，以便可以访问私有字段
                field.setAccessible(true);
                // 跳过id字段的检查
                if ("id".equals(field.getName())) {
                    continue;
                }
                // 获取字段值
                Object value = field.get(user);
                // 如果有任意一个字段不为null，则返回false
                if (value != null) {
                    return true;
                }
            }
        } catch (IllegalAccessException e) {
            // 处理可能的访问权限问题，实际应用中可能需要更详细的错误处理
            throw new BusinesssException(ErrorCode.SYSTEM_ERROR);
        }
        // 所有非id字段都为null时返回true
        return false;
    }

    /**
     * @param num
     * @param loginUser
     * @return java.util.List<com.chenxin.player.model.domain.User>
     * @description 根据标签匹配用户
     * @author fangchenxin
     * @date 2024/5/12 15:16
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        if (num < 1 || num > 20) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        // 用户列表下标 =》相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或当前用户自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            if (CollectionUtils.isEmpty(userTagList)) {
                continue;
            }
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            //indexDistanceMap.put(i, distance);
            list.add(new Pair(user, distance));
        }
        // 编辑距离由小到大排列
        List<Pair<User, Long>> sortedList = list.stream()
                .sorted((o1, o2) -> (int) (o1.getValue() - o2.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        List<Long> userIdList = sortedList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        // 查询完 顺序变了
        userQueryWrapper.in("id", userIdList);
        List<User> unSortedList = this.list(userQueryWrapper).stream().map(this::getSafeUser).collect(Collectors.toList());
        Map<Long, List<User>> unSortedListMap = unSortedList.stream().collect(Collectors.groupingBy(User::getId));
        List<User> users = new ArrayList<>();
        for (Long userId : userIdList) {
            if (unSortedListMap.containsKey(userId)) {
                users.add(unSortedListMap.get(userId).get(0));
            }
        }
        return users;
    }

    /**
     * @param tagNameList
     * @return java.util.List<com.chenxin.player.model.domain.User>
     * @description 根据标签搜索用户（SQL过滤）
     * @author fangchenxin
     * @date 2024/4/25 11:16
     */
    @Deprecated
    private List<User> searchUserByTagsBySql(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());
    }
}




