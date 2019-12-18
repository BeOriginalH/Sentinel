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

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

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
        rule.setCount(20);
        rule.setLimitApp("app1");
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
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

    public static void main(String[] args)   throws Exception{
        ContextUtil.enter("context1","orgin1");
        Entry entry = SphU.entry("resource1");
        SphU.entry("resource1");
        SphU.entry("resource2'");
        SphU.entry("resource2");
        SphU.entry("resource3");


    }

//    public static void main(String[] args) {
//        // 配置规则.
//        initFlowRules();
//
//        Entry entry = null;
//        Entry entry1 = null;
//        Entry entry3 = null;
//        Entry entry2 = null;
//
//        new Thread(new Runnable(){
//
//            @Override
//            public void run() {
//
//                ContextUtil.enter("context1", "app1");
//                Entry helloWorld = null;
//                try {
//                    helloWorld = SphU.entry("HelloWorld");
//                } catch (Exception e) {
//                    System.out.println(e);
//                } finally {
//                    helloWorld.exit();
//                }
//                System.out.println("test");
//            }
//        }).start();
//
//        new Thread(new Runnable(){
//
//            @Override
//            public void run() {
//
//                ContextUtil.enter("context2", "app1");
//                Entry helloWorld = null;
//                try {
//                    helloWorld = SphU.entry("HelloWorld");
//                } catch (Exception e) {
//                    System.out.println(e);
//                } finally {
//                    helloWorld.exit();
//                }
//                System.out.println("test");
//            }
//        }).start();
//
//    }

}


