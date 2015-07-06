.SUFFIXES: .class .java

JAVAFILES = SvtObject.java       Hit.java           \
            Road.java            Packet.java        \
            SvtTrack.java        XftTrack.java      \
            SvtBufferData.java   SvtBoardData.java  \
            SvtCrateData.java    SvtErrorData.java  \
            SvtEvent.java        EventFrac.java     \
            HitEvent.java        RoadEvent.java     \
            PacketEvent.java     TFEvent.java       \
            XTFAEvent.java                          \
            SvtEvents.java       SvtEventsBase.java \
            HitEvents.java       RoadEvents.java    \
            PacketEvents.java    TFEvents.java      \
            XTFAEvents.java      EEWord.java        \
            PlotPanel.java       InfoFrame.java     \
            SpyBufferViewer.java SvtHistogrammer.java \
            Interpreter.java     BufferInfo.java \
            SpyType.java         SplashWindow.java \
            SvtEventsAttr.java

CLASSFILES = $(JAVAFILES:.java=.class)

FLAGS      = -g -deprecation -Xlint:unchecked

all:  classes

classes: $(CLASSFILES) 

# Default rule for compiling java code
.java.class:
	javac $(FLAGS) -sourcepath ${SVTMON_DIR} $<

doc:  
	javadoc -author -private -version -d ${SVTMON_DIR}/doc -sourcepath ${SVTMON_DIR} $(JAVAFILES)

clean:
	-rm *.class
