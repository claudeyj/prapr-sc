package org.mudebug.prapr.entry.report.compressedxml;

/*
 * #%L
 * prapr-plugin
 * %%
 * Copyright (C) 2018 - 2019 University of Texas at Dallas
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

import static org.mudebug.prapr.entry.report.compressedxml.Tag.block;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.description;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.index;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.killingTests;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.coveringTests;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.lineNumber;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.methodDescription;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.mutatedClass;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.mutatedMethod;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.mutation;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.mutator;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.suspValue;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.sourceFile;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.patchExecutionTime;
// import static org.mudebug.prapr.entry.report.compressedxml.Tag.testsExecutionTime;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.test;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.time;
import static org.mudebug.prapr.entry.report.compressedxml.Tag.name;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mudebug.prapr.core.SuspStrategy;
import org.mudebug.prapr.entry.report.Commons;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.Option;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.StringUtil;
import org.pitest.util.Unchecked;

enum Tag {
    mutation,
    sourceFile,
    mutatedClass,
    mutatedMethod,
    methodDescription,
    lineNumber,
    mutator,
    index,
    coveringTests,
    killingTests,
    description,
    suspValue,
    block,
    patchExecutionTime,
    test,
    time,
    name;
    // testsExecutionTime;
}

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 * @since 2.0.3
 */
public class CompressedXMLReportListener implements MutationResultListener {
    private final Writer out;
    private final SuspStrategy suspStrategy;
    private final Collection<String> failingTests;
    private final int allTestsCount;
    private final String patchExecutionTimePoolPath = "target/patch-execution-time-pool.txt";
    private final String testsExecutionTimePoolPath = "target/tests-of-patch-execution-time-pool.txt";

    public CompressedXMLReportListener(final CompressedDirectoryResultOutputStrategy outputStrategy,
                                       final SuspStrategy suspStrategy,
                                       final Collection<String> failingTests,
                                       final int allTestsCount) {
        this(outputStrategy.createWriterForFile("mutations.xml.gz"), suspStrategy, failingTests, allTestsCount);
    }

    private CompressedXMLReportListener(final Writer out,
                                       final SuspStrategy suspStrategy,
                                       final Collection<String> failingTests,
                                       final int allTestsCount) {
        this.out = out;
        this.suspStrategy = suspStrategy;
        this.failingTests = failingTests;
        this.allTestsCount = allTestsCount;
    }

    private void writeResult(final ClassMutationResults metaData) {
        for (final MutationResult mutation : metaData.getMutations()) {
            writeMutationResultXML(mutation);
        }
    }

    private void writeMutationResultXML(final MutationResult result) {
        write(makeNode(makeMutationNode(result), makeMutationAttributes(result),
                mutation) + "\n");
    }

    private String makeMutationAttributes(final MutationResult result) {
        return "detected='" + result.getStatus().isDetected() + "' status='"
                + result.getStatus() + "' numberOfTestsRun='"
                + result.getNumberOfTestsRun() + "'";
    }

    private String makeMutationNode(final MutationResult mutation) {
        final MutationDetails details = mutation.getDetails();
        //set execution time
        // details.setPatchExecutionTime(Long.parseLong(getTimeOfExecutions(details)));
        // String tests = createCoveringTestsDesc(details.getTestsInOrder());
        return makeNode(clean(details.getFilename()), sourceFile)
                + makeNode(clean(details.getClassName().asJavaName()), mutatedClass)
                + makeNode(clean(details.getMethod().name()), mutatedMethod)
                + makeNode(clean(details.getId().getLocation().getMethodDesc()), methodDescription)
                + makeNode("" + details.getLineNumber(), lineNumber)
                + makeNode(clean(details.getMutator()), mutator)
                + makeNode("" + details.getFirstIndex(), index)
                + makeNode("" + details.getBlock(), block)
                // + makeNode(createCoveringTestsDesc(details.getTestsInOrder()), coveringTests)
                + makeNode("" + getTestNodes(details.getTestsInOrder(), getTimeOfTestsMap(details)), coveringTests)
                + makeNode(createAllKillingTestDesc(mutation.getStatusTestPair().getKillingTest()), killingTests)
                + makeNode(Double.toString(getSusp(details)), suspValue)
                + makeNode(clean(details.getDescription()), description)
                + makeNode(getTimeOfExecutions(details), patchExecutionTime);
                // + makeNode(getTimeOfTests(details), testsExecutionTime);
    }

    private double getSusp(final MutationDetails details) {
        return Commons.calculateSusp(this.suspStrategy, details, this.failingTests, this.allTestsCount);
    }

    private String clean(final String value) {
        return StringUtil.escapeBasicHtmlChars(value);
    }

    private String makeNode(final String value, final String attributes,
                            final Tag tag) {
        if (value != null) {
            return "<" + tag + " " + attributes + ">" + value + "</" + tag + ">";
        } else {
            return "<" + tag + attributes + "/>";
        }

    }

    private String makeNode(final String value, final Tag tag) {
        if (value != null) {
            return "<" + tag + ">" + value + "</" + tag + ">";
        } else {
            return "<" + tag + "/>";
        }
    }

    private String createCoveringTestsDesc(final List<TestInfo> tio) {
        if (!tio.isEmpty()) {
            final String temp = tio.toString();
            return clean(temp.substring(1, temp.length() - 1));
        } else {
            return null;
        }
    }


    private String createAllKillingTestDesc(final Option<String> killingTest) {
        if (killingTest.hasSome()) {
            final String s = killingTest.value();
            return clean(s);
        } else {
            return null;
        }
    }

    private void write(final String value) {
        try {
            this.out.write(value);
        } catch (final IOException e) {
            throw Unchecked.translateCheckedException(e);
        }
    }

    /**
     * get patch execution time information from pool
     * Author: Jun Yang
     */
    private String getTimeOfExecutions(MutationDetails md)
    {
        String targetDetails = md.toString();
        String pool = Commons.readToString(this.patchExecutionTimePoolPath).trim();
        String[] mutations = pool.split("\n");
        for (String mutation : mutations)
        {
            String details = mutation.split("\t")[0];
            String time = mutation.split("\t")[1];
            if (details.equals(targetDetails))
            {
                return time;
            }
        }
        return null;
    }

    /**
     * get time of each test on patch
     * Author : Jun Yang
     */
    private Map<String, String> getTimeOfTestsMap(MutationDetails md)
    {
        String targetDetails = md.toStringNoTestsInOrder();
        String pool = Commons.readToString(this.testsExecutionTimePoolPath).trim();
        String[] mutations = pool.split("\n");
        Map<String, String> testTimeMap = new HashMap<>();
        for (String mutation : mutations)
        {
            String mutationDetails = mutation.split("\t")[0];
            String testDescription = mutation.split("\t")[1];
            String time = mutation.split("\t")[2];
            if (mutationDetails.equals(targetDetails))
            {
                testTimeMap.put(testDescription, time);
            }
        }
        return testTimeMap;
    }

    /**
     * Prepare to make test time nodes
     * Author: Jun Yang
     */
    private String getTestNodes(List<TestInfo> tis, Map<String, String> testTimeMap)
    {
        String testNodes = "";
        for (TestInfo ti : tis)
        {
            // String testNode = makeNode(testTimeMap.get(ti.toString()), te, coveringTests);
            String nameNode = makeNode(ti.toString(), name);
            String timeNode = makeNode(testTimeMap.get(ti.toString()), time);
            String testNode = makeNode(nameNode + timeNode, test);
            testNodes += testNode;
        }
        return testNodes;
    }

    @Override
    public void runStart() {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        write("<mutations>\n");
    }

    @Override
    public void handleMutationResult(final ClassMutationResults metaData) {
        writeResult(metaData);
    }

    @Override
    public void runEnd() {
        try {
            write("</mutations>\n");
            this.out.close();
        } catch (final IOException e) {
            throw Unchecked.translateCheckedException(e);
        }
    }
}
