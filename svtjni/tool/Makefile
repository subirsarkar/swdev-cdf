all : clean GetConstants create_java

GetConstants : GetConstants.c
	${SVTVME_DIR}/svtvmecomp_dbg GetConstants
	@echo executable GetConstants created ....

create_java : GetConstants
	./GetConstants 
	cp SvtvmeConstants.java ../jsvtvme
	@echo ../jsvtvme/SvtvmeConstants.java created ....

.PHONY   : clean
clean :
	-rm GetConstants
	-rm SvtvmeConstants.java

