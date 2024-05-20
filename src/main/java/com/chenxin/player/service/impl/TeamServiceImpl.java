package com.chenxin.player.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxin.player.common.DeleteRequest;
import com.chenxin.player.common.ErrorCode;
import com.chenxin.player.exception.BusinesssException;
import com.chenxin.player.mapper.TeamMapper;
import com.chenxin.player.model.domain.Team;
import com.chenxin.player.model.domain.User;
import com.chenxin.player.model.domain.UserTeam;
import com.chenxin.player.model.dto.TeamQuery;
import com.chenxin.player.model.enums.TeamQueryTypeEnum;
import com.chenxin.player.model.enums.TeamStatusEnum;
import com.chenxin.player.model.request.TeamJoinRequest;
import com.chenxin.player.model.request.TeamQuitRequest;
import com.chenxin.player.model.request.TeamUpdateRequest;
import com.chenxin.player.model.vo.TeamUserVO;
import com.chenxin.player.model.vo.UserVO;
import com.chenxin.player.service.TeamService;
import com.chenxin.player.service.UserService;
import com.chenxin.player.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author fangchenxin
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-05-08 00:16:40
 */
@Slf4j
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * @param teamQuery
     * @param isAdmin
     * @return com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.chenxin.player.model.domain.Team>
     * @description 拼接查询条件
     * @author fangchenxin
     * @date 2024/5/12 15:19
     */
    private QueryWrapper<Team> getQueryWrapper(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //1、从请求参数中取出队伍名称，如果存在则作为查询条件
        if (teamQuery != null) {
            // 队伍id
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            // 队伍列表id
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            // 搜索关键词
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 队伍名
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            // 队伍描述
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            // 队伍最大人数
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            // 根据队伍状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            // 只有管理员能查看私有队伍
            if (isAdmin) {
                if (statusEnum != null) {
                    queryWrapper.eq("status", statusEnum.getValue());
                }
            } else {
                if (statusEnum == null) {
                    queryWrapper.in("status", TeamStatusEnum.PUBLIC.getValue());
                } else {
                    if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
                        throw new BusinesssException(ErrorCode.NO_AUTH);
                    } else {
                        queryWrapper.eq("status", statusEnum.getValue());
                    }
                }
            }
            // 根据创建人查询
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 队伍查询类型
            Integer queryType = teamQuery.getQueryType();
            TeamQueryTypeEnum teamQueryTypeEnum = TeamQueryTypeEnum.getEnumByValue(queryType);
            if (teamQueryTypeEnum != null) {
                if (TeamQueryTypeEnum.NO_EXPIRED.equals(teamQueryTypeEnum)) {
                    queryWrapper.and(qw -> qw.ge("expireTime", new Date()).or().isNull("expireTime"));
                }
            }
        }
        return queryWrapper;
    }

    /**
     * @param team
     * @param loginUser
     * @return long
     * @description 添加队伍
     * @author fangchenxin
     * @date 2024/5/9 00:32
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1、请求参数为空
        if (team == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        //2、是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinesssException(ErrorCode.NO_AUTH);
        }
        final long userId = loginUser.getId();
        //3、校验信息
        //  1、队伍人数>1且<=20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //  2、队伍标题<=20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //  3、描述<=512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //  4、status是否公开int 不传默认为0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //  5、如果status是加密状态，一定要有密码，密码<=32
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            String password = team.getPassword();
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinesssException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //  6、超时时间>当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime == null || expireTime.before(new Date())) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        //  7、校验用户最多创建5个队伍
        // todo 可能同时创建100个队伍，限制某一时间范围只能创建1个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
        //4、插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean teamRes = this.save(team);
        Long teamId = team.getId();
        if (!teamRes || teamId == null) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //5、插入用户 =》队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setId(null);
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean userTeamRes = userTeamService.save(userTeam);
        if (!userTeamRes) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    /**
     * @param teamQuery
     * @param isAdmin
     * @return java.util.List<com.chenxin.player.model.vo.TeamUserVO>
     * @description 队伍列表查询
     * @author fangchenxin
     * @date 2024/5/9 00:32
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = this.getQueryWrapper(teamQuery, isAdmin);
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            // 根据用户id查询创建人信息
            User user = userService.getById(userId);
            if (user == null) {
                continue;
            }
            // 返回队伍信息
            TeamUserVO teamUserVO = new TeamUserVO();
            // 返回队伍创建人信息
            UserVO userVO = new UserVO();
            try {
                BeanUtils.copyProperties(teamUserVO, team);
                // 脱敏用户信息
                BeanUtils.copyProperties(userVO, user);
                teamUserVO.setCreateUser(userVO);
            } catch (Exception e) {
                throw new BusinesssException(ErrorCode.SYSTEM_ERROR);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public Page<TeamUserVO> listTeamsPage(TeamQuery teamQuery, boolean isAdmin) {
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = this.getQueryWrapper(teamQuery, isAdmin);
        Page<Team> teamPage = this.page(page, queryWrapper);
        List<Team> teamList = teamPage.getRecords();
        if (CollectionUtils.isEmpty(teamList)) {
            return new Page<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            // 根据用户id查询创建人信息
            User user = userService.getById(userId);
            if (user == null) {
                continue;
            }
            // 返回队伍信息
            TeamUserVO teamUserVO = new TeamUserVO();
            // 返回队伍创建人信息
            UserVO userVO = new UserVO();
            try {
                BeanUtils.copyProperties(teamUserVO, team);
                // 脱敏用户信息
                BeanUtils.copyProperties(userVO, user);
                teamUserVO.setCreateUser(userVO);
            } catch (Exception e) {
                throw new BusinesssException(ErrorCode.SYSTEM_ERROR);
            }
            teamUserVOList.add(teamUserVO);
        }
        Page<TeamUserVO> resPage = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        return resPage.setRecords(teamUserVOList);
    }


    /**
     * @param teamUpdateRequest
     * @param loginUser
     * @return boolean
     * @description 更新队伍信息
     * @author fangchenxin
     * @date 2024/5/9 00:32
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null || teamUpdateRequest.getId() <= 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        //2、是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinesssException(ErrorCode.NO_AUTH);
        }
        //  2、队伍标题<=20
        String name = teamUpdateRequest.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //  3、描述<=512
        String description = teamUpdateRequest.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        // 校验过期时间
        Date expireTime = teamUpdateRequest.getExpireTime();
        if (expireTime == null || expireTime.before(new Date())) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        Long id = teamUpdateRequest.getId();
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinesssException(ErrorCode.NULL_ERROR);
        }
        if (!Objects.equals(oldTeam.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinesssException(ErrorCode.NO_AUTH);
        }
        // 获取更新队伍的状态
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        // 如果是加密房间，必须设置密码
        if (TeamStatusEnum.SECRET.equals(statusEnum) && StringUtils.isBlank(teamUpdateRequest.getPassword())) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
        }
        Team updateTeam = new Team();
        try {
            BeanUtils.copyProperties(updateTeam, teamUpdateRequest);
        } catch (Exception e) {
            throw new BusinesssException(ErrorCode.SYSTEM_ERROR);
        }
        return this.updateById(updateTeam);
    }

    /**
     * @param teamJoinRequest
     * @param loginUser
     * @return boolean
     * @description 加入队伍
     * @author fangchenxin
     * @date 2024/5/12 15:18
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null || teamJoinRequest.getTeamId() <= 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        // 验证队伍是否存在
        Long teamId = teamJoinRequest.getTeamId();
        // 获取队伍信息
        Team team = this.getTeamById(teamId);
        // 验证要加入的队伍是否过期
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 私有队伍不允许加入
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        // 队伍密码
        String teamPassword = team.getPassword();
        // 申请加入队伍的密码
        String joinPassword = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(joinPassword) || !joinPassword.equals(teamPassword)) {
                throw new BusinesssException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 查询登录用户目前加入了多少队伍
        Long userId = loginUser.getId();
        // 分布式锁redisson
        RLock lock = redissonClient.getLock("player:join_team:" + userId + ":" + teamId);
        try {
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    log.info("getLock: " + Thread.currentThread().getId());
                    // 执行下面逻辑
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum > 5) {
                        throw new BusinesssException(ErrorCode.PARAMS_ERROR, "最多创建和加入5个队伍");
                    }
                    // 不能重复加入已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("teamId", teamId);
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinesssException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }

                    // 已加入队伍的用户个数
                    long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinesssException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 新增用户队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setJoinTime(new Date());
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("joinTeam getLock error", e);
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                log.info("joinTeam unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    /**
     * @param teamQuitRequest
     * @param loginUser
     * @return boolean
     * @description 退出队伍
     * @author fangchenxin
     * @date 2024/5/12 15:17
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null || teamQuitRequest.getTeamId() <= 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        // 获取队伍信息
        Team team = this.getTeamById(teamId);
        Long userId = loginUser.getId();
        if (!this.hasUserJoinTeam(teamId, userId)) {
            return false;
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        // 如果只有一人，直接解散
        if (teamHasJoinNum == 1) {
            // 删除队伍
            this.removeById(teamId);
        } else {
            // 如果是队长
            if (userId.equals(team.getUserId())) {
                // 退出队伍，将队长转移给最早加入的用户
                List<UserTeam> userTeamList = this.getUserTeamListByTeamId(teamId);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinesssException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                team.setUserId(nextTeamLeaderId);
                boolean updateRes = this.updateById(team);
                if (!updateRes) {
                    throw new BusinesssException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        // 删除用户队伍关系
        return this.removeTeamUser(teamId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(DeleteRequest deleteRequest, User loginUser) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = deleteRequest.getId();
        //2、校验队伍是否存在
        Team team = this.getTeamById(teamId);
        //3、校验是不是这个队伍的队长
        if (!loginUser.getId().equals(team.getUserId())) {
            throw new BusinesssException(ErrorCode.NO_AUTH, "无访问权限");
        }
        //4、移除所有加入队伍的关联信息
        boolean res = this.removeTeamUserByTeamId(teamId);
        if (!res) {
            throw new BusinesssException(ErrorCode.SYSTEM_ERROR, "删除用户队伍关系失败");
        }
        //5、删除队伍
        return this.removeById(teamId);
    }

    /**
     * @param teamId
     * @return long
     * @description 获取队伍中已加入的用户数量
     * @author fangchenxin
     * @date 2024/5/9 15:35
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * @param teamId
     * @return boolean
     * @description 删除队伍所有用户关系
     * @author fangchenxin
     * @date 2024/5/9 16:42
     */
    private boolean removeTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.remove(userTeamQueryWrapper);
    }

    /**
     * @param teamId
     * @return java.util.List<com.chenxin.player.model.domain.UserTeam>
     * @description 查询继任队长
     * @author fangchenxin
     * @date 2024/5/9 16:42
     */
    private List<UserTeam> getUserTeamListByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        userTeamQueryWrapper.orderByAsc("id");
        userTeamQueryWrapper.last("limit 2");
        return userTeamService.list(userTeamQueryWrapper);
    }

    /**
     * @param teamId
     * @param userId
     * @return boolean
     * @description 删除用户队伍关系
     * @author fangchenxin
     * @date 2024/5/9 16:45
     */
    private boolean removeTeamUser(long teamId, long userId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        userTeamQueryWrapper.eq("userId", userId);
        return userTeamService.remove(userTeamQueryWrapper);
    }

    /**
     * @param teamId
     * @param userId
     * @return long
     * @description 查询用户是否加入队伍
     * @author fangchenxin
     * @date 2024/5/9 16:46
     */
    private boolean hasUserJoinTeam(long teamId, long userId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        userTeamQueryWrapper.eq("userId", userId);
        long userTeamNum = userTeamService.count(userTeamQueryWrapper);
        if (userTeamNum == 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        return true;
    }

    /**
     * @param teamId
     * @return com.chenxin.player.model.domain.Team
     * @description 获取队伍信息
     * @author fangchenxin
     * @date 2024/5/9 18:16
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinesssException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinesssException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }
}




