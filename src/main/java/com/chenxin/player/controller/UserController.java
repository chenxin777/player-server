package com.chenxin.player.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenxin.player.common.BaseResponse;
import com.chenxin.player.common.ErrorCode;
import com.chenxin.player.common.ResultUtils;
import com.chenxin.player.exception.BusinesssException;
import com.chenxin.player.model.domain.User;
import com.chenxin.player.model.request.UserLoginRequest;
import com.chenxin.player.model.request.UserRegisterRequest;
import com.chenxin.player.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.chenxin.player.constant.UserConstant.USER_LOGIN_STATE;


/**
 * @author fangchenxin
 * @description 用户管理
 * @date
 * @modify
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * @param userRegisterRequest
     * @return java.lang.Long
     * @description 用户注册
     * @author fangchenxin
     * @date 2024/4/19 21:05
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        long res = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(res);
    }

    /**
     * @param userLoginRequest
     * @param request
     * @return com.chenxin.player.model.domain.User
     * @description 用户登陆
     * @author fangchenxin
     * @date 2024/4/19 21:05
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * @param request
     * @return com.chenxin.player.model.domain.User
     * @description 获取当前用户
     * @author fangchenxin
     * @date 2024/4/19 21:05
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = User.class.cast(userObj);
        if (currentUser == null) {
            throw new BusinesssException(ErrorCode.NOT_LOGIN);
        }
        // 查用户信息
        Long userId = currentUser.getId();
        // todo 校验用户是否合法
        User user = userService.getById(userId);
        User safeUser = userService.getSafeUser(user);
        return ResultUtils.success(safeUser);
    }

    /**
     * @param username
     * @param request
     * @return java.util.List<com.chenxin.player.model.domain.User>
     * @description 查找用户
     * @author fangchenxin
     * @date 2024/4/19 21:04
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(@RequestParam(required = false) String username, HttpServletRequest request) {
        // 鉴权 仅管理员可查询
        if (!userService.isAdmin(request)) {
            throw new BusinesssException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (!StringUtils.isBlank(username)) {
            userQueryWrapper.like("username", username);
        }
        List<User> userList = userService.list(userQueryWrapper);
        return ResultUtils.success(userList.stream().map(userService::getSafeUser).collect(Collectors.toList()));
    }

    /**
     * @param id
     * @param request
     * @return boolean
     * @description 删除用户
     * @author fangchenxin
     * @date 2024/4/19 21:04
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinesssException(ErrorCode.NO_AUTH);
        }
        if (id < 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.removeById(id));
    }

    /**
     * @param request
     * @return java.lang.Integer
     * @description 用户注销
     * @author fangchenxin
     * @date 2024/4/19 21:03
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * @param tagNameList
     * @return com.chenxin.usercenterserver.common.BaseResponse<java.util.List < com.chenxin.player.model.domain.User>>
     * @description 根据标签搜索用户
     * @author fangchenxin
     * @date 2024/4/27 20:49
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.searchUserByTags(tagNameList));
    }

    /**
     * @param user
     * @param request
     * @return com.chenxin.usercenterserver.common.BaseResponse<java.lang.Integer>
     * @description 用户更新细腻下
     * @author fangchenxin
     * @date 2024/4/27 22:50
     */
    @PostMapping("update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        // 判断user里面除id以外的属性是否都为null
        if (!userService.areFieldsNullExceptId(user)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        // 校验权限
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinesssException(ErrorCode.NO_AUTH);
        }
        // 触发更新
        return ResultUtils.success(userService.updateUser(user, loginUser));
    }

    /**
     * TODO
     *
     * @param pageSize
     * @param pageNum
     * @param request
     * @return com.chenxin.usercenterserver.common.BaseResponse<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.chenxin.player.model.domain.User>>
     * @description 主页推荐 分页
     * @author fangchenxin
     * @date 2024/4/29 12:09
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(int pageSize, int pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        String redisKey = String.format("player:user:recommend:%s", loginUser.getId());
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 无缓存 查库
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        Page<User> userList = userService.page(new Page<>(pageNum, pageSize), userQueryWrapper);
        // 写缓存
        try {
            valueOperations.set(redisKey, userList, 10000, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            log.error("redis set key error", ex);
        }
        return ResultUtils.success(userList);
    }

    @GetMapping("/match")
    public BaseResponse<List<User>> matchUser(long num, HttpServletRequest request) {
        if (num < 1 || num > 20) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<User> userList = userService.matchUsers(num, loginUser);
        return ResultUtils.success(userList);
    }

}
