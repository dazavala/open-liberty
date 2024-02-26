/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package io.openliberty.ejbcontainer.mdb.ra.checkpoint.fat.tests;

import static io.openliberty.ejbcontainer.mdb.checkpoint.fat.FATSuite.getTestMethod;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.ShrinkHelper;
import com.ibm.websphere.simplicity.ShrinkHelper.DeployOptions;
import com.ibm.websphere.simplicity.config.ActivationSpec;
import com.ibm.websphere.simplicity.config.ServerConfiguration;

import componenttest.annotation.Server;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.rules.repeater.FeatureReplacementAction;
import componenttest.rules.repeater.JakartaEE10Action;
import componenttest.rules.repeater.JakartaEE9Action;
import componenttest.rules.repeater.RepeatTests;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.utils.FATServletClient;

@RunWith(FATRunner.class)
//@CheckpointTest
public class AuthDataTest extends FATServletClient {

    final static String SERVER_NAME = "checkpointMsgEndpointServer";

    @ClassRule
    public static RepeatTests r = RepeatTests.with(FeatureReplacementAction.EE8_FEATURES().fullFATOnly().forServers(SERVER_NAME)).andWith(new JakartaEE9Action().conditionalFullFATOnly(FeatureReplacementAction.GREATER_THAN_OR_EQUAL_JAVA_11).forServers(SERVER_NAME)).andWith(new JakartaEE10Action().forServers(SERVER_NAME));

    @Server(SERVER_NAME)
    public static LibertyServer server;

    @BeforeClass
    static public void setupClass() throws Exception {
        server.saveServerConfiguration();
    }

    static final String AUTHDATA_USER = "ACTV1USER";
    static final String AUTHDATA_PASSWORD = "{xor}HhwLCW4PCBs="; // "ACTV1PWD"

    static final String[] IGNORE_REGEX = new String[] { "J2CA8501E:.*Property propertyJ", // Bean property cannot be set
                                                        "CNTR0067W:.*MsgEndpointApp#MsgEndpointEJB.jar#MDBTimedBMTBean", // Mixed transaction types
                                                        "CNTR0067W:.*MsgEndpointApp#MsgEndpointEJB.jar#MDBTimedBMTFailBean",
                                                        "CNTR4015W:.*EndpointBMTNonJMSNoActSpec" }; // Missing activationSpec

    TestMethod testMethod;
    List<String> testMsgs;

    @Before
    public void setupTest() throws Exception {
        testMethod = getTestMethod(TestMethod.class, testName);
        testMsgs = null;

        server.removeAllInstalledAppsForValidation(); // Added by assembleAndDeployEarsWarsRars()
        ShrinkHelper.cleanAllExportedArchives();
        // No need to remove server.env file

        server.setArchiveMarker(testMethod + ".marker");
        server.restoreServerConfiguration();
        assembleAndDeployEarsWarsRars();

//        switch (testMethod) {
//            case testAuthDataUpdate:
//            case testJMSAuthDataUpdate:
//                // Override the endpoint's activationSpec authData at restore
//                server.setCheckpoint(CheckpointPhase.AFTER_APP_START, false, checkpointServer -> {
//                    File serverEnvFile = new File(checkpointServer.getServerRoot() + "/server.env");
//                    try (PrintWriter serverEnvWriter = new PrintWriter(new FileOutputStream(serverEnvFile))) {
//                        serverEnvWriter.println("AUTHDATA_USER=" + AUTHDATA_USER);
//                        serverEnvWriter.println("AUTHDATA_PASSWORD=" + AUTHDATA_PASSWORD);
//                    } catch (FileNotFoundException e) {
//                        throw new UncheckedIOException(e);
//                    }
//                });
//                server.addCheckpointRegexIgnoreMessages(IGNORE_REGEX);
//                server.startServer();
//                break;
//            default:
//                throw new Exception("Missing configuration for " + testName);
//        }
    }

    void assembleAndDeployEarsWarsRars() throws Exception {
        //#################### MsgEndpointApp.ear
        JavaArchive MsgEndpointEJB = ShrinkHelper.buildJavaArchive("MsgEndpointEJB.jar", "io.openliberty.ejbcontainer.fat.msgendpoint.ejb.");
        MsgEndpointEJB = (JavaArchive) ShrinkHelper.addDirectory(MsgEndpointEJB, "test-applications/MsgEndpointEJB.jar/resources");
        WebArchive MsgEndpointWeb = ShrinkHelper.buildDefaultApp("MsgEndpointWeb.war", "io.openliberty.ejbcontainer.fat.msgendpoint.web.");

        EnterpriseArchive MsgEndpointApp = ShrinkWrap.create(EnterpriseArchive.class, "MsgEndpointApp.ear");
        MsgEndpointApp.addAsModule(MsgEndpointEJB).addAsModule(MsgEndpointWeb);
        MsgEndpointApp = (EnterpriseArchive) ShrinkHelper.addDirectory(MsgEndpointApp, "test-applications/MsgEndpointApp.ear/resources");

        ShrinkHelper.exportAppToServer(server, MsgEndpointApp, DeployOptions.SERVER_ONLY);

        server.addInstalledAppForValidation("MsgEndpointApp");

        //#################### AdapterForEJB.jar (RAR implementation)
        JavaArchive AdapterForEJBJar = ShrinkHelper.buildJavaArchive("AdapterForEJB.jar", "com.ibm.ws.ejbcontainer.fat.rar.*");
        ShrinkHelper.exportToServer(server, "ralib", AdapterForEJBJar, DeployOptions.SERVER_ONLY);

        //#################### AdapterForEJB.rar
        ResourceAdapterArchive AdapterForEJBRar = ShrinkWrap.create(ResourceAdapterArchive.class, "AdapterForEJB.rar");
        ShrinkHelper.addDirectory(AdapterForEJBRar, "test-resourceadapters/AdapterForEJB.rar/resources");
        ShrinkHelper.exportToServer(server, "connectors", AdapterForEJBRar, DeployOptions.SERVER_ONLY);
    }

    @After
    public void teardownTest() throws Exception {
        if (server.isStarted()) {
            server.stopServer(IGNORE_REGEX);
        }
    }

    void runTest(String servlet, String... testName) throws Exception {
        if (testName.length > 0)
            FATServletClient.runTest(server, servlet, testName[0]);
        else
            FATServletClient.runTest(server, servlet, getTestMethodSimpleName());
    }

    /**
     * Verify message endpoint activates after server startup using updated authData.
     */
    @Test
    public void testAuthDataUpdate() throws Exception {

        // Updating authData referenced by an activationSpec causes endpoints bound to the activationSpec
        // to deactivate; then the activationSpec service (EndpointActivationService) will rebind using new
        // authData, and the endpoints will reactivate. Since endpoint activation defers to checkpoint
        // restore, endpoints should activate for the first time during restore using the updated authData.

        server.startServer();

        ServerConfiguration config = server.getServerConfiguration();

// Changing AuthData elements does not cause authData elements to change during endpoint activation,
// but it does cause an exception in the app manager, and the application to restart
//        AuthData endpointAuthData = config.getAuthDataElements().getById("endpointAuthData");
//        endpointAuthData.setUser("ACTV1USER");
//        endpointAuthData.setPassword("{xor}HhwLCW4PCBs="); // ACTV1PW

// Changing AuthData reference in this manner
        ActivationSpec actSpec = config.getActivationSpecs().getById("ejb/EndpointRestoreAuthDataNonJMS");
        actSpec.setAuthDataRef("endpointAuthDataUpdate");

        server.setMarkToEndOfLog();
        server.updateServerConfiguration(config);
        server.waitForConfigUpdateInLogUsingMark(Collections.singleton("MsgEndpointApp"));

        // Verify activationSpec authData updates to the values in server.env. This message emits when
        // the JCA runtime invokes RA.endpointActivate(), after the EndpointActivationService has rebinded.
        testMsgs = server.findStringsInLogsUsingMark("JCA activation authData for endpoint named EndpointRestoreAuthDataNonJMS is user=ACTV1USER, password=ACTV1PW",
                                                     server.getDefaultLogFile());
        assertFalse(testMsgs.isEmpty());

        // Verify activation occurs exactly once
        testMsgs = server.findStringsInLogsUsingMark("J2CA8801I: .*ejb/EndpointRestoreAuthDataNonJMS .*MsgEndpointApp#MsgEndpointEJB.jar#EndpointRestoreAuthDataNonJMS",
                                                     server.getDefaultLogFile());
        assertTrue(testMsgs.size() == 2);

        // Verify transactional delivery: CMT + required
        runTest("MsgEndpointWeb/NonJMS_MDServlet", "testAuthDataUpdatesDuringRestoreAAS");
        testMsgs = server.findStringsInLogsUsingMark("EndpointRestoreAuthDataNonJMS is in a global transaction",
                                                     server.getDefaultLogFile());
        assertFalse(testMsgs.isEmpty());
    }

    /**
     * Verify message endpoint activates during restore using updated JMS authData.
     */
//    @Test
//    public void testJMSAuthDataUpdate() throws Exception {
//
//        server.checkpointRestore();
//
//        // AuthData referenced by jmsActivationSpec updated before RA.endpointActivation()
//        testMsgs = server.findStringsInLogsUsingMark("JMS activation authData for endpoint named EndpointRestoreAuthDataJMS is user=ACTV1USER, password=ACTV1PW",
//                                                     server.getDefaultLogFile());
//        assertFalse(testMsgs.isEmpty());
//
//        // Endpoint activated
//        testMsgs = server.findStringsInLogsUsingMark("J2CA8801I: .*ejb/EndpointRestoreAuthDataJMS .*MsgEndpointApp#MsgEndpointEJB.jar#EndpointRestoreAuthDataJMS",
//                                                     server.getDefaultLogFile());
//        assertTrue(testMsgs.size() == 1);
//
//        // Deliver non-transactional message: BMT
//        runTest("MsgEndpointWeb/JMS_MDServlet");
//        testMsgs = server.findStringsInLogsUsingMark("EndpointRestoreAuthDataJMS is in a local transaction",
//                                                     server.getDefaultLogFile());
//        assertFalse(testMsgs.isEmpty());
//    }

    static enum TestMethod {
        testAuthDataUpdate,
        testJMSAuthDataUpdate,
        unknown;
    }

}
