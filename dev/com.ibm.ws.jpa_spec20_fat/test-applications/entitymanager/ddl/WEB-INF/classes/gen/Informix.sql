CREATE TABLE CEmployee (id INTEGER NOT NULL, firstName VARCHAR(255), lastName VARCHAR(255), vacationDays INTEGER, version NUMERIC(32,0), PRIMARY KEY (id)) LOCK MODE ROW;
CREATE TABLE EMDETACH_ENTAM2MLIST (JPA20EMDETACHENTITY_ID INTEGER, ENTAM2MLIST_ID INTEGER) LOCK MODE ROW;
CREATE TABLE EMDETACH_ENTAM2MLIST_CA (JPA20EMDETACHENTITY_ID INTEGER, ENTAM2MLIST_CA_ID INTEGER) LOCK MODE ROW;
CREATE TABLE EMDETACH_ENTAM2MLIST_CD (JPA20EMDETACHENTITY_ID INTEGER, ENTAM2MLIST_CD_ID INTEGER) LOCK MODE ROW;
CREATE TABLE EMDETACH_ENTAO2MLIST (JPA20EMDETACHENTITY_ID INTEGER, ENTAO2MLIST_ID INTEGER) LOCK MODE ROW;
CREATE TABLE EMDETACH_ENTAO2MLIST_CA (JPA20EMDETACHENTITY_ID INTEGER, ENTAO2MLIST_CA_ID INTEGER) LOCK MODE ROW;
CREATE TABLE EMDETACH_ENTAO2MLIST_CD (JPA20EMDETACHENTITY_ID INTEGER, ENTAO2MLIST_CD_ID INTEGER) LOCK MODE ROW;
CREATE TABLE JPA20EMDetachEntity (id INTEGER NOT NULL, strData VARCHAR(255), version NUMERIC(32,0), ENTAM2O_ID INTEGER, ENTAM2O_CA_ID INTEGER, ENTAM2O_CD_ID INTEGER, ENTAO2O_ID INTEGER, ENTAO2O_CA_ID INTEGER, ENTAO2O_CD_ID INTEGER, PRIMARY KEY (id)) LOCK MODE ROW;
CREATE TABLE JPA20EMEntityA (id INTEGER NOT NULL, strData VARCHAR(255), version NUMERIC(32,0), PRIMARY KEY (id)) LOCK MODE ROW;
CREATE TABLE JPA20EMEntityA_JPA20EMEntityB (ENTITYALIST_ID INTEGER, ENTITYBLIST_ID INTEGER) LOCK MODE ROW;
CREATE TABLE JPA20EMEntityB (id INTEGER NOT NULL, strData VARCHAR(255), version NUMERIC(32,0), PRIMARY KEY (id)) LOCK MODE ROW;
CREATE TABLE JPA20EMEntityC (id INTEGER NOT NULL, strData VARCHAR(255), version NUMERIC(32,0), ENTITYA_ID INTEGER, ENTITYALAZY_ID INTEGER, PRIMARY KEY (id)) LOCK MODE ROW;
CREATE INDEX I_MDTCLST_ELEMENT ON EMDETACH_ENTAM2MLIST (ENTAM2MLIST_ID);
CREATE INDEX I_MDTCLST_JPA20EMDETACHENTITY_ID ON EMDETACH_ENTAM2MLIST (JPA20EMDETACHENTITY_ID);
CREATE INDEX I_MDTCT_C_ELEMENT ON EMDETACH_ENTAM2MLIST_CA (ENTAM2MLIST_CA_ID);
CREATE INDEX I_MDTCT_C_JPA20EMDETACHENTITY_ID ON EMDETACH_ENTAM2MLIST_CA (JPA20EMDETACHENTITY_ID);
CREATE INDEX I_MDTC_CD_ELEMENT ON EMDETACH_ENTAM2MLIST_CD (ENTAM2MLIST_CD_ID);
CREATE INDEX I_MDTC_CD_JPA20EMDETACHENTITY_ID ON EMDETACH_ENTAM2MLIST_CD (JPA20EMDETACHENTITY_ID);
CREATE INDEX I_MDTCLST_ELEMENT1 ON EMDETACH_ENTAO2MLIST (ENTAO2MLIST_ID);
CREATE INDEX I_MDTCLST_JPA20EMDETACHENTITY_ID1 ON EMDETACH_ENTAO2MLIST (JPA20EMDETACHENTITY_ID);
CREATE INDEX I_MDTCT_C_ELEMENT1 ON EMDETACH_ENTAO2MLIST_CA (ENTAO2MLIST_CA_ID);
CREATE INDEX I_MDTCT_C_JPA20EMDETACHENTITY_ID1 ON EMDETACH_ENTAO2MLIST_CA (JPA20EMDETACHENTITY_ID);
CREATE INDEX I_MDTC_CD_ELEMENT1 ON EMDETACH_ENTAO2MLIST_CD (ENTAO2MLIST_CD_ID);
CREATE INDEX I_MDTC_CD_JPA20EMDETACHENTITY_ID1 ON EMDETACH_ENTAO2MLIST_CD (JPA20EMDETACHENTITY_ID);
CREATE INDEX I_JP20TTY_ENTAM2O ON JPA20EMDetachEntity (ENTAM2O_ID);
CREATE INDEX I_JP20TTY_ENTAM2O_CA ON JPA20EMDetachEntity (ENTAM2O_CA_ID);
CREATE INDEX I_JP20TTY_ENTAM2O_CD ON JPA20EMDetachEntity (ENTAM2O_CD_ID);
CREATE INDEX I_JP20TTY_ENTAO2O ON JPA20EMDetachEntity (ENTAO2O_ID);
CREATE INDEX I_JP20TTY_ENTAO2O_CA ON JPA20EMDetachEntity (ENTAO2O_CA_ID);
CREATE INDEX I_JP20TTY_ENTAO2O_CD ON JPA20EMDetachEntity (ENTAO2O_CD_ID);
CREATE INDEX I_JP20TYB_ELEMENT ON JPA20EMEntityA_JPA20EMEntityB (ENTITYBLIST_ID);
CREATE INDEX I_JP20TYB_ENTITYALIST_ID ON JPA20EMEntityA_JPA20EMEntityB (ENTITYALIST_ID);
CREATE INDEX I_JP20TYC_ENTITYA ON JPA20EMEntityC (ENTITYA_ID);
CREATE INDEX I_JP20TYC_ENTITYALAZY ON JPA20EMEntityC (ENTITYALAZY_ID);