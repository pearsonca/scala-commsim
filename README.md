##SCALA-COMMSIM - A Scala-Based Social Simulator
###Abstract
Network science approaches to social phenomena often reduce to comparing various centrality
measures: between groups, between times, aggregating on different interactions, *et cetera*.
The network, however, is only a representation.  Unless that network explains
(and preferrably, predicts) some phenomena, it has no meaning, and those centrality metrics
must recognized to be similarly meaningless.  Sans meaning, our work is mathematics at best,
and bookkeeping at worst.

We discuss two complementary perspectives, with the goal of exhorting reader to avoid network
accounting, one generally from physical sciences traditions and one specifically from
Partially Observed Markov Process modeling.  We discuss how lessons from software engineering
culture can provide a practical means to implement these perspectives, and close with a
demonstration.

###Introduction

###Methods
We demonstate using the framework to create individual-based models, incrementally composed
from different phenomena.

####Family Demographics
Given basic population information -- e.g., age of reproduction, typical number of children,
life expectancy -- we can simulate models of family life, undertake careful inference
work to fit our model parameters, and then select a particular model using model comparison
techniques.  If our models of family life are phenomenological -- we describe some
process by which families are formed, grow, and die, rather than a simply formula with
coefficients to fit -- then we are assigning meaning.  Perhaps not well, and certainly only
as well as our data allow, but our results draw phenomenological conclusions, not just
mathematic ones.

If those models are individual-based, then we may capture interactions and subject them to
network-based analyses.

####Economic Interactions

##LICENSE

Create Commons, Non-Commercial, Share-alike

http://creativecommons.org/licenses/by-nc-sa/3.0/

Attribute source to 

Carl A. B. Pearson, github/pearsonca
