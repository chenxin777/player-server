package com.chenxin.player.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenxin.player.common.DeleteRequest;
import com.chenxin.player.model.domain.Team;
import com.chenxin.player.model.domain.User;
import com.chenxin.player.model.dto.TeamQuery;
import com.chenxin.player.model.request.TeamJoinRequest;
import com.chenxin.player.model.request.TeamQuitRequest;
import com.chenxin.player.model.request.TeamUpdateRequest;
import com.chenxin.player.model.vo.TeamUserVO;

import java.util.List;


/**
 * @author fangchenxin
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2024-05-08 00:16:40
 */
public interface TeamService extends IService<Team> {

    /**
     * @param team
     * @return long
     * @description 创建队伍
     * @author fangchenxin
     * @date 2024/5/8 15:31
     */
    long addTeam(Team team, User loginUser);

    /**
     * @param teamQuery
     * @param isAdmin
     * @return java.util.List<com.chenxin.player.model.vo.TeamUserVO>
     * @description 获取队伍列表
     * @author fangchenxin
     * @date 2024/5/9 00:24
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    Page<TeamUserVO> listTeamsPage(TeamQuery teamQuery, boolean isAdmin);

    /**
     * @param teamUpdateRequest
     * @param loginUser
     * @return boolean
     * @description 修改队伍信息
     * @author fangchenxin
     * @date 2024/5/9 00:24
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * @param teamJoinRequest
     * @param loginUser
     * @return boolean
     * @description 加入队伍
     * @author fangchenxin
     * @date 2024/5/9 15:17
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * @param teamQuitRequest
     * @param loginUser
     * @return boolean
     * @description 退出队伍
     * @author fangchenxin
     * @date 2024/5/9 15:17
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(DeleteRequest deleteRequest, User loginUser);

}
