.SUFFIXES: .class .java

JAVAFILES = HistogramUpdateable.java          Histogram.java \
            Histogram1D.java                Histogram2D.java \
            HistogramColl.java        SvtHistogramColl.java \
            HistogramPlotPanel.java  HistogramListPanel.java \
            HistogramDisplayFrame.java  SVTHistoMessage.java \
            Histogram1DImpl.java        Histogram2DImpl.java \
            HistogramDrawingManager.java                     \
            Histogram2DDecl.java        HistogramViewer.java \
            HistoMessageCreator.java    GraphDataSource.java \
            XYPlotPanel.java

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
