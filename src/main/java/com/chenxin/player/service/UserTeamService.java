package com.chenxin.player.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenxin.player.model.domain.User;
import com.chenxin.player.model.domain.UserTeam;

import java.util.List;


/**
 * @author fangchenxin
 * @description 针对表【user_team(用户队伍关系)】的数据库操作Service
 * @createDate 2024-05-08 00:17:02
 */
public interface UserTeamService extends IService<UserTeam> {

    /**
     * @param user
     * @return java.util.List<java.lang.Long>
     * @description 获取用户加入队伍的id集合
     * @author fangchenxin
     * @date 2024/5/10 19:22
     */
    List<Long> getTeamIdListByUserId(User user);

}
