package org.pitest.mutationtest.execute;

/*
 * #%L
 * prapr-plugin
 * %%
 * Copyright (C) 2018 - 2021 University of Texas at Dallas
 * %%
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
 * #L%
 */


import java.util.HashMap;
import java.util.Map;

import org.pitest.functional.Option;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestResult;
import org.pitest.util.Log;
import java.util.logging.Logger;


public class CheckTestHasFailedResultWithTimingListener extends CheckTestHasFailedResultListener{
    private Option<Description>          lastFailingTest        = Option.none();
    private int                          testsRun               = 0;
    private Map<Description, Long>       testsDescExecutionTimeMap  = new HashMap<>();
    // private long                         mutationExecutionTime  = 0;
    private static final Logger          LOG                    = Log.getLogger();

    @Override
    public void onTestFailure(final TestResult tr) {
        this.lastFailingTest = Option.some(tr.getDescription());
    }

    @Override
    public void onTestSkipped(final TestResult tr) {

    }

    @Override
    public void onTestStart(final Description d) {
        this.testsRun++;
    }

    @Override
    public void onTestSuccess(final TestResult tr) {
    }

    public DetectionStatus status() {
        if (this.lastFailingTest.hasSome()) {
          return DetectionStatus.KILLED;
        } else {
          return DetectionStatus.SURVIVED;
        }
      }
    
    public Option<Description> lastFailingTest() {
    return this.lastFailingTest;
    }

    public int getNumberOfTestsRun() {
    return this.testsRun;
    }

    @Override
    public void onRunEnd() {

    }

    @Override
    public void onRunStart() {

    }

    public void setDescTestsExecutionTime(Map<Description, Long> testsDescExecutionTimeMap)
    {
        this.testsDescExecutionTimeMap = testsDescExecutionTimeMap;
    }

    public Map<Description, Long> getDescTestsExecutionTime()
    {
        return this.testsDescExecutionTimeMap;
    }

    // @Deprecated
    // public long getMutationExecutionTime()
    // {
    //     return this.mutationExecutionTime;
    // }

    // @Deprecated
    // public void setMutationExecutionTime(long mutationExecutionTime)
    // {
    //     this.mutationExecutionTime = mutationExecutionTime;
    // }
    
}
