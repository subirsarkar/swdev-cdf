UNAME = $(shell uname)
SHLIB = libSvtsimImpl

vpath %.c .

CSUF  = c
SHSUF = so
LIB   = ${SVTVME_LD_PATH}/$(SHLIB).$(SHSUF)
TOOLO = ${SVTVME_DIR}/Native/src/jnitools.o

SRCS = $(wildcard *.$(CSUF))

ifeq ($(UNAME),Linux)
  MAKEDEPEND  = /usr/bin/X11/makedepend
  JAVACFLAGS  = -I${JAVA_DIR}/include -I${JAVA_DIR}/include/linux
  CC          = gcc
  CCFLAGS     = -g  -Wall -ansi -fPIC
  LD          = gcc
else
  CC          = cc
  CCFLAGS     = -g  -KPIC
  LD          = ld
  MAKEDEPEND  = /cdf/people1/cdf_svt/bin/makedepend_IRIX
  JAVACFLAGS  = -I${JAVA_DIR}/include -I${JAVA_DIR}/include/irix
endif

OCFLAGS  = -I../include -I${SVTVME_DIR}/include/
LDFLAGS  = -g
SOFLAGS  = -shared
CCFLAGS += $(OCFLAGS)
CCFLAGS += $(JAVACFLAGS)
LIBS     = ${SVTVME_LD_PATH}/svtvme_th.o  $(TOOLO) \
           ${FISION_DIR}/${fision_ld_path}/${fision_mach_arch}/libVISIONclient.so

all: depend $(LIB)

# Create shared library
 
$(LIB): $(patsubst %.$(CSUF),%.o,$(SRCS))
	$(LD)  $(SOFLAGS) $(LDFLAGS) $^ -o $@ $(LIBS)
	@echo Shared library $@ done

# Create object files

%.o : %.$(CSUF)
	$(CC) $(CCFLAGS) -c $< -o $@

# makedepend

depend: $(SRCS:.$(CSUF)=.$(CSUF).dep)
	cat $(notdir $^) > Makefile.dep
	-rm -f $(notdir $^) $(patsubst %,%.bak,$(notdir $^))
	perl -pi -e 's#^(\w+)(.o)#src/\1\2#g' Makefile.dep
%.dep:
	touch $(notdir $@)
	$(MAKEDEPEND) -f$(notdir $@) -- $(CCFLAGS) -- $*
include Makefile.dep

# Ignore errors, continue compilation

.IGNORE :

# Clean

.PHONY   : clean
clean :
	-rm $(patsubst %.$(CSUF),%.o,$(SRCS)) 
	-rm $(LIB)
