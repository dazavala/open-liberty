package com.ibm.ws.messaging.JMS20.fat.Checkpoint;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.websphere.simplicity.ShrinkHelper;
import com.ibm.ws.messaging.JMS20security.fat.TestUtils;

import componenttest.rules.repeater.EE8FeatureReplacementAction;
import componenttest.rules.repeater.JakartaEE9Action;
import componenttest.rules.repeater.JakartaEE10Action;
import componenttest.rules.repeater.RepeatTests;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;

import io.openliberty.checkpoint.spi.CheckpointPhase;

public class JMSCheckpointSecurityTest {

    @ClassRule
    public static RepeatTests r = RepeatTests.with( new EE8FeatureReplacementAction() )
                                             .andWith( new JakartaEE9Action().fullFATOnly() )
                                             .andWith( new JakartaEE10Action().fullFATOnly() );

	private static LibertyServer server = LibertyServerFactory.getLibertyServer("TestServer");
	private static LibertyServer server1 = LibertyServerFactory.getLibertyServer("TestServer1");

    private static final int PORT = server.getHttpDefaultPort();
    private static final String HOST = server.getHostname();

    
    // FROM JMSMDBTest
    @BeforeClass
    public static void configMDBTest() throws Exception {

        //Enable messaging, connector and mdb features for InstantOn
        Map<String, String> jvmOptions = server.getJvmOptionsAsMap();
        jvmOptions.put("-Dcom.ibm.ws.beta.edition", "true");
        server.setJvmOptions(jvmOptions);

        server.copyFileToLibertyInstallRoot("lib/features", "features/testjmsinternals-1.0.mf");
        server.copyFileToLibertyServerRoot("resources/security", "clientLTPAKeys/mykey.jks");
        server.setServerConfigurationFile("EJBMDB_server.xml");
        TestUtils.addDropinsWebApp(server, "JMSContextInject", "web");
        TestUtils.addDropinsWebApp(server, "mdbapp", "mdb");
    }

    // FROM JMSMDBTest
    @org.junit.AfterClass
    public static void tearDown() throws Exception {
        if (server.isStarted()) {
            System.out.println("Stopping server");
            server.stopServer();
        }
        server.setJvmOptions(Collections.emptyMap());
        ShrinkHelper.cleanAllExportedArchives();
    }

    // FROM JMSMDBTest
    @Test
    public void testQueueMDB_AAS() throws Exception {

        server.setCheckpoint(CheckpointPhase.AFTER_APP_START, false, null);
        server.startServer("JMSConsumerTestClient.log");

        // Introduce config changes here

        server.checkpointRestore();
        String waitFor = server.waitForStringInLog("CWWKF0011I.*", server.getMatchingLogFile("messages.log"));
        assertNotNull("Server ready message not found", waitFor);

        boolean servletResult = runInServlet("testQueueMDB");
        assertTrue("testQueueMDB failed ", servletResult);

        String msg = server.waitForStringInLog("Message received on Annotated MDB: testQueueMDB", 5000);
        assertNotNull("Test testQueueMDB failed", msg);
    }

    // FROM JMSMDBTest
    private boolean runInServlet(String test) throws IOException {

        URL url = new URL("http://" + HOST + ":" + PORT + "/JMSContextInject?test=" + test);
        System.out.println("The Servlet URL is : " + url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("GET");
            con.connect();
            InputStream is = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String sep = System.lineSeparator();
            StringBuilder lines = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine())
                lines.append(line).append(sep);
            if (lines.indexOf(test + " COMPLETED SUCCESSFULLY") < 0) {
                org.junit.Assert.fail("Missing success message in output. " + lines);
                return false;
            }
            return true;
        } finally {
            con.disconnect();
        }
    }

}
