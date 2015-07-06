UNAME  = $(shell uname)
NAME   = RootHist
SHLIB  = lib$(NAME)

# No blanks space
CSUF   = cxx
SHL    = so
EXE    = exe
OSUF   = o
HSUF   = h

VPATH = src:./include
vpath %.h ./include

SRCS   = $(wildcard ./src/*.$(CSUF))
OBJS   = $(patsubst %.$(CSUF), %.$(OSUF), $(SRCS))

LIBL   =  LinkDef.h
LIBH   =  $(wildcard ./include/*.$(HSUF))
LIBDS  =  $(NAME)Cint.$(CSUF)

ROOTCFLAGS = $(shell ${ROOTSYS}/bin/root-config --cflags)
ROOTLIBS   = $(shell ${ROOTSYS}/bin/root-config --libs)
ROOTGLIBS  = $(shell ${ROOTSYS}/bin/root-config --glibs)

OCFLAGS = -I./include -I/usr/include/g++-2/

ifeq ($(UNAME),Linux)

  CXX      =  egcs
  LD       =  egcs
  LDFLAGS  =  -g
  CXXFLAGS =  -O -Wall -fPIC $(ROOTCFLAGS) $(OCFLAGS) -fno-rtti -fno-exceptions
  SOFLAGS  =  -shared -Wl,-soname,
  MAKEDEPEND  = $(shell which makedepend)

else

  CXX      =  KCC
  LD       =  KCC
  CXXOPT   = +K0 --thread_safe
  OPT      =  -g -O0
  EXCEPTION = --exceptions
  ABI       = 
  LDFLAGS  =  
  CXXFLAGS =  $(ABI) $(EXCEPTION) --signed_chars  --display_error_number \
               --diag_suppress 68 --diag_suppress 191 --diag_suppress 830 \
               --diag_suppress 837 --diag_suppress 426 --diag_suppress 167 \
               $(CXXOPT) $(ROOTCFLAGS) $(OCFLAGS)
  SOFLAGS  =  $(OPT) $(CXXOPT) $(ABI) $(EXCEPTION)
  MAKEDEPEND  = /cdf/people2/sarkar/bin/makedepend_IRIX

endif

# Create executable 
$(NAME)_$(UNAME).$(EXE): $(OBJS)
	$(LD)  $(LDFLAGS) $^ -o $@ $(ROOTGLIBS)
	@echo "Executable $(EXE) created"

# Create shared library
$(SHLIB)_$(UNAME).$(SHL): $(OBJS)
	$(LD)  $(SOFLAGS) $(LDFLAGS) $^ -o $@ $(ROOTGLIBS)
	@echo "Shared library done"


$(LIBDS): $(LIBL) $(LIBH)
	@echo Generating dictionary $(LIBDS)
	rootcint -f $(LIBDS) -c $(LIBH) $(LIBL)

# Create object files
%.o : %.$(CSUF)
	$(CXX) $(CXXFLAGS) -c $< -o $@

# makedepend
depend: $(SRCS:.$(CSUF)=.$(CSUF).dep)
	cat $(notdir $^) > Makefile.dep
	-rm -f $(notdir $^) $(patsubst %,%.bak,$(notdir $^))
%.dep:
	touch $(notdir $@)
	$(MAKEDEPEND) -f$(notdir $@) -- $(CXXFLAGS) -- $*
include Makefile.dep

# Ignore errors, continue compilation
.IGNORE :

# Clean
.PHONY   : clean
clean :
	-rm $(OBJS) 
	-rm $(SHLIB)_$(UNAME).$(EXE)
