package com.chenxin.player.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxin.player.model.domain.Tag;
import com.chenxin.player.service.TagService;
import com.chenxin.player.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author fangchenxin
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-04-24 22:01:45
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




