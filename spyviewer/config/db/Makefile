.SUFFIXES: .class .java

JAVAFILES = SvtDb.java SvtDbBrowser.java BillCRC.java

CLASSFILES = $(JAVAFILES:.java=.class)
FLAGS = -g -deprecation -Xlint:unchecked

all:  classes

classes: $(CLASSFILES) 

# Default rule for compiling java code
.java.class:
	javac $(FLAGS) -sourcepath ${SVTMON_DIR} $<

doc:  
	javadoc -author -private -version -d ${SVTMON_DIR}/doc -sourcepath ${SVTMON_DIR} $(JAVAFILES)

clean:
	-rm *.class
