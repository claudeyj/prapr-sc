package org.pitest.testapi.execute;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.mudebug.prapr.entry.report.Commons;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;
import org.pitest.util.Log;
import org.pitest.util.PitError;

public class Pitest {

  private static final Logger                LOG = Log.getLogger();

  private final TestListener listener;

  public Pitest(final TestListener listener) {
    this.listener = listener;
  }

  // entry point for mutation testing
  public void run(final Container container,
      final List<? extends TestUnit> testUnits) {

    LOG.fine("Running " + testUnits.size() + " units");

    signalRunStartToAllListeners();

    executeTests(container, testUnits);

    signalRunEndToAllListeners();

  }

  //new entry point here!
  public void run(final Container container,
      final List<? extends TestUnit> testUnits, Mutant mutedClass) {

    LOG.fine("Running " + testUnits.size() + " units");

    signalRunStartToAllListeners();

    executeTests(container, testUnits, mutedClass);

    signalRunEndToAllListeners();

  }

  private void executeTests(final Container container,
      final List<? extends TestUnit> testUnits) {
    for (final TestUnit unit : testUnits) {
        long t0 = System.currentTimeMillis();
        final List<TestResult> results = container.execute(unit);
        processResults(results);
    }
  }

  //new funtion here!
  private void executeTests(final Container container,
      final List<? extends TestUnit> testUnits, Mutant mutedClass) {
    for (final TestUnit unit : testUnits) {
        final List<TestResult> results = container.execute(unit);
        for (TestResult tr : results)
        {
            if (tr.getDescription().getName().startsWith("testExecutionTime:"))
            {
                String descName = tr.getDescription().getName();
                String test = descName.substring(descName.lastIndexOf(":") + 1);
                String testFullName = test.split("-")[0];
                String testTime = test.split("-")[1];
                String testClassName = testFullName.split("#")[0];
                String testShortName = testFullName.split("#")[1];
                mutedClass.getDetails().addTestTime(new Description(testShortName, testClassName), Long.parseLong(testTime));
            }
        }
        processResults(results);
    }
  }

  // much of the legacy test suite exercises the system via this method
  @Deprecated
  public void run(final Container defaultContainer, final Configuration config,
      final Class<?>... classes) {
    run(defaultContainer, config, Arrays.asList(classes));
  }

  private void run(final Container container, final Configuration config,
      final Collection<Class<?>> classes) {

    final FindTestUnits find = new FindTestUnits(config);
    run(container, find.findTestUnitsForAllSuppliedClasses(classes));
  }

  private void processResults(final List<TestResult> results) {
    for (final TestResult result : results) {
      if (result.getDescription().getName().startsWith("testExecutionTime-")) continue;
      final ResultType classifiedResult = classify(result);
      classifiedResult.getListenerFunction(result).apply(listener);
    }
  }

  private void signalRunStartToAllListeners() {
    listener.onRunStart();
  }

  private void signalRunEndToAllListeners() {
    listener.onRunEnd();
  }

  private ResultType classify(final TestResult result) {

    switch (result.getState()) {
    case STARTED:
      return ResultType.STARTED;
    case NOT_RUN:
      return ResultType.SKIPPED;
    case FINISHED:
      return classifyFinishedTest(result);
    default:
      throw new PitError("Unhandled state");
    }

  }

  private ResultType classifyFinishedTest(final TestResult result) {
    if (result.getThrowable() != null) {
      return ResultType.FAIL;
    } else {
      return ResultType.PASS;
    }
  }
}