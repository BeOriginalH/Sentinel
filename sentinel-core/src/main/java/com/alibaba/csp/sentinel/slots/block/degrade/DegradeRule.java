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

package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * Degrade is used when the resources are in an unstable state, these resources
 * will be degraded within the next defined time window. There are two ways to
 * measure whether a resource is stable or not:
 * </p>
 * <ul>
 * <li>
 * Average response time ({@code DEGRADE_GRADE_RT}): When
 * the average RT exceeds the threshold ('count' in 'DegradeRule', in milliseconds), the
 * resource enters a quasi-degraded state. If the RT of next coming 5
 * requests still exceed this threshold, this resource will be downgraded, which
 * means that in the next time window (defined in 'timeWindow', in seconds) all the
 * access to this resource will be blocked.
 * </li>
 * <li>
 * Exception ratio: When the ratio of exception count per second and the
 * success qps exceeds the threshold, access to the resource will be blocked in
 * the coming window.
 * </li>
 * </ul>
 *
 * @author jialiang.linjl
 */
public class DegradeRule extends AbstractRule {

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("sentinel-degrade-reset-task", true));

    public DegradeRule() {
    }

    public DegradeRule(String resourceName) {
        setResource(resourceName);
    }

    /**
     * RT threshold or exception ratio threshold count.
     */
    private double count;

    /**
     * Degrade recover timeout (in seconds) when degradation occurs.
     */
    private int timeWindow;

    /**
     * Degrade strategy (0: average RT, 1: exception ratio, 2: exception count).
     */
    private int grade = RuleConstant.DEGRADE_GRADE_RT;

    /**
     * 每秒钟连续进入的请求超的平均响应时间超过阀值的请求数
     * Minimum number of consecutive slow requests that can trigger RT circuit breaking.
     *
     * @since 1.7.0
     */
    private int rtSlowRequestAmount = RuleConstant.DEGRADE_DEFAULT_SLOW_REQUEST_AMOUNT;

    /**
     * 每秒钟连续进入的请求发生异常的请求数
     * Minimum number of requests (in an active statistic time span) that can trigger circuit breaking.
     *
     * @since 1.7.0
     */
    private int minRequestAmount = RuleConstant.DEGRADE_DEFAULT_MIN_REQUEST_AMOUNT;

    public int getGrade() {
        return grade;
    }

    public DegradeRule setGrade(int grade) {
        this.grade = grade;
        return this;
    }

    public double getCount() {
        return count;
    }

    public DegradeRule setCount(double count) {
        this.count = count;
        return this;
    }

    public int getTimeWindow() {
        return timeWindow;
    }

    public DegradeRule setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
        return this;
    }

    public int getRtSlowRequestAmount() {
        return rtSlowRequestAmount;
    }

    public DegradeRule setRtSlowRequestAmount(int rtSlowRequestAmount) {
        this.rtSlowRequestAmount = rtSlowRequestAmount;
        return this;
    }

    public int getMinRequestAmount() {
        return minRequestAmount;
    }

    public DegradeRule setMinRequestAmount(int minRequestAmount) {
        this.minRequestAmount = minRequestAmount;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DegradeRule that = (DegradeRule) o;
        return Double.compare(that.count, count) == 0 &&
                timeWindow == that.timeWindow &&
                grade == that.grade &&
                rtSlowRequestAmount == that.rtSlowRequestAmount &&
                minRequestAmount == that.minRequestAmount;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + new Double(count).hashCode();
        result = 31 * result + timeWindow;
        result = 31 * result + grade;
        result = 31 * result + rtSlowRequestAmount;
        result = 31 * result + minRequestAmount;
        return result;
    }

    @Override
    public String toString() {
        return "DegradeRule{" +
                "resource=" + getResource() +
                ", grade=" + grade +
                ", count=" + count +
                ", limitApp=" + getLimitApp() +
                ", timeWindow=" + timeWindow +
                ", rtSlowRequestAmount=" + rtSlowRequestAmount +
                ", minRequestAmount=" + minRequestAmount +
                "}";
    }

    // Internal implementation (will be deprecated and moved outside).

    private AtomicLong passCount = new AtomicLong(0);

    /**
     * 标识是否在降级的时间窗口内
     */
    private final AtomicBoolean cut = new AtomicBoolean(false);

    /**
     * 规则检查
     *
     * @param context      current {@link Context} 上下文
     * @param node         current {@link com.alibaba.csp.sentinel.node.Node} 节点
     * @param acquireCount
     * @param args         arguments of the original invocation.
     * @return
     */
    @Override
    public boolean passCheck(Context context, DefaultNode node, int acquireCount, Object... args) {
        //如果是在限流的窗口时间内，直接降级
        if (cut.get()) {
            return false;
        }

        //降级只针对resource维度进行，不区分context，不区分orgin，在statisticSlot中会在defaultNode里面对clusterNode进行累加
        ClusterNode clusterNode = ClusterBuilderSlot.getClusterNode(this.getResource());
        if (clusterNode == null) {
            return true;
        }

        if (grade == RuleConstant.DEGRADE_GRADE_RT) {

            //case1：rt降级

            //平均响应时间
            double rt = clusterNode.avgRt();

            //平均响应时间小于设置的阀值，直接通过
            //todo 如何计算平均响应时间的？
            if (rt < this.count) {
//           System.out.println("threadName: "+Thread.currentThread().getName()+",time: "+ TimeUtil.currentTimeMillis() +", rt: "+rt +",count: "+passCount.get());

                passCount.set(0);
                return true;
            }


            // Sentinel will degrade the service only if count exceeds.
            //请求数自增，如果请求数小于设置的值5，直接通过
            if (passCount.incrementAndGet() < rtSlowRequestAmount) {
//                System.out.println("threadName: "+Thread.currentThread().getName()+", rt: "+rt +",count: "+passCount.get());

                return true;
            }


        } else if (grade == RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO) {

            //case2 异常比例

            //todo 数据如何统计的，看完统计的代码再来看这个
            double exception = clusterNode.exceptionQps();
            double success = clusterNode.successQps();
            double total = clusterNode.totalQps();
            // If total amount is less than minRequestAmount, the request will pass.
            //如果请求总数小于设置的每秒允许的最小请求数量，直接返回
            if (total < minRequestAmount) {
                return true;
            }

            // In the same aligned statistic time window,
            // "success" (aka. completed count) = exception count + non-exception count (realSuccess)
            double realSuccess = success - exception;
            if (realSuccess <= 0 && exception < minRequestAmount) {
                return true;
            }

            if (exception / success < count) {
                return true;
            }
        } else if (grade == RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {

            //case3: 异常数

            //统计异常总数的时间窗口是分钟级别的，如果timeWindow的时间小于60s，会以60s进行熔断
            //todo 如何统计总共的异常数据的？
            double exception = clusterNode.totalException();
            //如果异常数小于指定的值，直接返回
            if (exception < count) {
                return true;
            }
        }

        //开启一个定时任务，在指定的窗口时间后执行，将请求数设置为0，降级标识设置为false
        if (cut.compareAndSet(false, true)) {
            ResetTask resetTask = new ResetTask(this);
            pool.schedule(resetTask, timeWindow, TimeUnit.SECONDS);
        }

        return false;
    }

    /**
     * 定时任务，复位请求数和降级标识
     */
    private static final class ResetTask implements Runnable {

        private DegradeRule rule;

        ResetTask(DegradeRule rule) {
            this.rule = rule;
        }

        @Override
        public void run() {
            rule.passCount.set(0);
            rule.cut.set(false);
        }
    }
}
