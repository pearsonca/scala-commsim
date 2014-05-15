---
layout: paper
title: Challenges to Understanding Covert Groups
authors:
 - name: Carl A. B. Pearson
   email: cap10@ufl.edu
   corresponding: 1
 - name: Burton H. Singer
   email: bhsinger@ufl.edu
 - name: Edo Airoldi
 - name: Ed Kao
---
##Abstract

Implicit in the label *dark network* is a suggestion that the activity of covert
groups is driven by - and therefore, may be understood in terms of - network
measures like degree distributions and centralities.  That may be true.

Network representations, however, are a remarkably simple description, and
simple models of all but the most trivial phenomena must make very strong
assumptions.  Alas, we have little basis to assume anything: we accurately gauge
our knowledge by calling these groups *dark*.

Modelers in the natural sciences have recently tackled similar problems
by modeling both the system and observation process, with subsequent
formal relaxation of simplifying assumptions (if model parameters are
identifiable) and directed data collection (if they are not).  They still
enjoy many of the same simplifying advantages as the physical sciences, and thus
their models may be largely numerical.

We argue that social science phenomena require a more-than-numerical formal
expression, and extend previous arguments for agent-based modeling to embrace
the lessons of software engineering.  In this form, a model with observation
explicitly incorporated is a suitable platform for evaluating network science
results.  We close with a practical demonstration.

##Introduction

Network science approaches to social phenomena often reduce to comparing degree
structures and centrality measures: between groups, between times, aggregating
on different interactions, *et cetera*.  A network, however, is a very simple
model of a group, and simple models (of all but the most trivial phenomena) are
only obtained by making strong assumptions.  One flavor of strong assumption is
treating different relations as a single kind of tie, either via data massage
(e.g., co-enrollment in an introductory course being equal to co-enrollment in a
small seminar course) or as a consequence of the data source (co-arrest of two
people assaulting a third versus co-arrest for assaulting each other).
Indeed, static ties are themselves a quite strong assumption.

Another very strong, but problematically often unacknowledged, assumption is
that the individuals are of the same kind.  Extreme differences in contact
distributions for flight attendants versus theoretical mathematicians become
artificially interesting when we treat those people as the same kind of thing.
Bipartite networks can be used to address this problem, but they are just as
vulnerable.

The physical sciences reliably enjoy this (typically unacknowledged) simplifying
assumption.  As far and wide as we have looked, one hydrogen atom is the same as
another (give or take a few nucleons, but in a very simply described way).  Prominent
publications on networks by scientists from that background often adopt this
assumption implicitly.  Sometimes, this assumption is generally reasonable (e.g.,
Ising models of phase transitions), and sometimes it's acceptable given the
specific question (e.g., the growth of the world wide web or citation networks).
Sometimes [posterior analyses][barabasi1999] find this assumption is not
obviously precluded (though they may argue a stronger conclusion), but that's
scant indication the assumption is reasonable.

This homogeneity in kinds, however, seems suspect for many questions in social
science.  Several of the other chapters in these proceedings highlight such
questions and discuss ways to reduce these assumptions in network models.  We
approach the problem from the complementary direction, by treating the network
as the observation rather than the phenomena.  More on that in a moment.

Another subtle advantage of homogeneity in kinds is the ability to smooth over
observation difficulties.  Can't find the Higgs boson?  Get another 300 trillion
[samples][atlas2012].  We should hope we cannot get 300 independent samples of
terrorist cells, let alone a trillion times that, but the few samples we do have
are further fraught with observation issues.

For certain flavors of covert groups - e.g., consumers and distributors of
illegal substances, persons with persecuted sexual preferences - snowball
sampling can make [reasonable in-roads][biernacki1981snowball].  However, the
techniques required for successful snowball sampling are conspicuously untenable
for certain classes of covert groups, such as violent terrorist organizations or
state espionage apparati.  In some cases, these groups may even be structured in
a way - high compartmentalization, with multiple layers of indirection - such
that fundamental assumptions of snowball sampling about the relationship
structure are violated.

These problems - heterogeneity and stringent observation limits - are shared
by the natural sciences.  Recent work has begun to tackle these problems in
network contexts, e.g. [HIV transmission][volz2013inferring], but there is also
some history of broader adoption of this perspective via concepts like
[partially observed Markov processes][Ionides05122006].

This work may still enjoy more homogeneity in the atomic pieces of the model
than the social sciences, and many of the pertinent observations are the same
sort of properties that concern the physical sciences - temperatures, chemical
concentrations, sizes, masses, *et cetera*.  Hence, the largely equation-based
formal representation remains quite powerful.

We contend that the notion of adopting the equation-based formalism for social
science problems as the go-to for modeling is fundamentally flawed.  Yes,
there are plenty of important numerical quantities.  Yes, quantitative statistical
analysis of the models is still important.

But we should accept that many social science models are most naturally expressed
with the tools of mathematical logic, which are today most practically
implemented in general purpose programming languages.  And while the physical
and natural sciences have a successful history with numerical models, and
certainly a tradition of model selection worth consideration, the best source of
practical model insight looks more like the videogame industry than physicists
working on statistical mechanics.

Prominent articles in the social sciences have previously argue this position,
though perhaps less specifically, by observing that agent-based
modeling is the more natural and powerful means to explore representations of
social phenomena.  Yet, some still insist on using networks as a fundamental
element of [those models][Snijders201044].  We propose a different lens - the
transient *events* are what should be modeled, not the ties.  The events are
then filtered through a model observation process which translates those events
into a network; this is where we have the opportunity to explicitly state how we
are aggregating.  We may then meaningful compare predictions based on network
measurements to model outcomes via relevant tests.

We will begin by reflecting on modeling philosophy, then discuss some practical
outcomes of that philosophy when realized in general purpose programming, and
close with an implementation that reflects those values in a simulation of
covert groups.

##Principles of Modeling with Code

Scientific results are not measured by some idealized ability to explain and
predict, but by their use them to those ends.  Regardless of their power,
if poorly communicated, they will remain idle.  Which is to say, great stories
and great storytelling are not one and the same.  Our common language
description of scientific ideas contributes to their effective communication,
certainly, but the core expression of those models must be in precise syntax,
and choices there matter as well.

Equations are a typical way to accomplish such precision.  Those familiar with
numerical equation based models will recognize some of the features of effective
expression: conventional and consistent notation to leverage the audiences' pre-
existing knowledge, equation forms that relate parameters that have intuitive
meanings in obvious ways, and finally variable name choices that connect the
model to the phenomena.

These traits have analogies in code, though the first two were difficult to put into
practice until recently.  Historically, most general purpose languages reflected
what actually occurred in memory with the syntax improvements limited to
numerical abstractions and flow control.  The development and promulgation of object-oriented concepts
shifted how we reasoned about programs, leading to languages and libraries
designed for those concepts.  Modern programming languages bring even-more-natural
language syntax while maintaining the precision necessary to direct a computer,
allowing us to represent complex models with [literate code][knuth1984literate].
We will demonstrate this shortly using Scala.

First, there are some other practical considerations to modeling in code, also
analogous to features of modeling in equations.

- equations relatively easy to check
- often clear where exactly assumptions are being made (e.g., explicitly dropping
particular order)
- equations are there for re-use, re-mixing

should expect the same for any code model - pseudo-code, preferably the
actual code itself (depending on how critical performance is - high performance
languages are not known for code clarity).  Non-trivial equation
models typically consistent of several equations of different kinds, each
representing some aspect of the system.  That set forms the basis for some particular
study, yet those equations are independently re-usable.

engineering numerical models often involve many different phenomena (eg a flow
model, a heat exchange model, a power generation model).  those
phenomena can be separately modeled and tested (for code quality) and validated
(for parameter fitting), then assembled iteratively (flow + heat exchange) with
corrections made at each step.

this is consistent with good modeling habits - start small, then relax assumptions
(aka combine models).

it is possible to implement these by implementing many small models first, then
copy pasting that code into a bigger models, and so on.  copy-pasting and making
things play nice takes a lot of effort.  using object oriented concepts, we can
avoid copy-pasta.  there are several other advantages: partitioning work allows
domain experts to develop models in parallel, those partitioned components can
be the subject of rigorous testing (itself a way to improve the formal
expression of a model beyond the syntactic constraints of a language) and
independent pre-parameterization, and of course reuse.

> want to code in a way the minimizes rework - not just for practical reasons,
but so that we may be confident of the foundation.  Reuse means we gain all the
confidence in the past work that has accrued, but also in revisiting the old
code we may fix bugs, etc.  if we've been working in the open, repeatable
science mode, then we should be able to easily revisit past work that may have
been effected by the bug.

> how do we encourage reuse?  by building pieces in a modular way.  this has a
number of advantages - easier to test, easier to get domain experts to reason
about their piece, easier to upgrade pieces (both computationally and
representationally). The principle disadvantage appears when we want those
pieces to talk to each other.

> modular pieces - design patterns book suggests many ways to achieve these ends.
We suggest one based strongly on those concepts (mixing chain of responsibility,
delegation, and state) and another based on those concepts, but relying on the
scala syntax and multiple inheritance resolution.  We prefer the latter, since
it achieves many of the desirable aspects of reuse without requiring as much
tinkering to get up and running.  It does not preclude some problems (e.g.,
namespace collisions), but the sort of modeling care needed to avoid those is
far lower than the sort of care to properly apply the design patterns.

> Test Driven Design [TDD][janzen2005test] = looks exactly like Koopman's modeling
loop

There are several approaches to achieve this.

inheritance is one option.  However, traditional linear
inheritance is problematic for reusability if we want to have different kinds.
If we very carefully implement state + delegation, and chain of responsibility,
then linear inheritance can work.  Careful implementation of those is hard.

That also looks exactly like composition, but composition is often syntactically
hard (lots of boilerplate code to do delegation).  Indeed, both of those options
feature lots of boilerplate code, and boilerplate to maintain flexibility
obscures our ability to read the code directly as the model.

Scala to the rescue: multiple inheritence via traits which can contain
implementations eliminates most of the boilerplate.  It is even reasonable to
imagine distributing these traits as modeling packages, and then a particular
model could import the trait package, declare agents using it and other traits,
then define their particular behavior in terms of those trait properties / methods.

Also non-trivial advantage of scala: has an actor model baked in the standard
language library, which handles the boilerplate of distributed computing for the
modeler.

Scala not necessarily for everyone.  key point to keep in mind: build actors
by composition (allows rigorous independent testing, development, comprehension
of bite size bits rather than the whole pie, reuse, etc).  Other languages can
do composition, though authors not aware of any that do so as easily while still
providing benefit of compiled, strongly typed language.

##Demonstration Model
two basic social traits - family relationships, religious affiliation
affiliation

third trait for covert group membership - all agents have it, only active in
some (recruits)

all use a generic base agent trait to cover tracking through time:

{% highlight scala %}
import scala.concurrent.Future.successful
import akka.actors.TypedActor

trait TimeResponse {
  def tick(implicit when:Int) = successful(Ack)
}
{% endhighlight %}

###`ReligiousAffiliation`

{% highlight scala %}
trait ReligiousAffiliation extends TimeResponse {

}
{% endhighlight %}


- institution
- attendence generator on tick
- probability of attendence

note assumptions, like weekly service model, uniform probability of attendance.
options for higher attendance on holidays, longer religious observations, etc

###`FamilyRelations`

{% highlight scala %}
trait FamilyRelations extends TimeResponse {

}
{% endhighlight %}

- parents
- partner
- children
- contact generator per tick

note how assumptions can be highlighted: monogamous, permanent partnerships
no gender in model

###`CovertAffiliation`

- known other affiliates
- activation state

###Family Generation

show opportunity for demographic fitting options

###Religious Affiliation Generation

show opportunity for more demographics options

###Covert Generation

recruitment dynamics based on arbitrary acquaintance generation

###Composed Models

show family altering religious affiliation generation (recruit children into
same org.).  Show religious affiliation modifying family generation
(partnership formation).  show covert recruitment based on familial / religious
ties

##Afterthoughts

[janzen2005test]: <http://digitalcommons.calpoly.edu/cgi/viewcontent.cgi?article=1034&context=csse_fac> "optional title"
[barabasi1999]: <http://dx.doi.org/10.1126/science.286.5439.509> "optional title"
[atlas2012]: <http://www.sciencedirect.com/science/article/pii/S0370269312001852> "optional title"
[biernacki1981snowball]: <http://smr.sagepub.com/content/10/2/141.full.pdf> "optional title"
[knuth1984literate]: <http://dx.doi.org/10.1093/comjnl/27.2.97> "optional title"
[Ionides05122006]: <http://dx.doi.org/10.1073/pnas.0603181103> "optional title"
[volz2013inferring]: <http://dx.doi.org/10.1371/journal.pcbi.1003397> "optional title"
[Snijders201044]: <http://dx.doi.org/10.1016/j.socnet.2009.02.004> "optional title"
