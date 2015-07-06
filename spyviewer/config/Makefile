.SUFFIXES: .class .java

JAVAFILES = RCStateConstants.java     SpyMessenger.java         \
            SpyErrorFrame.java        \
            SvtCratesFrame.java       UserCallout.java          \
            MessageComposer.java      SpySchemePanel.java       \
            DecodeWord.java           SpyDataMessage.java       \
            SpyDumpFrame.java         SvtCrateMap.java          \
            SubscriptionDialog.java   SpyBuffer.java            \
            ReceiverDisplayPanel.java SpyDumpPanel.java         \
            SvtErrorDataMessage.java  SvtButton.java            \
            SvtCrateConfig.java       BeamPosition.java         \
            RCState.java              SimpleSessionManager.java \
            SessionManager.java       SvtBoardErrorFrame.java   \
            GlobalErrorSummaryPanel.java \
            BoardErrorRegPanel.java   HFBoardErrorRegPanel.java \
            CDFErrorPanel.java        TModePanel.java           \
            SendCommandFrame.java     SvtDataMining.java        \
            SvtDataManager.java       CrateInfo.java            \
            BoardInfo.java            ConfigDataFrame.java      \
            SvtConfigurator.java      SpyErrorViewer.java       \
            SvtBoardErrorViewer.java  SvtStatusViewer.java

CLASSFILES = $(JAVAFILES:.java=.class)
FLAGS      = -g -deprecation -Xlint:unchecked


all: util_files svt_files hist_files db_files beam_files classes

util_files:	
	cd util; gmake

svt_files:	
	cd svt; gmake 

hist_files:	
	cd hist; gmake

db_files:	
	cd db; gmake

beam_files:	
	cd beam; gmake

classes: $(CLASSFILES) 

# Default rule for compiling java code
.java.class:
	javac $(FLAGS) -sourcepath ${SVTMON_DIR} $<

doc:
	cd util; gmake doc
	cd hist; gmake doc
	cd svt; gmake doc
	cd db; gmake doc
	cd beam; gmake doc
	javadoc -author -private -version -d ${SVTMON_DIR}/doc -sourcepath ${SVTMON_DIR} $(JAVAFILES)

clean:
	cd hist; gmake clean
	cd util; gmake clean
	cd svt; gmake clean
	cd db; gmake clean
	cd beam; gmake clean
	-rm *.class
