package com.alibaba.csp.sentinel.demo.flow;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author harvey
 * @date 2019/10/29
 * @email husy7@midea.com
 */
public class FlowSimpleDemo{

    private static void initFlowRules() {

        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("HelloWorld");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 20.
        rule.setCount(20);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

    private static void initFlowRules1() {

        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("HelloWorld1");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 20.
        rule.setCount(10);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }
    private static void initFlowRules2() {

        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("HelloWorld2");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 20.
        rule.setCount(11);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

    public static void main(String[] args) {
        // 配置规则.
        initFlowRules();
        initFlowRules1();
        initFlowRules2();

        while (true) {
            Entry entry = null;
            Entry entry1 = null;
            Entry entry3 = null;
            Entry entry2 = null;

            Context context1 = ContextUtil.enter("context1");


            ContextUtil.exit();
            Context context2 = ContextUtil.enter("context2");


            try {

                entry = SphU.entry("HelloWorld");
                System.out.println("hello world");

                entry2 = SphU.entry("HelloWorld1");
                System.out.println("hello world");

                entry1 = SphU.entry("HelloWorld");
                System.out.println("hello world");

                entry3 = SphU.entry("HelloWorld1");
                System.out.println("hello world");



            } catch (BlockException e1) {
                System.out.println("blocked!");
            } finally {
                if (entry3 != null) {
                    entry3.exit();
                }
                if (entry1 != null) {
                    entry1.exit();
                }
                if (entry2 != null) {
                    entry2.exit();
                }
                if (entry != null) {
                    entry.exit();
                }
                ContextUtil.exit();
            }

        }
    }
}


