package com.chenxin.player.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenxin.player.common.BaseResponse;
import com.chenxin.player.common.DeleteRequest;
import com.chenxin.player.common.ErrorCode;
import com.chenxin.player.common.ResultUtils;
import com.chenxin.player.exception.BusinesssException;
import com.chenxin.player.model.domain.Team;
import com.chenxin.player.model.domain.User;
import com.chenxin.player.model.domain.UserTeam;
import com.chenxin.player.model.dto.TeamQuery;
import com.chenxin.player.model.request.TeamAddRequest;
import com.chenxin.player.model.request.TeamJoinRequest;
import com.chenxin.player.model.request.TeamQuitRequest;
import com.chenxin.player.model.request.TeamUpdateRequest;
import com.chenxin.player.model.vo.TeamUserVO;
import com.chenxin.player.service.TeamService;
import com.chenxin.player.service.UserService;
import com.chenxin.player.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author fangchenxin
 * @description
 * @date 2024/5/8 10:35
 * @modify
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    /**
     * @param teamAddRequest
     * @param request
     * @return com.chenxin.player.common.BaseResponse<java.lang.Long>
     * @description 新增队伍
     * @author fangchenxin
     * @date 2024/5/8 10:46
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamAddRequest);
        } catch (Exception e) {
            throw new BusinesssException(ErrorCode.SYSTEM_ERROR);
        }
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    /**
     * @param deleteRequest
     * @param request
     * @return com.chenxin.player.common.BaseResponse<java.lang.Boolean>
     * @description 删除队伍
     * @author fangchenxin
     * @date 2024/5/8 10:46
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.deleteTeam(deleteRequest, loginUser);
        if (!res) {
            throw new BusinesssException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * @param teamUpdateRequest
     * @param request
     * @return com.chenxin.player.common.BaseResponse<java.lang.Boolean>
     * @description 更新队伍
     * @author fangchenxin
     * @date 2024/5/8 10:48
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!res) {
            throw new BusinesssException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinesssException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    /**
     * @param teamQuery
     * @return com.chenxin.player.common.BaseResponse<java.util.List < com.chenxin.player.model.domain.Team>>
     * @description 队伍列表查询
     * @author fangchenxin
     * @date 2024/5/8 12:11
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        // 判断当前用户是否已加入队伍
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 查当前用户加入的队伍列表
        try {
            User loginUser = userService.getLoginUser(request);
            List<UserTeam> hasJoinList = userTeamService.list(new QueryWrapper<UserTeam>().eq("userId", loginUser.getId()).in("teamId", teamIdList));
            Set<Long> hasJoinTeamId = hasJoinList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList = teamList.stream().map(team -> {
                team.setHasJoin(hasJoinTeamId.contains(team.getId()));
                return team;
            }).collect(Collectors.toList());
        } catch (Exception e) {
        }
        // 查询加入队伍的用户信息
        List<UserTeam> userTeamList = userTeamService.list(new QueryWrapper<UserTeam>().in("teamId", teamIdList));
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList = teamList.stream().map(team -> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
            return team;
        }).collect(Collectors.toList());
        return ResultUtils.success(teamList);
    }

    /**
     * TODO
     *
     * @param teamQuery
     * @return com.chenxin.player.common.BaseResponse<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.chenxin.player.model.domain.Team>>
     * @description 队伍分页查询
     * @author fangchenxin
     * @date 2024/5/8 12:11
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<TeamUserVO>> listTeamsByPage(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        Page<TeamUserVO> teamPage = teamService.listTeamsPage(teamQuery, isAdmin);
        List<TeamUserVO> teamList = teamPage.getRecords();
        // 判断当前用户是否已加入队伍
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 查当前用户加入的队伍列表
        try {
            User loginUser = userService.getLoginUser(request);
            List<UserTeam> hasJoinList = userTeamService.list(new QueryWrapper<UserTeam>().eq("userId", loginUser.getId()).in("teamId", teamIdList));
            Set<Long> hasJoinTeamId = hasJoinList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList = teamList.stream().map(team -> {
                team.setHasJoin(hasJoinTeamId.contains(team.getId()));
                return team;
            }).collect(Collectors.toList());
        } catch (Exception e) {
        }
        return ResultUtils.success(teamPage.setRecords(teamList));
    }

    /**
     * @param teamJoinRequest
     * @param request
     * @return com.chenxin.player.common.BaseResponse<java.lang.Boolean>
     * @description 加入队伍
     * @author fangchenxin
     * @date 2024/5/9 15:03
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * @param teamQuitRequest
     * @param request
     * @return com.chenxin.player.common.BaseResponse<java.lang.Boolean>
     * @description 退出队伍
     * @author fangchenxin
     * @date 2024/5/9 18:03
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null || teamQuitRequest.getTeamId() <= 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(teamService.quitTeam(teamQuitRequest, loginUser));
    }

    /**
     * @param teamQuery
     * @param request
     * @return com.chenxin.player.common.BaseResponse<java.util.List < com.chenxin.player.model.vo.TeamUserVO>>
     * @description 获取创建的队伍
     * @author fangchenxin
     * @date 2024/5/10 18:34
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        final List<Long> teamIdList = userTeamService.getTeamIdListByUserId(loginUser);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        teamList = teamList.stream().peek(team -> team.setHasJoin(true)).collect(Collectors.toList());
        // 获取加入队伍的人数
        List<UserTeam> userTeamList = userTeamService.list(new QueryWrapper<UserTeam>().in("teamId", teamIdList));
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList = teamList.stream().map(team -> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
            return team;
        }).collect(Collectors.toList());
        return ResultUtils.success(teamList);
    }

    /**
     * @param teamQuery
     * @param request
     * @return com.chenxin.player.common.BaseResponse<java.util.List < com.chenxin.player.model.vo.TeamUserVO>>
     * @description
     * @author fangchenxin
     * @date 2024/5/10 18:46
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        final List<Long> teamIdList = userTeamService.getTeamIdListByUserId(loginUser);
        teamQuery.setIdList(teamIdList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        teamList = teamList.stream().peek(team -> team.setHasJoin(true)).collect(Collectors.toList());
        // 获取已加入队伍的人数
        List<UserTeam> userTeamList = userTeamService.list(new QueryWrapper<UserTeam>().in("teamId", teamIdList));
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList = teamList.stream().map(team -> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
            return team;
        }).collect(Collectors.toList());
        return ResultUtils.success(teamList);
    }

}
