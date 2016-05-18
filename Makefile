# using covert pops from input/digest/covert/%scenario/#.csv...

include references.mk
include $(REFDIR)/references.mk

INPUTSRC   := $(INDIR)/$(WORKINGDIR)/covert
OUTSRC     := $(INDIR)/simulate/covert
FILTERDATA := $(INDIR)/$(WORKINGDIR)/filter
DIGESTDATA := $(INDIR)/$(WORKINGDIR)/clustering

default:
	@echo helloworld

START  := target/start
DIGPATH := ../montreal-reprocess
STARTDIG  := $(DIGPATH)/$(START)

EMPTY :=
SPACE := $(EMPTY) $(EMPTY)

R := /usr/bin/env Rscript

$(START): $(shell find ./src -type f)
	sbt start-script

$(STARTDIG): $(shell find $(DIGPATH)/src -type f)
	@cd $(DIGPATH); sbt start-script

USAGE := low mid high
PWRS := lo med hi
TIMS := early middle late
SZS := 5 10 20

$(INPUTSRC): | $(INPUT)
	mkdir -p $@

define factorial1dir
$(INPUTSRC)/$(1): | $(INPUTSRC)
	mkdir $$@
endef

define factorial2dir
$(INPUTSRC)/$(1): | $(INPUTSRC)/$(dir $(1))
	mkdir $$@
endef

$(foreach u,$(USAGE),$(eval $(call factorial1dir,$(u))))
$(foreach u,$(USAGE),\
 $(foreach p,$(PWRS),\
  $(eval $(call factorial2dir,$(u)/$(p)))\
))
$(foreach u,$(USAGE),\
 $(foreach p,$(PWRS),\
  $(foreach t,$(TIMS),\
   $(eval $(call factorial2dir,$(u)/$(p)/$(t)))\
)))

COVERTDIMS :=

$(foreach u,$(USAGE),\
 $(foreach p,$(PWRS),\
  $(foreach t,$(TIMS),\
   $(foreach s,$(SZS),\
$(eval COVERTDIMS += $(u)/$(p)/$(t)/$(s))\
))))

#$(info $(words $(COVERTDIMS)) $(COVERTDIMS))

SAMPN := 60

ALLPBS :=

define dirtars
$(call factorial2dir,$(1))

$(OUTSRC)/$(1)/%/: | $(OUTSRC)/$(1)
	mkdir -p $$@

$(OUTSRC)/$(1)/%/out.csv: $(START) $(INPUTSRC)/$(1)/%.csv | $(OUTSRC)/$(1)/%/
	./$$^ 10 7 4 > $$@

$(OUTSRC)/$(1)/%/trans.csv: translate.R $(INDIR)/remap-location-ids.rds $(OUTSRC)/$(1)/%/out.csv
	$(RPATH) $$^ 20649600 > $$@

$(OUTSRC)/$(1)/%/cc.csv $(OUTSRC)/$(1)/%/cu.csv: $(STARTDIG) $(OUTSRC)/$(1)/%/trans.csv
	./$$^

$(INPUTSRC)/$(1)/%.csv: mkusergroup.R\
 $(DIGESTDATA)/userrefs.rds $(FILTERDATA)/detail_input.rds $(DIGESTDATA)/locrefs.rds\
 $(FILTERDATA)/location_pdf.csv $(DIGESTDATA)/uprefs.rds | $(INPUTSRC)/$(1)
	$(R) $$^ $(subst /,$(SPACE),$(1)) $$* > $$@

ALLPBS += samples-$(subst /,-,$(1)).pbs

ALLTRANSPBS += translated-$(subst /,-,$(1)).pbs

samples-$(subst /,-,$(1)).pbs: sample_pbs.sh
	./$$^ $(subst /,-,$(1)) $(1) $(SAMPN) > $$@

translated-$(subst /,-,$(1)).pbs: translate_pbs.sh
	./$$^ $(subst /,-,$(1)) $(1) $(SAMPN) > $$@

endef

$(foreach d,$(COVERTDIMS),\
 $(eval $(call dirtars,$(d)))\
)

allsamples: $(ALLPBS)

alltrans: $(ALLTRANSPBS)
