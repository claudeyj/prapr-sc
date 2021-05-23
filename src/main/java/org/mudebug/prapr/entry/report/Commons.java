package org.mudebug.prapr.entry.report;

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

import org.mudebug.prapr.core.SuspStrategy;
import org.mudebug.prapr.core.commons.TestCaseUtil;
import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.testapi.Description;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 * @since 1.0.0
 */
public final class Commons {
    private Commons() {

    }

    public static String sanitizeMutatorName(String mutator) {
        mutator = mutator.substring(1 + mutator.lastIndexOf('.'));
        if (Character.isDigit(mutator.charAt(mutator.length() - 1))) {
            final int lastIndex = mutator.lastIndexOf('_');
            mutator = mutator.substring(0, lastIndex);
        }
        return mutator;
    }

    public static double calculateSusp(final SuspStrategy suspStrategy,
                                       final MutationDetails mutationDetails,
                                       Collection<String> failingTests,
                                       final int allTestsCount) {
        final int allFailingTestsCount = failingTests.size();
        failingTests = new HashSet<>(failingTests); // make a copy
        int ef = 0;
        int ep = 0;
        for (final TestInfo ti : mutationDetails.getTestsInOrder()) {
            if (TestCaseUtil.contains(failingTests, ti)) {
                ef++;
                TestCaseUtil.remove(failingTests, ti);
            } else {
                ep++;
            }
        }
        final int nf = failingTests.size();
        final int np = allTestsCount - allFailingTestsCount - ep;
        return suspStrategy.computeSusp(ef, ep, nf, np);
    }

    /**
     * @author: Jun Yang
     */
    @Deprecated
    public static void addContentToPool(String poolPath, String content) {
        FileWriter fw = null;
        try {
          File f=new File(poolPath);
          fw = new FileWriter(f, true);
          } catch (IOException e) {
          e.printStackTrace();
          }
          PrintWriter pw = new PrintWriter(fw);
          pw.println(content);
          pw.flush();
          try {
          fw.flush();
          pw.close();
          fw.close();
          } catch (IOException e) {
          e.printStackTrace();
          }
        }

    /**
     * @author: Jun Yang
     * @param fileName
     * @return
     */
    @Deprecated
    public static String readToString(String fileName) {
      // String encoding = "ISO-8859-1";
      File file = new File(fileName);
      Long filelength = file.length();
      byte[] filecontent = new byte[filelength.intValue()];
      try {
        FileInputStream in = new FileInputStream(file);
        in.read(filecontent);
        in.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      // try {
        // return new String(filecontent, encoding);
        return new String(filecontent);
      // } catch (UnsupportedEncodingException e) {
      //   System.err.println("The OS does not support " + encoding);
      //   e.printStackTrace();
      //   return null;
      // }
    }

    /**
     * 
     * @author: Jun Yang
     */
    @Deprecated
    public static List<String> readToLinesList(String fileName) {
        List<String> linesList = new ArrayList<>();
        
        try {
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                linesList.add(line.replace("\n", ""));
            }
            br.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      return linesList;
    }

    public static <V> Map<String, V> getStringMapFromDescMap(Map<Description, V> descMap)
    {
        Map<String, V> stringMap = new HashMap<>();
        for (Map.Entry<Description, V>entry : descMap.entrySet())
        {
            stringMap.put(entry.getKey().getFirstTestClass() + "." + entry.getKey().getName(), entry.getValue());
        }

        return stringMap;
    }
}