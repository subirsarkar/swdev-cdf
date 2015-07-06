.SUFFIXES: .class .java

JAVAFILES = Spy.java                  SpyHistoryPane.java    \
            SpyMasterPane.java        SpySlaveStatPane.java  \
            SpySlaveGenPane.java      SpySlaveErrPane.java   \

CLASSFILES = $(JAVAFILES:.java=.class)

all:  classes

classes: $(CLASSFILES) 

# Default rule for compiling java code
.java.class:
	javac -g -sourcepath ${SVTVME_DIR}/Native $<

doc:
	javadoc -author -private -version -d doc *.java

clean:
	-rm *.class
