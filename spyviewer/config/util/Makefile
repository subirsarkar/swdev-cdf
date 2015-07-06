.SUFFIXES: .java .class
JAVAFILES = GriddedPanel.java         HexPanel.java            \
            JBinaryField.java         JFloatField.java         \
            JHexField.java            JNumberField.java        \
            AppConstants.java         Tools.java               \
            Converter.java            MemoryTest.java          \
            History.java              SimpleEditor.java        \
            Browser.java              SmallButton.java         \
            SmallToggleButton.java    MenuHelpTextAdapter.java \
            DigitalClock.java         TimeEvent.java           \
            ToStringGenerator.java    StatusButton.java        \
            DataFrame.java            AboutDialog.java         \
            TextPanel.java            SvtCommand.java          \
            StatusBar.java            Debug.java               \
            MyTreeModelListener.java  PrintfFormat.java        \
            TimeEventFrame.java       WordPanel.java           \
            ComboPanel.java           HtmlPanel.java           \
            AbstractMessageThread.java AbstractTextPanel.java  \
            SimpleTextPanel.java     FontChooser.java          \
            ErrorLogPanel.java       Gunzip.java               \
            ArchiveExtractor.java    ZipExtractor.java         \
            JarExtractor.java        TarExtractor.java         \
            FetchURL.java            MyCellRenderer.java       \
            RemoteFileDialog.java    FileTypeFilter.java       \
            MutableList.java         PopupListener.java        \
            ConsolePanel.java        OutputLoggerPanel.java    \
            ErrorLoggerPanel.java    Timex.java

CLASSFILES = $(JAVAFILES:.java=.class)
FLAGS      = -g -deprecation -Xlint:unchecked

# -d ${SVTMON_DIR}/src/config/util

all:  classes

classes: $(CLASSFILES) 

# Default rule for compiling java code
.java.class:
	javac $(FLAGS) -sourcepath ${SVTMON_DIR} $<

doc:	
	javadoc -author -private -version -d ${SVTMON_DIR}/doc -sourcepath ${SVTMON_DIR} $(JAVAFILES)

clean:
	-rm *.class
