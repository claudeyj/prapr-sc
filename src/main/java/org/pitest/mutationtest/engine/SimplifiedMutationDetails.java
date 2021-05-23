package org.pitest.mutationtest.engine;

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
import java.util.HashMap;
import java.util.Map;

import org.pitest.testapi.Description;

@Deprecated
public class SimplifiedMutationDetails implements Serializable
  {
        private static final long serialVersionUID = 1L;

        private MutationIdentifier id;
        private long patchExecutionTime;
        private Map<String, Long> testExecutionTimeMap = new HashMap<>();
        private Map<Description, Long> testExecutionDescTimeMap = new HashMap<>();

        public SimplifiedMutationDetails(MutationDetails md)
        {
            this.id = md.getId();
            this.patchExecutionTime = md.getPatchExecutionTime();
            this.testExecutionTimeMap = md.getTestsTimeMap();
            this.testExecutionDescTimeMap = md.getDescTestsTimeMap();
        }

        public MutationIdentifier getId()
        {
            return this.id;
        }

        public long getPatchExecutionTime()
        {
            return this.patchExecutionTime;
        }

        public Map<String, Long> getTestsTimeMap()
        {
            return this.testExecutionTimeMap;
        }

        public Map<Description, Long> getTestsDescTimeMap()
        {
            return this.testExecutionDescTimeMap;
        }
  }