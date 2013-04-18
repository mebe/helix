package org.apache.helix.participant.statemachine;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.reflect.Method;

import org.apache.helix.NotificationContext;
import org.apache.helix.model.Message;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestStateModelParser {

  private static Logger LOG = Logger.getLogger(TestStateModelParser.class);

  @StateModelInfo(initialState = "OFFLINE", states = { "MASTER", "SLAVE", "ERROR" })
  class TestStateModel extends StateModel {
    @Transition(to = "SLAVE", from = "OFFLINE")
    public void onBecomeSlaveFromOffline(Message message, NotificationContext context) {
      LOG.info("Become SLAVE from OFFLINE");
    }
    
    @Transition(to = "DROPPED", from = "ERROR")
    public void onBecomeDroppedFromError(Message message, NotificationContext context) {
      LOG.info("Become DROPPED from ERROR");
    }

  }
  
  @StateModelInfo(initialState = "OFFLINE", states = { "MASTER", "SLAVE", "ERROR" })
  class DerivedTestStateModel extends TestStateModel {
    @Transition(to = "SLAVE", from = "OFFLINE")
    public void derivedOnBecomeSlaveFromOffline(Message message, NotificationContext context) {
      LOG.info("Derived Become SLAVE from OFFLINE");
    }
  }
  
  @Test
  public void test() {
    StateModelParser parser = new StateModelParser();
    TestStateModel testModel = new TestStateModel();
    
    Method method = parser.getMethodForTransitionUsingAnnotation(testModel.getClass(),
        "offline",
        "slave",
        new Class[] { Message.class, NotificationContext.class});
    
    // System.out.println("method-name: " + method.getName());
    Assert.assertEquals(method.getName(), "onBecomeSlaveFromOffline");
  }
  
  @Test
  public void testDerived() {
    StateModelParser parser = new StateModelParser();
    DerivedTestStateModel testModel = new DerivedTestStateModel();
    
    Method method = parser.getMethodForTransitionUsingAnnotation(testModel.getClass(),
        "offline",
        "slave",
        new Class[] { Message.class, NotificationContext.class});
    
    // System.out.println("method-name: " + method.getName());
    Assert.assertEquals(method.getName(), "derivedOnBecomeSlaveFromOffline");
    
    
    method = parser.getMethodForTransitionUsingAnnotation(testModel.getClass(),
        "error",
        "dropped",
        new Class[] { Message.class, NotificationContext.class});

    // System.out.println("method: " + method);
    Assert.assertEquals(method.getName(), "onBecomeDroppedFromError");

  }
}