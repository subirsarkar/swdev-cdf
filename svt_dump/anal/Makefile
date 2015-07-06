# Construct binary name
UNAME = $(shell uname)
PNAME = spy_dump

vpath %.c src

# Basics
SRCS       = $(wildcard src/*.c)
CC         = gcc
CCFLAGS    = -g -Wall 
LD         = gcc
LDFLAGS  = -g 
MAKEDEPEND = /usr/bin/X11/makedepend

# Update compilation flag
CCFLAGS  += -I./include -I. 

LIBS = -lm

# Create the binary

bin/$(PNAME)_$(UNAME): $(patsubst %.c,%.o,$(SRCS))
	-rm $@
	$(LD)  $(LDFLAGS) $^ -o $@ $(LIBS)
	@echo Binary $@ created ....

# Create object files

%.o : %.c
	$(CC) $(CCFLAGS) -c $< -o $@

# makedepend

depend: $(SRCS:.c=.c.dep)
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
	-rm $(patsubst %.c,%.o,$(SRCS)) 
	-rm bin/$(PNAME)_$(UNAME)

