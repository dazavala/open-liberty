/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.kernel.feature.fat;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import componenttest.annotation.ExpectedFFDC;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;

@RunWith(FATRunner.class)
public class CompatibilityTest {

    private static LibertyServer server = LibertyServerFactory.getLibertyServer("com.ibm.ws.kernel.feature.compatibility");

    @BeforeClass
    public static void installFeatures() throws Exception {
        // Renamed ee9 feature/prog-model (e.g. faces in jakartaee) with equivalent versions in ee6..8 (e.g. jsf in javaee)
        server.installSystemFeature("test.compatibility.faces-2.4");
        server.installSystemBundle("test.compatibility.faces");
        // ee9 feature/prog-model
        server.installSystemFeature("test.compatibility.newEe9-1.0");
        server.installSystemBundle("test.compatibility.newEe9");
        server.installSystemFeature("test.compatibility.needsNewEe9-1.0");
    }

    @AfterClass
    public static void uninstallFeatures() throws Exception {
//        server.uninstallSystemFeature("test.compatibility.faces-2.4");
//        server.uninstallSystemBundle("test.compatibility.faces");
//        server.uninstallSystemFeature("test.compatibility.newEe9-1.0");
//        server.uninstallSystemBundle("test.compatibility.newEe9");
//        server.uninstallSystemFeature("test.compatibility.needsNewEe9-1.0");
    }

    @After
    public void tearDown() throws Exception {
        if (server != null && server.isStarted()) {
            server.stopServer();
        }
    }

    private static String msg;

    // singleton toleration conflict msg regex's
    static final String GENERAL_CONFLICT = "CWWKF0033E.*",
                    SAME_EE_PLTFRM_CONFLICT = "CWWKF0043E.*",
                    DIFF_EE_PLTFRM_CONFLICT = "CWWKF0044E.*",
                    ANY_CONFLICT = GENERAL_CONFLICT + "|" + SAME_EE_PLTFRM_CONFLICT + "|" + DIFF_EE_PLTFRM_CONFLICT;

    private static String installedFeatures;

    private static long shortTimeOut = 3 * 1000;

    /**
     * Ensure changes to ee compatibility support do not cause the server to
     * report unexpected conflicts.
     */
    @Test
    public void testCompatibleEEFeaturesAnyPlatform() throws Exception {

        // Verify compatible javaeeX features
        server.changeFeatures(Arrays.asList("jca-1.7", "jsf-2.3")); // jca={javaee7,javaee8}, jsf={javaee8}
        server.startServer();
        msg = server.waitForStringInLogUsingMark(ANY_CONFLICT, shortTimeOut);
        assertTrue("The feature manager should not report any conflict for the configured ee{7,8} (javaee) featuress, but it did: msg=" + msg,
                   msg == null);
        server.stopServer();

        // Verify compatible jakartaX features.
        server.changeFeatures(Arrays.asList("newEE9-1.0", "faces-2.4"));
        server.startServer();
        msg = server.waitForStringInLogUsingMark(ANY_CONFLICT, shortTimeOut);
        assertTrue("The feature manager should not report any conflict for the configured ee9 (jakartaee) features, but it did: msg=" + msg,
                   msg == null);
        server.stopServer();
    }

    @Test
    @ExpectedFFDC("java.lang.IllegalArgumentException")
    public void testIncompatibleEeFeaturesSamePlatform() throws Exception {
        // Conflicting root features: servlet-4.0 of javaee8, servlet-3.1 of javaee7
        server.changeFeatures(Arrays.asList("servlet-4.0", "servlet-3.1"));
        server.startServer();
        msg = server.waitForStringInLogUsingMark(GENERAL_CONFLICT, shortTimeOut);
        assertTrue("The feature manager should report a general toleration conflict for two versions of the same singleton javaee feature, but it did not: msg=" + msg,
                   msg != null && msg.contains("servlet-4.0") && msg.contains("servlet-3.1"));
        msg = server.waitForStringInLogUsingMark(SAME_EE_PLTFRM_CONFLICT, shortTimeOut);
        assertTrue("The feature manager should not report an EE compatibility conflict for the configured javaee features, but it did: msg=" + msg,
                   msg == null);
        installedFeatures = TestUtils.getInstalledFeatures(server);
        assertTrue("The server should not install feature servlet-4.0 nor servlet-3.1 , but it did: " + installedFeatures,
                   installedFeatures == null || !installedFeatures.contains("servlet-4.0") && !installedFeatures.contains("servlet-3.1"));
        server.stopServer(ANY_CONFLICT + "|CWWKE0702E.*");

        // Conflicting non-root features: jsp-2.2 of javaee6, jsfContainer-2.3 of javaee7
        server.changeFeatures(Arrays.asList("jsp-2.2", "jsfContainer2.3"));
        server.startServer();
        // One of several included feature conflicts
        msg = server.waitForStringInLogUsingMark(GENERAL_CONFLICT, shortTimeOut);
        assertTrue("The feature manager should report a general toleration conflict for two versions of the same singleton javaee feature, but it did not: msg=" + msg,
                   msg != null && msg.contains("servlet-3.0") && msg.contains("servlet-3.1"));
        msg = server.waitForStringInLogUsingMark(SAME_EE_PLTFRM_CONFLICT, shortTimeOut);
        assertTrue("The feature manager should report an EE compatibility conflict for the configured javaee features, but it did not: msg=" + msg,
                   msg != null && msg.contains("jsp-2.2") && msg.contains("jspContainer-2.3"));
        installedFeatures = TestUtils.getInstalledFeatures(server);
        assertTrue("The server should not install neither feature servlet-3.0 nor servlet-3.1 , but it did: " + installedFeatures,
                   installedFeatures == null || !installedFeatures.contains("servlet-3.0") && !installedFeatures.contains("servlet-3.1"));
        server.stopServer(ANY_CONFLICT + "|CWWKE0702E.*");
    }

    @Test
    @ExpectedFFDC("java.lang.IllegalArgumentException")
    public void testIncompatibleEeFeaturesDifferentPlatform() throws Exception {
        // Equivalent root features in incompatible ee platforms
        server.changeFeatures(Arrays.asList("faces-2.4", "jsf-2.3"));
        server.startServer();
        msg = server.waitForStringInLogUsingMark(DIFF_EE_PLTFRM_CONFLICT, shortTimeOut);
        assertTrue("The feature manager should report an EE compatibility conflict for the configured jakarta and javaee features, but it did not: msg=" + msg,
                   msg != null && msg.contains("faces-2.4") && msg.contains("jsf-2.3"));
        installedFeatures = TestUtils.getInstalledFeatures(server);
        assertTrue("The server should not install neither feature faces-2.4 nor jsf-2.3 , but it did: " + installedFeatures,
                   installedFeatures == null || !installedFeatures.contains("faces-2.4") && !installedFeatures.contains("jsf-2.3"));
        server.stopServer(ANY_CONFLICT + "|CWWKE0702E.*");

        // Non-equivalent root features in incompatible ee platforms
        server.changeFeatures(Arrays.asList("newEe9-1.0", "jsf-2.3"));
        server.startServer();
        msg = server.waitForStringInLogUsingMark(DIFF_EE_PLTFRM_CONFLICT, shortTimeOut);
        assertTrue("The feature manager should report an EE compatibility conflict for the configured jakarta and javaee features, but it did not: msg=" + msg,
                   msg != null && msg.contains("newEe9-1.0") && msg.contains("jsf-2.3"));
        installedFeatures = TestUtils.getInstalledFeatures(server);
        assertTrue("The server should not install neither feature newEe9-1.0 nor jsf-2.3 , but it did: " + installedFeatures,
                   installedFeatures == null || !installedFeatures.contains("newEe9-1.0") && !installedFeatures.contains("jsf-2.3"));
        server.stopServer(ANY_CONFLICT + "|CWWKE0702E.*");

        // Non-equivalent root features in incompatible ee platforms
        server.changeFeatures(Arrays.asList("needsNewEe9-1.0", "jsf-2.3"));
        server.startServer();
        msg = server.waitForStringInLogUsingMark(DIFF_EE_PLTFRM_CONFLICT, shortTimeOut);
        assertTrue("The feature manager should report an EE compatibility conflict for the configured jakarta and javaee features, but it did not: msg=" + msg,
                   msg != null && msg.contains("newEe9-1.0") && msg.contains("jsf-2.3"));
        installedFeatures = TestUtils.getInstalledFeatures(server);
        assertTrue("The server should not install neither feature newEe9-1.0 nor jsf-2.3 , but it did: " + installedFeatures,
                   installedFeatures == null || !installedFeatures.contains("newEe9-1.0") && !installedFeatures.contains("jsf-2.3"));
        server.stopServer(ANY_CONFLICT + "|CWWKE0702E.*");
    }

}