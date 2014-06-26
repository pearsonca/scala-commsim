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
measures like degree distributions, paths, centralities, and so on.

That may be true.

Network representations, however, are a remarkably simple description, and
simple models of all but the most trivial phenomena must make very strong
assumptions.  We have little basis to assume anything in the case of covert
groups: the *dark* label is an accurate gauge of our knowledge.

Thus, to genuinely characterize the performance of these assorted network criteria, we
must acknowledge they are applied to shadows of the true phenomena.  And unlike
the more measurements typifying physical sciences, we do not yet have centuries
of argument to provide context and meaning to what exactly these networks mean.

Fortunately, we can leverage developments in natural sciences that explicitly
incorporate observation into the modeling process.  We demonstrate these, with
particular emphasis on agent-based models and a development approach the
encourages reuse, replication, and refutation.

This approach acknowledges the lessons of traditional equation-based models (the
primary approach of physical and natural sciences), while also embracing ideas
from software engineering that are a more appropriate perspective for this kind
of modeling.

* * *

##Introduction

Some network science approaches to social phenomena seem to amount to looking
for statistically significant differences on degree distributions, paths,
centrality measures, *et cetera* between scenarios.

A network, however, is a *very* simple model of a group, and simple models (of
all but the most trivial phenomena) are only obtained by making strong
assumptions.  Such assumptions can be reasonable, and when they are, a very
compact general description can usefully cover a broad array of phenomena.  This
is the case for traditional problems in the physical sciences, loosely speaking.

These assumptions seem less tenable in the natural sciences, and often border on
suspicious in the social sciences.  One particularly suspicious application is
to *dark networks* - the social network representation of covert groups, most
often criminal organizations.

Throughout this chapter, we will refer to dataset and
its interpretations to illustrate that skepticism, using shorthand like *the
Montreal data* or *the Montreal network*.  The work that introduces the data
focuses on a theoretical epidemiological question - whether or not a unique
network structure could explain a specific kind of epidemic dynamic.  The
relationship between the data and that network model, however, is ideal for
exploring many of the issues present in attempting to represent covert social
groups with so-called dark networks. We will refer back to this *Montreal Model*
as a practical touchstone for issues we highlight with typical approaches.

>###The Montreal Municipal WiFi Service Data & A Basic Model
>
>In a [forthcoming publication][montreal], epidemiological modelers use data on
access to the Montreal Municipal WiFi service to build a contact network and
then consider the spread of flu-like pathogens on that network.
>
>The anonymized raw data is straightforward to understand.  Users have log on
and log off times at WiFi hotspots associated with the service.  How the data
are translated into a network for the epidemiological
analysis is also simple: users that logged into the same location at the same time are joined
by an edge.  Those edges are then aggregated into a contact network, removing
duplicates and self loops.
>
>The data spans roughly five years, a few hundred thousand entries of login data
with X users and Y hotspot locations.

The strongest assumptions in most network analyses concern homogeneity and
observational accuracy.

Homogeneity assumptions take many forms, some obvious, some subtle.  We will
focus on the assumption of homogeneity in kinds, as that is perhaps the most
fundamental assumption in network models: that edges are edges are edges, and
likewise with vertices.  In some systems, this is quite reasonable, but when
the network is organized (by design or spontaneously) to accomplish some task,
the parts and their interactions are not typically interchangeable.

Observation assumptions come in levels of strength.  The strongest being of
course that what is observed is the whole truth and nothing but the truth.  This
is typically weakened to some censoring of events (*i.e.*, false negatives) and
some mislabeling of non-events (*i.e., false positives*), but with errors that
independent and identical distributed (usually according to a simple distribution, as well).
However, with covert groups, the (non-)observation process is censored in a
deliberate, but unknown, manner.

Critically, these assumptions are suspect when trying to use network analyses
to find covert groups, also know as the *dark network detection problem*.

##Homogeniety

*Homogeniety in kinds* is the modeling assumption that is
that pieces of some kind in the model are identical (and often, therefore,
interchangeable).  To be clear, this an assumption about the model entities and
inputs, not an assumption that, say, all vertices have identical degrees or
centralities.  Indeed, the assumption is critical in many analyses that we
expect to measure different values, like centralities or module membership.

> ### Homogeniety in the Montreal Network
>
> The Montreal source data homogenizes on several key aspects:
>
> - all user logins are treated as equal
> - all locations are treated as equal
>
> But we can imagine potentially meaningful censoring that occurs due to this
homogenization.  In the context of transmissible disease:
>
> - some of the *users* might simply be shared business connections (the data has
several high utilization users that are consistent with this sort of use)
> - some of the locations might have many ways for co-users to transmit disease,
while others might be relatively sterile
>
> Furthermore, the previous work using this data treats connections as homogeneous
in terms of their capability to serve as a transmission route.  But there is quite
a range of variability in the co-location times: some users overlap mere seconds,
while others overlap for hours.

Contact distributions are replete with this sort of assumption.  Extreme
differences in contact distributions for flight attendants versus theoretical mathematicians can become
artificially interesting when we treat those jobs, and the kind of interactions
they entail as the same kind of thing.  Certainly for some questions, this
homogenization might be reasonable.  But for many others, valuable insight
arises when our model preserves distinctions.  This lets us highlight [Erdos][grossman1995portion]
and differentiate flight attendants on private, regional, and hub-to-hub flights.

Bipartite networks may appear to address this problem, but are often simply adding another *kind*.
Having two kinds of interactions still homogenizes those
interactions -- *e.g.*, a white collar worker visiting a coffeeshop en route
to work in morning is a very different sort of interaction than even that same
white collar worker visiting an alley in the dead of night.

Network module
analyses are often attempts to distinguish kinds, as are motif searches, but
these typical rely on reductive assumptions about the interactions.

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

##Observation

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

This work still has more homogeneity in the atomic pieces of the model
than the social sciences, and many of the pertinent observations are the same
sort of properties that concern the physical sciences - temperatures, chemical
concentrations, sizes, masses, *et cetera*.  Hence, the largely equation-based
formal representation remains quite powerful.

However, given the extreme heterogeneity in kinds for most social science
questions, we contend that adopting the equation-based formalism to model those
problems is fundamentally flawed.  Yes, there are important numerical
measurements.  Yes, quantitative statistical analysis of the models is still
important.

But we should accept that many social science models are most naturally expressed
with the tools of mathematical logic, which are today most practically
implemented in general purpose programming languages.  While the physical
and natural sciences have a successful history with numerical models, and
certainly a tradition of model selection worth consideration, the best source of
practical model insight looks more like the videogame industry than physicists
working on statistical mechanics.

Prominent articles in the social sciences have previously argued this position,
though perhaps less specifically, by observing that agent-based
modeling is the more natural and powerful means to explore representations of
social phenomena.  Yet, some still insist on using networks as the fundamental
element of [those models][Snijders201044].  We propose a different lens - the
transient *events* are what should be modeled, not the ties.  The events are
then filtered through a model observation process which translates those events
into a network; this is where we have the opportunity to explicitly state how we
are aggregating.  We may then meaningfully compare predictions based on network
measurements to model outcomes via relevant tests.

###Aggregation

First, the matter of aggregation: as highlighted in [subsequent work][epidemics4], the
connection data tells substantially different stories depending how it is
sliced.  The network extracted from the data for their theoretical work is
used for a single epidemic season simulation, but that data is over several years.
Careful review of the users and hotspot locations indicates a high rate of
turnover in both.  Using a similar model for the spread of infection, changes in
tie aggregation windows result in very different disease outcomes.

###Homogenuous Kinds

> observe interaction events, but also observe state changes: a person has supplies
when they did not before.  they no longer have supplies they once had.  if the view
is only the network, then who cares.  if the view is the process, then...where did
these supplies come from?  go to?  if you see them with network associates, then
you simply missed an exchange events.  if you do not see them, then what contacts
might you be missing?

##Network Problems

We will begin by reflecting on modeling philosophy, then discuss some practical
outcomes of that philosophy when realized in general purpose programming, and
close with an implementation that reflects those values in a simulation of
covert groups.

One aside before we proceed: there have been great strides in non-model based
approaches to prediction, such as neural networks and classification trees, and
those alternatives are both powerful and avoid many problems associated
[with models][breiman2001statistical].  But to use them effectively, we must
carefully attend to the distinction between [explanation and prediction][shmueli2010explain].
We do not address these approaches here, but we do wish to highlight what
\"thinking carefully\" means for covert groups.  These approaches are largely
interpolative: the product is at most what is in the training data.  There may be
quite a bit in the training set, indeed enough beyond what is readily apparent to
any researcher to make the process look capable of extrapolation.
For covert groups, however, training data is sparse and what
is present may be mischaracterized.  Researchers attempting to apply non-model
based approaches to covert groups should be especially skeptical, disbelieving
any results that do not explicitly address substantial input censoring and
error.

##Principles of Modeling with Code

Scientific results are not measured by some idealized ability to explain and
predict, but by their use to those ends.  Regardless of their power,
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
what actually occurred in computer memory with the syntax improvements limited to
abstractions for convenient creation and manipulation of numerical types, and
for flow control.  The development and promulgation of object-oriented concepts
shifted how we reasoned about programs, leading to languages and libraries
designed for those concepts.  Modern programming languages bring even-more-natural
language syntax while maintaining the precision necessary to direct a computer,
allowing us to represent complex models with [literate code][knuth1984literate].
We will demonstrate this shortly using [Scala](http://www.scala-lang.org/).

First, there are some other practical considerations to modeling in code, also
analogous to features of good modeling with equations.

Equation-based models tend to relate a few parameters at a time, despite
attempting to model many different interacting phenomena.  Consider recent work
on addressing disease risk as a consequence of [mosquito ecology response to climate change][morincomrie2010].
In that model, many relevant ecological phenomena are included, each by an equation
linking a few variables at a time.  This general approach affords many advantages:

- each relation can be individually considered (for comprehension, for evaluation
of reasonableness, for experimental validation, *et cetera*)
- the model can be constructed incrementally; i.e., relationships can begin as
simple constant parameters and then later capture more sophisticated covariates
- generally, alternative relationships can be proposed and substituted into the model
- relationships are individually portable to other modeling contexts
- use of general equation forms (where accurate) allows leveraging analytical results for
those general forms (e.g., integrals, standard approximations, transformations)

The [Morin-Comrie model][morincomrie2010] also illustrates many bad
habits related to equation based modeling.  Many equations have atypical syntax
(e.g., functional relationships expressed as $f_T$ instead of $f(T)$, piece-wise
defined functions are discontinuous and expressed with the domains first).  Many
constants are expressed simply as numerical values rather than the fitted
parameters that they are, though this problem is inconsistent, contributing even
further to the confusion about which numbers of which variety.  Most of the
equations are from other sources, but
there is no effort to argue that application context is consistent with the source
context.

You can find many similar examples throughout scientific and engineering
literature, and while the balance of good and bad will vary, these general
themes persist.  They have analogies in code-based models, even when those
models go beyond representing equation-based models, and these analogies are
well-captured by general software engineering patterns and principles developed
over many years in that industry.

Providing a complete overview of that discipline is best left to other venues.
We will instead focus on few lessons from a [seminal work in this area][gofbook],
a particular technique known as [Test-Driven Design (TDD)][janzen2005test].

The principle lessons of the [Design Patterns book][gofbook] are that we should
build objects - in this case, Agents - by composing capabilities and that we
should couple those capabilities as loosely as possible.  That work, and
subsequent innovation built upon it, describes many ways to achieve that end.
In our demonstration, we will focus on a few -- *Delegate*, *State*, and
*Chain of Responsibility* -- which reflect the way scientific models acheive
reuse.

delegate - agent has some capability, executes it by plugging in some module then handing off
queries / method calls to that module.

state - response sets are driven by historical exposure.  allows agents to have different
responses to the same events without model having to create entirely new objects

chain of responsibility concept allows single event to trigger many responses from different
aspects of agent behavior.

Test-driven design is also a natural fit for scientific modeling.  As [Janzen and Saiedian highlight in their review][janzen2005test], test driven design encourages incremental development of
narrow, independent behaviors.  as new behavior added to a module, or modules integrated,
can use test infrastructure to verify that model still obeys constraints / context
expressed by tests.  This looks quite similar to good modeling practice: develop
simplest conceivable alternative models (which has many, many simplifying assumptions baked in).
those model results will suggest two routes: either experiments to distinguish models,
or that it is time to relax the assumptions and see if results hold.

in addition to
providing confidence in implementation details, the test-based approach provides
additional specification of what the model component does.

> it is possible to implement these by implementing many small models first, then
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


##Scala Demonstration Model
There are several approaches to achieve the aforementioned design patterns.  One
typical approach is via inheritance.  In some parent representation, we describe
all the actions our agents can take, and then we have child classes that
implement, override, or extend those actions.  

However, traditional linear
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

basic approach: agents mixin in traits (in this approach, all agents mixin the
  same traits, but those traits have state which make them inactive)

one *Universe* agent which handles agent interaction with outside world

*Universe* controls progress of time, logs external interactions

individuals log their interactions

in response to each tick, individuals issue interaction events to other
individuals.  in this simulation, people have homogenous distribution of
their probability to contact a family member (p to issue a message), and
homogenous lamba for number of hops for message (nuclear = parents, siblings, children all
  1 hop)

two basic social traits - family relationships, religious affiliation

third trait for covert group membership - all agents have it, only active in
some (recruits)

###Technical Asides
The [Akka Actor][akkk] framework is based on asynchronous calculation;
each agent represents its own thread of computation and memory.  Though we
implemented this particular example on a single machine (with multiple cores),
this independence abstraction makes it straightforward to reimplement
the simulation on many machines.

However, it requires overcoming perspective instilled in typical programming
training.  Namely, that directions to the computer are executed and resolve in
a particular sequence.  

all use a generic base agent trait to cover tracking through time:

{% highlight scala %}
trait TimeSensitive {

  private[this] var was = 0
  protected def last = was

  final def tick(when:Int) : Future[Try[Reply]] =
    Future({ resolve(when) }).andThen({
      case Success(res) =>
        was = when
        res
      case f => f
    })


  protected def resolve(when:Int) : Try[Reply] =
    Success(Ack)

}
{% endhighlight %}

This has two parts, one which handles initiating a new iteration, `tick`,
and one is a hook on which to hang other behaviors, `resolve`.  It also keeps
track of what time the agent is at via `last`.  This looks pretty basic

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

##Acknowledgements

We would like to thank [Thomas J. Hladish](github.com/tjhladish) and
[Juliet R. C. Pulliam](github.com/jrcpulliam) for their
feedback getting the voice right for this piece.

[janzen2005test]: <http://digitalcommons.calpoly.edu/cgi/viewcontent.cgi?article=1034&context=csse_fac> "optional title"
[barabasi1999]: <http://dx.doi.org/10.1126/science.286.5439.509> "optional title"
[atlas2012]: <http://www.sciencedirect.com/science/article/pii/S0370269312001852> "optional title"
[biernacki1981snowball]: <http://smr.sagepub.com/content/10/2/141.full.pdf> "optional title"
[knuth1984literate]: <http://dx.doi.org/10.1093/comjnl/27.2.97> "optional title"
[Ionides05122006]: <http://dx.doi.org/10.1073/pnas.0603181103> "optional title"
[volz2013inferring]: <http://dx.doi.org/10.1371/journal.pcbi.1003397> "optional title"
[Snijders201044]: <http://dx.doi.org/10.1016/j.socnet.2009.02.004> "optional title"
[morincomrie2010]: <http://dx.doi.org/10.1007/s00484-010-0349-6> "optional title"
[gofbook]: <> "Design Patterns"
[breiman2001statistical]: <http://projecteuclid.org/download/pdf_1/euclid.ss/1009213726> "optional title"
[shmueli2010explain]: <http://projecteuclid.org/download/pdfview_1/euclid.ss/1294167961> "optional title"
[akka]: <http://akka.io> "Akka"
[montreal]: <> "Montreal Network"
[epidemics4]: <https://github.com/pearsonca/epidemics4-talk/blob/master/poster.pdf?raw=true> "Epidemics 4 Poster"
[grossman1995portion]: <> "On a portion of the well-known collaboration graph"
