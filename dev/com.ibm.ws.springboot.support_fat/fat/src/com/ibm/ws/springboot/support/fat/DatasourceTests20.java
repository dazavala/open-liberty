/**
 *
 */
package com.ibm.ws.springboot.support.fat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.config.ConfigElementList;
import com.ibm.websphere.simplicity.config.DataSource;
import com.ibm.websphere.simplicity.config.Fileset;
import com.ibm.websphere.simplicity.config.JdbcDriver;
import com.ibm.websphere.simplicity.config.KeyStore;
import com.ibm.websphere.simplicity.config.Library;
import com.ibm.websphere.simplicity.config.ServerConfiguration;
import com.ibm.websphere.simplicity.config.dsprops.Properties_derby_embedded;

import componenttest.custom.junit.runner.FATRunner;
import componenttest.topology.utils.HttpUtils;

/**
 *
 */
//@Mode(FULL)
@RunWith(FATRunner.class)
public class DatasourceTests20 extends AbstractSpringTests {

    @Override
    public Set<String> getFeatures() {
        return new HashSet<>(Arrays.asList("springBoot-2.0", "javaee-8.0"));
    }

    @Override
    public void modifyServerConfiguration(ServerConfiguration serverConfig) {
        configureDerbyDataAccess(serverConfig);
        configureKeystore(serverConfig);
    }

    void configureDerbyDataAccess(ServerConfiguration serverConfig) {
        // <jdbcDriver libraryRef="DerbyLib" id="DerbyEmbedded"/>
        // <library filesetRef="DerbyFileset" id="DerbyLib"/>
        // <fileset dir="${shared.resource.dir}/derby" includes="derby.jar" id="DerbyFileset"/>
        // <dataSource jdbcDriverRef="DerbyEmbedded" id="DefaultDataSource">
        //     <properties.derby.embedded createDatabase="create" databaseName="memory:ds1"/>
        // </dataSource>
        Library driverLibrary = configureDerbyLibrary(serverConfig);
        configureDerbyDataSource(serverConfig, driverLibrary);

    }

    Library configureDerbyLibrary(ServerConfiguration serverConfig) {
        Library library = null;
        try {
            ConfigElementList<Fileset> configuredFileSets = serverConfig.getFilesets();
            configuredFileSets.clear();
            Fileset fileset = serverConfig.getFilesets().getOrCreateById("DerbyFileset", Fileset.class);
            fileset.setDir("${shared.resource.dir}/derby");
            fileset.setIncludes("derby.jar");

            ConfigElementList<Library> configuredLibraries = serverConfig.getLibraries();
            configuredLibraries.clear();
            library = configuredLibraries.getOrCreateById("DerbyLib", Library.class);
            library.setFileset(fileset);
        } catch (Exception e) {
            System.out.println("An exception occurred creating the Derby library");
            e.printStackTrace(System.out);
        }
        return library;

    }

    DataSource configureDerbyDataSource(ServerConfiguration serverConfig, Library driverLibrary) {
        DataSource dataSource = null;
        try {
            List<DataSource> configuredDataSources = serverConfig.getDataSources();
            configuredDataSources.clear();
            dataSource = serverConfig.getDataSources().getOrCreateById("DefaultDataSource", DataSource.class);
            //dataSource.setJndiName("jdbc/ds1");

            JdbcDriver jdbcDriver = configureDerbyJdbcDriver(serverConfig, driverLibrary);
            dataSource.setJdbcDriverRef(jdbcDriver.getId());

            Properties_derby_embedded dataSourceProperties = new Properties_derby_embedded();
            dataSourceProperties.setDatabaseName("memory:ds1");
            dataSourceProperties.setCreateDatabase("create");
            //dataSourceProperties.setUser("user1");
            //dataSourceProperties.setPassword("password");
            dataSource.getProperties_derby_embedded().add(dataSourceProperties);
        } catch (Exception e) {
            System.out.println("An exception occurred creating the Derby DataSource");
            e.printStackTrace(System.out);
        }
        return dataSource;
    }

    JdbcDriver configureDerbyJdbcDriver(ServerConfiguration serverConfig, Library driverLibrary) {
        JdbcDriver jdbcDriver = null;
        try {
            ConfigElementList<JdbcDriver> configuredJdbcDrivers = serverConfig.getJdbcDrivers();
            configuredJdbcDrivers.clear();
            jdbcDriver = configuredJdbcDrivers.getOrCreateById("DerbyEmbedded", JdbcDriver.class);
            jdbcDriver.setLibrary(driverLibrary);
        } catch (Exception e) {
            System.out.println("An exception occurred creating the Derby JdbcDriver");
            e.printStackTrace(System.out);
        }
        return jdbcDriver;
    }

    void configureKeystore(ServerConfiguration serverConfig) {
        List<KeyStore> keystores = serverConfig.getKeyStores();
        keystores.clear();

        KeyStore keyStore = new KeyStore();
        keystores.add(keyStore);
        keyStore.setId("defaultKeyStore");
        keyStore.setPassword("yourPassword");
    }

    @Override
    public String getApplication() {
        return SPRING_BOOT_20_APP_BASE;
    }

    /**
     *
     */
    @Test
    public void testDefaultDataSource() throws Exception {
        HttpUtils.findStringInUrl(server, "testDefaultDatasource", "PASSED");
    }

}