/*******************************************************************************
 * Copyright (c) 2020, 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.messaging.JMS20security.fat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.ws.messaging.JMS20.fat.DurableUnshared.DurableUnshared;
import com.ibm.ws.messaging.JMS20.fat.MDB.JMSMDBTest;
import com.ibm.ws.messaging.JMS20.fat.Checkpoint.JMSCheckpointSecurityTest;
import com.ibm.ws.messaging.JMS20security.fat.DCFTest.JMSDefaultConnectionFactorySecurityTest;
import com.ibm.ws.messaging.JMS20security.fat.JMSConsumerTest.JMSConsumerTest;


import componenttest.rules.repeater.EE7FeatureReplacementAction;
import componenttest.rules.repeater.JakartaEE10Action;
import componenttest.rules.repeater.JakartaEE9Action;
import componenttest.rules.repeater.RepeatTests;
import componenttest.topology.impl.LibertyServer;

@RunWith(Suite.class)
@SuiteClasses({
                DummyTest.class,
                JMSConsumerTest.class,
                DurableUnshared.class,
                JMSDefaultConnectionFactorySecurityTest.class,
                JMSMDBTest.class,
                JMSCheckpointSecurityTest.class
})
public class FATSuite {
    @ClassRule
    public static RepeatTests r = RepeatTests.with( new EE7FeatureReplacementAction() )
                                             .andWith( new JakartaEE9Action().fullFATOnly() )
                                             .andWith( new JakartaEE10Action().fullFATOnly() );

    static public void configureEnvVariables(LibertyServer server, Map<String, String> newEnv) throws Exception {
        Properties serverEnvProperties = new Properties();
        serverEnvProperties.putAll(newEnv);
        File serverEnvFile = new File(server.getFileFromLibertyServerRoot("/server.env").getAbsolutePath());
        try (OutputStream out = new FileOutputStream(serverEnvFile)) {
            serverEnvProperties.store(out, "");
        }
    }

}
