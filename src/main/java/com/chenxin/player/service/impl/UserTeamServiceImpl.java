package com.chenxin.player.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxin.player.mapper.UserTeamMapper;
import com.chenxin.player.model.domain.User;
import com.chenxin.player.model.domain.UserTeam;
import com.chenxin.player.service.UserTeamService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fangchenxin
 * @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
 * @createDate 2024-05-08 00:17:02
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
        implements UserTeamService {

    /**
     * @param loginUser
     * @return java.util.List<java.lang.Long>
     * @description 查询用户加入的队伍id列表
     * @author fangchenxin
     * @date 2024/5/10 19:21
     */
    @Override
    public List<Long> getTeamIdListByUserId(User loginUser) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = this.list(queryWrapper);
        return userTeamList.stream().map(UserTeam::getTeamId).distinct().collect(Collectors.toList());
    }
}




