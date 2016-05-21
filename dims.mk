# using covert pops from input/digest/covert/%scenario/#.csv...

EMPTY :=
SPACE := $(EMPTY) $(EMPTY)

USAGE := low mid high
PWRS := lo med hi
TIMS := early middle late
SZS := 5 10 20

COVERTDIMS :=

$(foreach u,$(USAGE),\
 $(foreach p,$(PWRS),\
  $(foreach t,$(TIMS),\
   $(foreach s,$(SZS),\
$(eval COVERTDIMS += $(u)/$(p)/$(t)/$(s))\
))))

seq2 = $(strip $(shell for i in $$(seq $(1) $(2)); do printf '%03d ' $$i; done))

SAMPN := 60
SAMPS := $(call seq2,1,$(SAMPN))