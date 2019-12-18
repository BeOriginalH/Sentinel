/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.statistic.metric;

import java.util.List;

import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;

/**
 *
 * 一个描述资源调用情况的指标，定义各种获取指标和修改指标的操作接口
 * Represents a basic structure recording invocation metrics of protected resources.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public interface Metric extends DebugSupport {

    /**
     * 总共成功的数量
     * Get total success count.
     *
     * @return success count
     */
    long success();

    /**
     * 最大成功的数量
     * Get max success count.
     *
     * @return max success count
     */
    long maxSuccess();

    /**
     * 异常总数
     * Get total exception count.
     *
     * @return exception count
     */
    long exception();

    /**
     * 阻塞总数
     * Get total block count.
     *
     * @return block count
     */
    long block();

    /**
     * 通过总数
     * Get total pass count. not include {@link #occupiedPass()}
     *
     * @return pass count
     */
    long pass();

    /**
     * 总的响应时间
     * Get total response time.
     *
     * @return total RT
     */
    long rt();

    /**
     * 最小的响应时间
     * Get the minimal RT.
     *
     * @return minimal RT
     */
    long minRt();

    /**
     *
     * Get aggregated metric nodes of all resources.
     *
     * @return metric node list of all resources
     */
    List<MetricNode> details();

    /**
     * Get the raw window array.
     *
     * @return window metric array
     */
    MetricBucket[] windows();

    /**
     * 添加异常总数
     * Add current exception count.
     *
     * @param n count to add
     */
    void addException(int n);

    /**
     * 添加阻塞总数
     * Add current block count.
     *
     * @param n count to add
     */
    void addBlock(int n);

    /**
     * 添加成功总数
     * Add current completed count.
     *
     * @param n count to add
     */
    void addSuccess(int n);

    /**
     * 添加通过总数
     * Add current pass count.
     *
     * @param n count to add
     */
    void addPass(int n);

    /**
     * 添加总的响应时间
     * Add given RT to current total RT.
     *
     * @param rt RT
     */
    void addRT(long rt);

    /**
     * 获取每秒窗口滑动的长度
     * Get the sliding window length in seconds.
     *
     * @return the sliding window length
     */
    double getWindowIntervalInSec();

    /**
     *
     * Get sample count of the sliding window.
     *
     * @return sample count of the sliding window.
     */
    int getSampleCount();

    /**
     * Note: this operation will not perform refreshing, so will not generate new buckets.
     *
     * @param timeMillis valid time in ms
     * @return pass count of the bucket exactly associated to provided timestamp, or 0 if the timestamp is invalid
     * @since 1.5.0
     */
    long getWindowPass(long timeMillis);

    // Occupy-based (@since 1.5.0)

    /**
     * 添加正在占据的数量
     * Add occupied pass, which represents pass requests that borrow the latter windows' token.
     *
     * @param acquireCount tokens count.
     * @since 1.5.0
     */
    void addOccupiedPass(int acquireCount);

    /**
     * 添加正在等待通过的数量
     * Add request that occupied.
     *
     * @param futureTime   future timestamp that the acquireCount should be added on.
     * @param acquireCount tokens count.
     * @since 1.5.0
     */
    void addWaiting(long futureTime, int acquireCount);

    /**
     * 等待通过的数量
     * Get waiting pass account
     *
     * @return waiting pass count
     * @since 1.5.0
     */
    long waiting();

    /**
     * 占据通过的数量
     * Get occupied pass count.
     *
     * @return occupied pass count
     * @since 1.5.0
     */
    long occupiedPass();

    // Tool methods.

    long previousWindowBlock();

    long previousWindowPass();
}
