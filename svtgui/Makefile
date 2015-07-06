# Construct executable name
UNAME = $(shell uname)
PNAME = svtgui

# Make magic
vpath %.c src

# Basics
SRCS       = $(wildcard src/*.c)
CC         = gcc
CCFLAGS    = -g -Wall 
LD         = gcc
LDFLAGS    = -g 
MAKEDEPEND = /usr/bin/X11/makedepend

# GTK+/GNOME resources
GNOME_CONFIG = /usr/bin/gnome-config
GNOME_CFLAGS = $(shell $(GNOME_CONFIG) --cflags gnomeui)
GNOME_LIBS   = $(shell $(GNOME_CONFIG) --libs gnomeui)

# Update compilation flag
CCFLAGS  += -I./include -I. -I${SVTVME_DIR}/include $(GNOME_CFLAGS) \
            -I/cdf/code-common/cdfonline/svttest/python/Python-1.5.2/Include

# Application specific libraries
SVTVMELIB = ${SVTVME_LD_PATH}/svtvme_wrap.o ${SVTVME_LD_PATH}/svtvme_th.o 
FISIONLIB = ${FISION_DIR}/${fision_ld_path}/${fision_mach_arch}/libVISIONclient.a
LIBS      = $(FISIONLIB) $(GNOME_LIBS) \
            -L/cdf/code-common/cdfonline/svttest/python/Python-1.5.2 -lpython1.5

# Create the executable

bin/$(PNAME): $(patsubst %.c,%.o,$(SRCS)) $(SVTVMELIB)
	@$(LD)  $(LDFLAGS) $^ -o $@ $(LIBS) -lm -ldl
	@echo Binary $@ created ....

# Create object files

%.o : %.c
	@$(CC) $(CCFLAGS) -c $< -o $@

# makedepend

depend: $(SRCS:.c=.c.dep)
	@cat $(notdir $^) > Makefile.dep
	@-rm -f $(notdir $^) $(patsubst %,%.bak,$(notdir $^))
	@perl -pi -e 's#^(\w+)(.o)#src/\1\2#g' Makefile.dep
%.dep:
	@touch $(notdir $@)
	@$(MAKEDEPEND) -f$(notdir $@) -- $(CCFLAGS) -- $*
include Makefile.dep

# Ignore errors, continue compilation

.IGNORE :

# Clean

.PHONY   : clean
clean :
	@-rm $(patsubst %.c,%.o,$(SRCS)) 
	@-rm bin/$(PNAME)
