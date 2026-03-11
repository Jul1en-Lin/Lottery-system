package com.julien.lotterysystem.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PrizeInfoListResponse<T> {
    // 奖品总数
    private Integer total;
    // 当前页列表
    private List<T> records;
}
