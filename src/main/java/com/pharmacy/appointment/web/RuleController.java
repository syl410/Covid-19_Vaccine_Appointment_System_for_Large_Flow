package com.pharmacy.appointment.web;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class RuleController {
    private static final int MAX_VISITS = 50000;
    private static final int FAST_VISITS = 1;

    @PostConstruct
    public void appointmentsFlow(){
        //1, create a list to keep rules of rate limit
        List<FlowRule> rules = new ArrayList<>();
        //2, create main page rule to prevent visiting main page too fast
        FlowRule mainPageRule = new FlowRule();
        // - define proceted resource
        mainPageRule.setResource("appointments");
        // - define rate limit type
        mainPageRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // - define QPS
        mainPageRule.setCount(FAST_VISITS);

        // 3, create rule to limit number of visit of scheuling page
        FlowRule schedulingLimitRule = new FlowRule();
        schedulingLimitRule.setResource("scheduling");
        schedulingLimitRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        schedulingLimitRule.setCount(MAX_VISITS);

        // 4, all rules to the list
        rules.add(mainPageRule);
        rules.add(schedulingLimitRule);
        // 5, load the rules
        FlowRuleManager.loadRules(rules);
    }
}