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
package com.alibaba.csp.sentinel.slots.block;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;

/**
 * 定义所有规则的基本接口
 * Base interface of all rules.
 *
 * @author youji.zj
 */
public interface Rule {

    /**
     * 检查当前的统计指标是否符合指定的规则，即不超过任何阈值
     * Check whether current statistical indicators（统计指标） meet this rule, which means not exceeding any threshold.
     *
     * @param context current {@link Context} 上下文
     * @param node    current {@link com.alibaba.csp.sentinel.node.Node} 节点
     * @param count   tokens needed.
     * @param args    arguments of the original invocation.
     * @return If current statistical indicators not exceeding any threshold return true, otherwise return false.
     */
    boolean passCheck(Context context, DefaultNode node, int count, Object... args);

}
