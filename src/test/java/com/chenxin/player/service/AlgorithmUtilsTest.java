package com.chenxin.player.service;

import com.chenxin.player.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author fangchenxin
 * @description
 * @date 2024/5/11 15:27
 * @modify
 */
public class AlgorithmUtilsTest {

    @Test
    void test() {
        List<String> list1 = Arrays.asList("python", "女", "JJ", "音乐");
        List<String> list2 = Arrays.asList("Go", "男", "JJ", "音乐");
        List<String> list3 = Arrays.asList("Java", "男", "JJ", "音乐");
        List<String> list4 = Arrays.asList("python", "女", "JJ", "play");
        System.out.println(AlgorithmUtils.minDistance(list1, list2));
        System.out.println(AlgorithmUtils.minDistance(list1, list3));
        System.out.println(AlgorithmUtils.minDistance(list1, list4));
    }
}
