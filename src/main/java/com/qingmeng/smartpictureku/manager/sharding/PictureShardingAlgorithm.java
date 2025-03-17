package com.qingmeng.smartpictureku.manager.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * &#064;description: 自定义分表算法
 *
 * @author Wang
 * &#064;date: 2025/3/16
 */
public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        Long spaceId = preciseShardingValue.getValue();
        String logicTableName = preciseShardingValue.getLogicTableName();
        // 如果spaceId 为null,公共空间,查询逻辑表(原表)
        if (spaceId == null) {
            return logicTableName;
        }
        // 根据spaceId 动态生成分片表
        String realTableName = "picture_" + spaceId;
        // 如果
        if (collection.contains(realTableName)) {
            return realTableName;
        }else{
            return logicTableName;
        }
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        return List.of();
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties properties) {

    }
}
