package com.embrace.cup.job;

import com.embrace.cup.zoo.Log;
import com.embrace.cup.zoo.JobExecutor;

public class J001 implements JobExecutor {

    public void execute(String[] params) {
        Log.info(J001.class.getSimpleName(), String.join(",", params) );
    }
}
