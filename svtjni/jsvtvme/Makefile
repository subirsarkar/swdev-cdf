.SUFFIXES: .java .class
JFLAGS = -g -deprecation -sourcepath ${SVTVME_DIR}/Native

SRCS = $(wildcard *.java)

CLASSFILES   = $(SRCS:.java=.class)

all: classes header

classes: $(CLASSFILES)

# Default rule for compiling Java source
.java.class: %.class
	javac $(JFLAGS) $<

header: 
	javah -jni -classpath ${SVTVME_DIR}/Native -d ../include jsvtvme.SvtvmeImpl

doc:
	javadoc -author -private -version -d ${SVTVME_DIR}/Native/doc $(SRCS)

# Ignore errors, continue compilation

.IGNORE :

# Clean

.PHONY   : clean
clean :
	-rm *.class
