package org.pitest.mutationtest;

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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.pitest.functional.Option;
import org.pitest.testapi.Description;

public final class MutationStatusTestPair implements Serializable {
  
  private static final long serialVersionUID = 1L;

  private final int              numberOfTestsRun;
  private final DetectionStatus  status;
  private final Option<String>   killingTest;
  private long                   mutationExecutionTime = 0;
  private Map<Description, Long> testsDescExecutionTimeMap = new LinkedHashMap();

  public MutationStatusTestPair(final int numberOfTestsRun,
      final DetectionStatus status) {
    this(numberOfTestsRun, status, null);
  }

  public MutationStatusTestPair(final int numberOfTestsRun,
      final DetectionStatus status, final String killingTest) {
    this.status = status;
    this.killingTest = Option.some(killingTest);
    this.numberOfTestsRun = numberOfTestsRun;
  }

  public DetectionStatus getStatus() {
    return this.status;
  }

  public Option<String> getKillingTest() {
    return this.killingTest;
  }

  public int getNumberOfTestsRun() {
    return this.numberOfTestsRun;
  }

  public void setDescTestsExecutionTime(Map<Description, Long> testsDescExecutionTimeMap)
  {
      this.testsDescExecutionTimeMap = testsDescExecutionTimeMap;
  }

  public Map<Description, Long> getTestsExecutionTime()
  {
      return this.testsDescExecutionTimeMap;
  }

  public long getMutationExecutionTime()
  {
      return this.mutationExecutionTime;
  }

  public void setMutationExecutionTime(long mutationExecutionTime)
  {
      this.mutationExecutionTime = mutationExecutionTime;
  }

  @Override
  public String toString() {
    if (this.killingTest.hasNone()) {
      return this.status.name();
    } else {
      return this.status.name() + " by " + this.killingTest.value();
    }

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.killingTest == null) ? 0 : this.killingTest.hashCode());
    result = (prime * result) + this.numberOfTestsRun;
    result = (prime * result)
        + ((this.status == null) ? 0 : this.status.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MutationStatusTestPair other = (MutationStatusTestPair) obj;
    if (this.killingTest == null) {
      if (other.killingTest != null) {
        return false;
      }
    } else if (!this.killingTest.equals(other.killingTest)) {
      return false;
    }
    if (this.numberOfTestsRun != other.numberOfTestsRun) {
      return false;
    }
    if (this.status != other.status) {
      return false;
    }
    return true;
  }

}
