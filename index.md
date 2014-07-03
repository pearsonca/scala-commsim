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
the measurements typifying physical sciences, we do not yet have centuries
of argument to provide context and meaning to what exactly these networks mean.

Fortunately, we can leverage developments in natural sciences that explicitly
incorporate observation into the modeling process.  We demonstrate these, with
particular emphasis on agent-based models and a development approach the
encourages reuse, replication, and refutation.

This approach acknowledges the lessons of traditional equation-based models (the
primary approach of physical and natural sciences), while also embracing ideas
from software engineering that are a more appropriate perspective for agent-based
modeling.

* * *

##Introduction

Some network science approaches to social phenomena seem to amount to looking
for statistically significant differences on degree distributions, paths,
centrality measures, *et cetera* between scenarios.

A network, however, is a *very* simple model of a group, and simple models (of
all but the most trivial phenomena) are only obtained by making strong
assumptions.  Such assumptions can be reasonable, and when they are,
compact descriptions can usefully cover a broad array of phenomena.  One particularly
strong assumption is homogeneity within kinds and in interactions between kinds.
This is an assumption that is frequently justified for problems in the physical
sciences.

The natural and social sciences, however, consider phenomena more sensitive to
context and variation. Thus, homogeneity is can be an unreliable assumption in
the natural sciences, and can border on suspicious in the social sciences.

One particularly questionable application is *dark networks* - the social
network representation of covert groups, which is often used to model criminal
organizations.  By focusing on the network reduction of these groups and
associated network metrics, we may forget that the network is not the phenomena.
The phenomena is the interactions between and individual changes in the members
of that covert enterprise over the life of their collaboration, and the effect
those have internal and external to that organization.  Indeed, these are the
observations we actually make - not some network - and the outcomes we actually
care to understand.  Additionally, if we are considering these groups as
foreground embedded in a background population, the problem is vastly
complicated by observation issues and computational concerns.

Representing those observations in a concise mathematical model, however, is an
implausible task.  In that sense, the network reduction is a very practical tool.

We propose that concise mathematical models are the wrong formal representation.
Rather, concise *programmatic* models are a useful and practical representation.
Strictly speaking, these are still mathematical models (based on the equations
of mathematical logic rather than numerical equations), but that position is
confusingly pedantic given that many of the mathematical power tools are
provably unavailable (general proofs, most notably).  This approach is not new;
it is essentially advocating for agent-based modeling.  We reiterate some
insights from the software engineering discipline on how to proceed in a
reuse-able, replicable fashion, and discard some past emphasis on embedding
those agents in a network.

To motivate the discussion of notable pitfalls in the *dark network* approach,
we will refer to back to a dataset and
its interpretations to illustrate that skepticism, using shorthand like *the
Montreal data* or *the Montreal network*.  For longer notes, we will embrace an
aside format as follows:

>##The Montreal Municipal WiFi Service Data & A Basic Model
>
>In a [forthcoming publication][montreal], epidemiological modelers use data on
access to the Montreal Municipal WiFi service to build a contact network and
then consider the spread of flu-like pathogens on that network.  That work
focuses on a theoretical epidemiological question - whether or not a unique
network structure could explain a specific kind of epidemic dynamic.  The
relationship between the data and that network model, however, is ideal for
exploring many of the issues present in attempting to represent covert social
groups with so-called dark networks.
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

Ultimately, we will use the *Montreal data* to highlight some of the issues with
network-based approaches in a simulation, and discuss what sort of steps should
be taken with *dark network* research questions.

##Assumptions Gone Wild

The strongest assumptions in most network analyses concern homogeneity and
observational accuracy.

Homogeneity assumptions take many forms, some obvious, some subtle.  We will
focus on the assumption of homogeneity in kinds, as that is perhaps the most
fundamental assumption in network models: that edges are edges are edges, and
likewise with vertices.  In some systems, this is quite reasonable, but when
the network is organized (by design or spontaneously) to accomplish some task,
the parts and their interactions are not typically universally interchangeable.
We will also highlight the problems with aggregation in time, from the standpoint
of scientific analysis as well as practical application.

Observation assumptions come in levels of strength.  The strongest being of
course that what is observed is the whole truth and nothing but the truth.  This
is typically weakened to some censoring of events (*i.e.*, false negatives) and
some mislabeling of non-events (*i.e., false positives*), but with errors that
independent and identical distributed (usually according to a simple distribution, as well).
However, with covert groups, the (non-)observation process is censored in a
deliberate, but unknown, manner for the covert actors, and confounded by
overlapping behavior in the background population.

The issues with these assumptions can be mitigated by adding complexity to
traditional equation- and network-based models.  However, at some point this
complexity becomes impractical for scientific work - it clouds reasoning about
those formal systems, can be source of implementation error, or can simply
overwhelm the abilities of researchers.

###Homogeniety

*Homogeniety in kinds* is the modeling assumption that is
that pieces of some kind in the model are identical (and often, therefore,
interchangeable).  To be clear, this an assumption about the model entities and
inputs, not an assumption that, say, all vertices have identical degrees or
centralities.  Indeed, the assumption is critical in many analyses that we
expect to measure different values, like centralities or module membership.

Most centrality metrics and modularization schemes are expressed in terms of
unweighted, undirected edges.  Some of these measures have generalizations
to account for weighting and direction, though an unscientific survey of
publications indicates these are rarely used.  But these all still rely on edges
and vertices being the same *kind* - there are fewer methods for computing
these metrics when edges are of *different kinds*{: .todo title="venice marriage-trade block model work?"}.
The typical resolution is to simply create separate networks for each tie-type,
re-run the metrics on each distinct network, and then speculate about the results.

As with edges, homogenizing assumptions are typically made for vertices as well.
Contact distribution studies are replete with this sort of assumption.  Extreme
differences in contacts for flight attendants versus theoretical mathematicians can become
artificially interesting when we treat those jobs and the kind of interactions
they entail as the same kind of thing.  Certainly for some questions, this
homogenization might be reasonable.  But for many others, valuable insight
arises when our model preserves distinctions.  This lets us highlight [Erdos][grossman1995portion]
and differentiate flight attendants on private, regional, and hub-to-hub flights.

Bipartite networks may appear to address this problem, but are often simply
adding another *kind*. Having two kinds of interactions still homogenizes those
interactions - *e.g.*, a white collar worker (person kind) visiting a coffeeshop
(location kind) en route to work in morning is a very different sort of
interaction than even that same white collar worker visiting an alley in the
dead of night.

> ###Homogeniety in the Montreal Network
>
> The Montreal source data homogenizes on several key aspects:
>
> - all user logins are treated as equal
> - all locations are treated as equal
>
> We can imagine and even find indications in the data of potentially meaningful censoring that occurs due to this
homogenization.  In the context of transmissible disease:
>
> - some of the *users* might simply be shared business connections (the data has
several high utilization users that are consistent with this sort of use)
> - some of the locations might have many ways for co-users to transmit disease,
while others might be relatively sterile (though not included in the published data,
the raw data includes geo-location of hotspots, which can in turn be tied to very
different sorts of businesses)
>
> Previous work using this data treats connections as homogeneous
in terms of their capability to serve as a transmission route.  But there is quite
a range of variability in the co-location times: some users overlap mere seconds,
while others overlap for hours.
>
> Indeed, transmission for pathogens like influenza that can persist in the
environment, introducing a directed edge between users that visit a location
one after the other is quite sensible relative to the process being modeled.

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

###Observation

In the simplest version of scientific pursuits, observation is direct truth: we
read off the ruler tick marks to know the dimensions of our subject.  By degrees,
however, this idealization must be sacrificed to practical realities.

Our observations have precision errors: we manufacture rulers with different lengths, or
we read the tick marks incorrectly.  Observations are transformations of the subjects:
we apply the ruler to what see through the microscope.  Observations are sensitive
to the subject's behavior: we use the ruler on something traveling a substantial portion
of the speed of light.  Subjects are very difficult to measure and appear rarely:
we must engineer rulers of colossal proportions and apply them over and over to
find what we seek.

For the physical sciences, this is traditionally the end of observational
difficulties (ignoring effects that preclude simultaneous measures of particular
features).  For the natural sciences, all of these problems are amplified, and
we start to see the problem of measurements changing meaning as we continue in
time and of the act of measurement having practical consequences for the subject.
For all the time spent smashing protons attempting to find the Higgs boson, we
may reliably expect that having started the same experiment a few years (even decades)
earlier or later would not affect our resulting knowledge, nor has the nature of
protons generally been altered by running that experiment.

If we actively sampled (*e.g.*, systematically modified environmental conditions
and observed response) a comparable number of any non-microbe species on earth,
even accounting for the time scale of the experiment, that species would be
irrevocably altered and quite possibly extinct. Because the individuals involved
also have notably more variability than protons, its even possible that the
question behind this massive hypothetical study might not be answered.

The social science version is even more practically complicated.  There are few
ethical dilemmas involved in basic physical science research (applications is
another matter, as any query of academic literature on, *e.g.*, `atomic bomb`
will reveal).  Not so for social science research, where even mildly
[active observations][facebook] at any appreciable scale elicit research ethics debates
(even if those studies have basically indiscernable effect sizes).  Likewise, in
addition to natural human biological variation, there are additional layers of
cultural life history diversity.

For social science, we a thus restricted to primarily passive observation of natural
experiments, with restricted ability to conduct active experimentation on a very small scale.
Covert groups, by definition, further complicate this passive observation.

> ### Observation in the Montreal Data
>
> The Montreal dataset is explicitly user logins to the publicly provisioned wifi
system at locations that have adopted that service.
>
> If we restrict our view to the very narrow activity this data covers - people
using wifi at places - we can still imagine significant concerns mapping our observations
to conclusions about co-location:
>
> - people do not always use the wifi, and some do not even have accounts
> - not all places use the public wifi service
> - some of the locations are close enough together that people may be at one location,
and use another's hotspot
> - multiple people may share a single account, or one person may have multiple
accounts
>
> Some of these obviously apply based on the data (*e.g.*, if businesses had as few
customers as many locations had users, they would fail).  Others would entail
gathering extensive additional data (*e.g.*, counting competing hotspot service
installations).

For certain flavors of covert groups - *e.g.*, consumers and distributors of
illegal substances, persons with persecuted sexual preferences - snowball
sampling can make [reasonable in-roads][biernacki1981snowball].  However, the
techniques required for successful snowball sampling are conspicuously untenable
for certain classes of covert groups, such as violent terrorist organizations or
state espionage apparati.  In some cases, these groups may even be structured in
a way - high compartmentalization, with multiple layers of indirection - such
that fundamental assumptions of snowball sampling about the relationship
structure are violated.

Stringent observation limits come up more often in the natural sciences.  Recent work has begun to tackle these problems in
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
important.  But we should accept that many social science models are most naturally expressed
with the tools of mathematical logic, which are today most practically
implemented in general purpose programming languages.  While the physical
and natural sciences have a successful history with numerical models, and
certainly a tradition of model selection worth consideration, the best source of
practical model insight looks more like the videogame industry than physicists
working on statistical mechanics.

The more application-oriented reader might at this point note though these
observation restrictions exist for academic research, they are less applicable
for law enforcement, military, and intelligence gathering organizations.  While
not unbound by rules, it is true that these entities are typically more capable
of more extensive passive observation and executing active experiments, by
nature of looser restrictions and more resources.  The fact remains that their
observations will be incomplete, clouded by background data, subject to
deception (of both the targets and biased analysts).  These effects might be
lessened for these organizations, but the consequences of action and inaction
certainly reflect higher stakes than typical academic outcomes.

##Addressing These Issues With Agents

###Brief Modeling Aside

Mathematical equations are the traditional way to accomplish the necessary
precision for scientific expression.  Those relations always rely on assumptions,
and the scientific utility of these ideas in turn rests on whether those assumptions
are accurate enough.  If the idea is sound, but the assumptions are not, relaxing
those assumptions can lead to a more accurate expression, but often this results
in reducing our ability to understand and work with that expression.  This means
that increased accuracy is not the same as increased utility.

Hence, we arrive at the problem with using simple mathematic structures and the
required strong assumptions, particularly homogeneity in kinds and strong observation,
with a problem like covert groups where those assumptions are not obviously
applicable.  General purpose programming languages compress mathematical notation complexity into
smaller forms, enabling us to formally model concepts like these covert groups in a way
that is both precise and possible for scientist to grapple with.  Note that while we
are distinguishing here the notion of simply using computers to better solve equations,
we do not suggest that such models would be equation free.

Prominent articles in the social sciences have previously argued this position,
though perhaps less specifically, by observing that agent-based modeling is the
more natural and powerful means to explore representations of social phenomena.
Yet, some still insist on using networks with particular properties (including
specific arrangements) as a fundamental assumption of [those models][Snijders201044].
Nor do those publications or most textbooks on agent-based modeling discuss what
principles of numerical models are worth preserving, let alone how to implement
them in agent-based models.  These concepts and guidance on how to achieve are
present, however, in the field of software engineering, if perhaps without
explicit ties to what makes good equation-based models.

Those familiar with equation based models will recognize some of the features of effective
expression:

 - conventional and consistent notation to leverage pre-existing knowledge,
 - variable choices that clearly connect the model to the phenomena,
 - composition of complex phenomena out many simple relations

Recent work on addressing disease risk as a consequence of [mosquito population response to climate change][morincomrie2010]
provides an example of both good and bad execution of this philosophy.
In that model, many relevant ecological phenomena are included, each by an equation
linking a few variables at a time.  This general approach affords many advantages:

 - each relation can be individually considered (for comprehension, for evaluation
of reasonableness, for experimental validation, *et cetera*)
 - the model can be constructed incrementally; *i.e.*, relationships can begin as
simple constant parameters and then later capture more sophisticated covariates
 - generally, alternative relationships can be proposed and substituted into the model
 - relationships are individually portable to other modeling contexts
 - use of general equation forms (where accurate) allows leveraging analytical results for
those general forms (*e.g.*, integrals, standard approximations, transformations)

This model also illustrates many bad
habits related to equation-based modeling:

 - several equations have atypical syntax (*e.g.*, functional relationships
   expressed as $f_T$ instead of $f(T)$, piece-wise defined functions are discontinuous and expressed with the domains first),
 - constants are used inconsistently: sometimes they are geometric values (*e.g.*, for rescaling temperature)
 sometimes they are fitted parameters, while both of those kinds are also sometimes
 expressed with variables,
 - most of the equations are from other sources, but there is no effort to argue that application context is consistent with the source
context.

The scientific and engineering literature are replete with these sort models,
and while the balance of good and bad will vary, these general themes persist.
They have analogies in code-based models, even when those models go beyond
representing equation-based models, and these analogies are well-captured by
general software engineering patterns and principles developed over many years
in that industry.

Providing a complete overview of that discipline is best left to other venues.
We will instead focus on few lessons from a [seminal work in this area][gofbook],
a particular technique known as [Test-Driven Design (TDD)][janzen2005test].

Initially, most general purpose languages reflected
what actually occurred in computer memory with the syntax improvements limited to
abstractions for convenient creation and manipulation of numerical types, and
for flow control.  The [development and promulgation][dahl1966simula] of object-oriented concepts
shifted how we reasoned about programs, leading to languages and libraries
designed for those concepts.  Modern programming languages bring even-more-natural
language syntax while maintaining the precision necessary to direct a computer,
allowing us to represent complex models with [literate code][knuth1984literate].
We will demonstrate this shortly using [Scala](http://www.scala-lang.org/).

###Agent-Based Solutions

In the aforementioned publication advocating for agent-based modeling, Snidjers
et al *explicitly argued against modeling events*{: .todo title="double check this"}.
While we agree with their argument generally for agent-based models, we contend
that the transient *events* are what should be modeled, not the ties.  If we wish
to subsequently apply network science methods (as is the case with evaluating
*dark network* conclusions), then we can filter these events through
a model observation process in a pure network reduction.  We can also apply more
sophisticated options, such as those used in biochemical network analysis that
capture vertex state, by altering how that observation filter works.

> observe interaction events, but also observe state changes: a person has supplies
when they did not before.  they no longer have supplies they once had.  if the view
is only the network, then who cares.  if the view is the process, then...where did
these supplies come from?  go to?  if you see them with network associates, then
you simply missed an exchange events.  if you do not see them, then what contacts
might you be missing?


First, there are some other practical considerations to modeling in code, also
analogous to features of good modeling with equations.


The principle lessons of the [Design Patterns book][gofbook] are that we should
build objects - in this case, Agents - by composing capabilities and that we
should couple those capabilities as loosely as possible.  That work, and
subsequent innovation built upon it, describes many ways to achieve that end.
In our demonstration, we will focus on a few -- *Delegate*, *State*, and
*Chain of Responsibility* -- which reflect the way scientific models achieve
reuse.

Delegate
: agent has some capability, executes it by plugging in some module then handing off
queries / method calls to that module.

State
: response sets are driven by historical exposure.  allows agents to have different
responses to the same events without model having to create entirely new objects

chain of responsibility
: concept allows single event to trigger many responses from different
aspects of agent behavior.

Test-driven design is also a natural fit for scientific modeling.  As
[Janzen and Saiedian highlight in their review][janzen2005test], test driven design
encourages incremental development of narrow, independent behaviors, which is
consistent with [iterative model evalution practices][koopman].  as new
behavior added to a module, or modules integrated, can use test infrastructure
to verify that model still obeys constraints / context expressed by tests.  This
looks quite similar to good modeling practice: develop simplest conceivable
alternative models (which has many, many simplifying assumptions baked in).
those model results will suggest two routes: either experiments to distinguish
models, or that it is time to relax the assumptions and see if results hold.

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

One aside before we proceed: there have been great strides in other algorithmic
approaches to prediction, such as neural networks and classification trees, and
those alternatives are both powerful and also avoid many problems associated
[with traditional equation-based models][breiman2001statistical].  But to use
them effectively, we must carefully attend to the distinction between
[explanation and prediction][shmueli2010explain]. We do not address these
approaches here, but we do wish to highlight what \"thinking carefully\" means
for covert groups.  These approaches are largely interpolative: the product is
at most what is in the training data.  There may be quite a bit in the training
set, indeed enough beyond what is readily apparent to any researcher, thus
making the process appear capable of extrapolation. For covert groups, however,
training data is sparse and what is present may be mischaracterized.
Researchers attempting to apply these sort of algorithmic based approaches to
covert groups should be especially skeptical, disbelieving any results that do
not explicitly address substantial input censoring and error.

###Aggregation

First, the matter of aggregation: as highlighted in [subsequent work][epidemics4], the
connection data tells substantially different stories depending how it is
sliced.  The network extracted from the data for their theoretical work is
used for a single epidemic season simulation, but that data is over several years.
Careful review of the users and hotspot locations indicates a high rate of
turnover in both.  Using a similar model for the spread of infection, changes in
tie aggregation windows result in very different disease outcomes.

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
[koopman]: <http://publichealthpractice.com/project-detail/transforming-public-health-surveillance-2/> "forthcoming chapter"
[facebook]: <http://dx.doi.org/10.1371/journal.pone.0090315> "facebook thing"
[dahl1966simula]: <http://dx.doi.org/10.1145/365813.365819> "simula"
