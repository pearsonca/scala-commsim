---
title: Challenges to Understanding Covert Groups
authors:
 - name: Carl A. B. Pearson
   email: cap10@ufl.edu
   corresponding: 1
 - name: Burton H. Singer
   email: bhsinger@ufl.edu
 - name: Edo Airoldi
---

##Abstract

Implicit in the label *dark network* is a suggestion that the activity of covert
groups is driven by - and therefore, may be understood in terms of - network
measures like degree distributions, paths, centralities, and so on.

That may be true in some cases, but the central assumption of network analyses,
that the represented entities and interactions are of a small set of formal
types, has limited empirical basis for covert groups.  Therefore, we should be
skeptical of applying network-based techniques to covert groups.

If we wish to more confidently use network-based analysis, we should ground the
performance of those techniques against more detailed models of underlying
phenomena. These detailed models suffer from a knowledge problem as well, but we
may reason about them in a more principled way and may consider many alternative
scenarios.  With these guesses about the underlying mechanisms, we can simulate
activity and then project observations of that activity into a network
representation for analysis.

We demonstrate such an approach by augmenting an empirical dataset with
synthetic results for simulated covert groups.  We discuss the results and
implications for naive application of social network analyses to covert groups.

* * *

##Introduction

Some network science approaches to social phenomena seem to amount to looking
for statistically significant differences on convenient mathematical metrics -
degree distributions, paths, centrality measures, *et cetera* - between
scenarios.

Network models, however, make strong assumptions - that the things we represent
as vertices are formally all the same *kind* of thing, that the interactions
between them are all of the same flavor.  Such assumptions can be reasonable,
and when they are, compact descriptions can usefully cover a broad array of
phenomena.  This is largely the basis for the success of [mathematics in the physical sciences][wigner1960unreasonable],
whether one subscribes to scientific realist or instrumentalist philosophies.

The natural and social sciences, however, consider phenomena more sensitive to
context and variation. For those phenomena, lumping elements into the same
categorical types can be an unreliable assumption.

One such questionable application is *dark networks*, the social network
representation of covert groups, which are often used as a means to model
criminal organizations, and then predict which individuals make the best
subjects for close observation or intervention.  By focusing on the network
reduction of these groups and associated network metrics, we may forget that the
network is a convenient representation and is not in fact the phenomena itself.
Even the aspects which have obvious translations into network representations
(*e.g.*, who knows whom) and have well-established options for data-gathering
(*e.g.*, snowball sampling) may be problematic for certain classes of covert
networks: snowball sampling may make [reasonable in-roads][biernacki1981snowball] for consumers and distributors of illegal
substances or persons with persecuted sexual preferences, but the techniques
required for successful snowball sampling are impractical for academics studying
violent terrorist organizations or state espionage apparati.  In some cases,
these groups may even be structured in a way - high compartmentalization, with
multiple layers of indirection - such that fundamental assumptions of snowball
sampling about the relationship structure are violated.

The phenomena is the interactions between and individual changes in the members
of that covert enterprise over the life of their collaboration, and the effect
those have internal and external to that organization.  Indeed, these are the
observations we actually make - not some network - and the outcomes we actually
care to understand.  Further complicating such a treatment, these groups exist
as foreground alongside a background population, with the populations largely
indistinguishable.  All of those concerns imply that mis-prediction by overzealous
application of network analysis could lead to erroneous action, and assorted
negative consequences, *e.g.* waste of resources, failure to counter threats,
creation of new threats via mis-identified individuals.

We should expect that representing such groups as a concise set of equations is
an implausible task, and as such we propose that concise mathematical models are
the wrong formal representation.  Rather, concise *programmatic* models are a
useful and practical representation.  This approach is not new; it is essentially
advocating for agent-based modeling.  We reiterate some insights from the
software engineering discipline on how to proceed in a reuse-able, replicable
fashion, then discuss how these practices relate to more traditional equation-based
models.

We motivate this discussion of the *dark network* approach, with a large
empirical dataset and its interpretations, using shorthand like *the Montreal
data* or *the Montreal network*.  We ultimately demonstrate characterizing
a simple network analysis - community detection - in terms of micro-simulation
of the covert group augmenting this dataset.

For longer notes, we will embrace an aside format as follows:

> ## The Montreal Municipal WiFi Service Data & A Basic Model
>
> In a [forthcoming publication][montreal], epidemiological modelers use data on
access to the Montreal Municipal WiFi service to build a contact network and
then consider the spread of flu-like pathogens on that network.  That work
focuses on a theoretical epidemiological question - whether or not a unique
network structure could explain a specific kind of epidemic dynamic.  The
relationship between the data and that network model, however, is ideal for
exploring many of the issues present in attempting to analyze covert social
groups with dark networks.
>
> The anonymized raw data is straightforward to understand.  Users have log on
and log off times at WiFi hotspots associated with the service.  How the data
are translated into a network for the epidemiological analysis is also simple:
users that logged into the same location at the same time are joined by an edge.
Those edges are then aggregated into a contact network, removing duplicates and
self loops.
>
> However, the raw data also be viewed directly as a time-sensitive (based on login-logout pairs),
bipartite (person-to-hotspot) graph.
>
>The data spans roughly five years, a few hundred thousand entries of login data
with around 200k users and roughly 350 hotspot locations.

## Behind Network Modeling

[Snijders *et al.*][Snijders201044] provide an outline of stochastic simulation
using agent-based models.  They advocate for this perspective as a useful
approach to longitudinal data, and they explicitly commit to the network
representation and to expressing dynamics in terms of network features.

However, we think it is strange to think in terms of, *e.g.*, triadic closures
instead of friendly introductions.  We propose that the transient *events*
(*e.g.*, individuals meet), *state changes* (*e.g.*, an individual gains or
expends money), and *observation* process of how those are recorded (*e.g.*,
people record transactions at particular locations at overlapping times) are
precisely what we should focus on modeling.

This is not to discount the value of network science techniques.  We suggest
that instead one must be cautious in their application when there is not a very
complete, clear, and uniform translation of observed phenomena into a network,
for the previously discussed formal issues with network representations.  One
way to take that caution is to step back into the messier details - the events,
states, and their observations - and simulate those as a ground truth.  When we do
that, we must clearly state the ways we believe the system might work.  We may
take those simulation results, and then translate them into networks.  Again, to
accomplish that, we must clearly state how we believe our observations relate to
the real phenomena and how we aggregate our observations into reduced measures.
Finally, we may perform our network science analyses and compare those results to the simulation inputs
where we know truth (*model* truth, that is).

In a very loose sense, we are performing a Bayesian analysis by integrating over
our priors (the space of our plausible individual models) to characterize our
confidence in the outcome of some network-based metric.

Implementing an agent-based simulation in this mode can actually be more intuitive.  The
events and state changes should correspond to phenomena we could observe, and
likely do observe, if perhaps not very easily for the covert members.  We have a
long history of models of such individual and small-group behavior, even if
perhaps those are simply story-telling models. Likewise, we can engage
critically with an explicitly modeled observation process.

> ### What is happening in the Montreal Data?
>
> The Montreal dataset comprises entries of unique user id login and logout
times to the publicly provisioned wifi system at a unique hotspot hardware id.
>
> We could very easily flatten this dataset into a time-aggregated,
person-to-person network and then perform some network-based analysis.  This is
precisely what the initial purveyors of the data did.
>
> Instead, we choose to view the data not as a network, but as the series of events
they represent, that *might* be able to inform us about a particular group.  To do
we have to model what the data mean.  For example, we could work backwards:
>
> - people near enough to some physical location take action to access the wifi
system; how often do they login if they are at that location?  how often does
the login represent that person being within the location?
> - people go to that location; are they going to meet particular other people?
if they are going to meet other people versus for some product or service, does that influence their use of the hotspot?
> - people with relationships (*e.g.*, friendship, work collaboration) interact
in a way correlated with that relationship; how often does that interaction manifest
as going to locations like those with the municipal wifi service?  likewise, people
go places for individual purposes as well; what is that behavior like?
> - locations adopt or leave the municipal wifi service; how do they decide to do so?
> - businesses open and close, and people join and leave the municipal population; what do
those turnover rates look like?

Thinking about what a dataset means and how it came to be gives us a potential
model to reproduce these events.  Some of the model components probably look
like traditional network descriptions (people that work together, what locations
a person likes to visit), but many may not.  Those that do correspond to network
relationships might be unlikely to manifest in the available data - *e.g.*, in
the case of the Montreal data, ignoring your boss while out together for coffee
to check your email might be unwise.

In Snijders *et al*, this mechanically-oriented aspect appears in the objective
function, framed in terms of network change events as determined by current
network and individual traits.  Their overall perspective is about model
selection (which objective function to use) and parameter fitting (what values
for the coefficients in the objective function).  Our emphasis differs - we want
to build some practical sensitivities and confidence about various approaches - but model selection and parameter fitting are just as
possible using, *e.g.*, [Algorithmic Bayesian Computation (ABC)][toni2009approximate]
and [Partial Least Squares (PLS)][geladi1986partial] for exploring and characterizing parameter space.  Indeed, that
would be a reasonable approach to trying to reproduce the features of the Montreal
data itself.

One final thought on the general advantage of this approach: we focus explicitly
on the *explanatory* aspect of science.  Alone, that is not sufficient to be
science: we must also commit to prediction and testing.  Or, in practical
circles, we are going to engage in interventions based on our analysis, and
monitor the consequences.  Those interventions may have a clearer translation to
a non-network perspective.  For example, if the intervention is to interfere
with financial transactions, presumably an individual then has less money to use
and that can be captured in a resource accumulation and expenditure model.  It
is less clear what that means in terms of a model that is only about the
creation and destruction of ties - does it mean fewer ties given less money to
distribute?  Does it mean more ties as the individual ramps up work to get
money?

## Modeling in Code

It may appear daunting to model agents in terms of many potentially interacting
behaviors.  Not just as a theoretical matter, but as a practical one - there is
little by way of easily accessible and reusable behavior libraries to mix into
agents. Many of the agent-based frameworks could potentially support behavior
libraries, but a [\"CRAN\"][cran]-like resource has yet to emerge.  However, there is
substantial value to had if something akin to that did appear.

If we are especially careful in our implementations of these models, we can
isolate particular aspects, and then reuse them.  We might have, for example,
agents that perform arbitrage.  That implementation, carefully abstracted, might
be portable from an initial context of stock market actors to criminals dealing
in black market goods.  Clearly, the inputs are different, but if we believe the
core mechanisms are the same, then we ought to implement it once and then reuse
that element.  This has dual advantages. There is the practical matter of having
more thoroughly vetted models faster. The second is that re-use could gradually
smooth the abstraction into what is actually conserved across those domains, and
further highlight what differs between contexts.

This would also make agent-based modeling begin to look a lot more like what
happens in the software engineering community, and in particular quite a bit
like the videogame industry.  Particular behaviors and recurring situations in
software are isolated into re-usable objects and design patterns.  While a game like,
say, \"The Sims\" might not be useful as a scientific model, the underlying implementation
may provide substantially more practical insight than traditional numerical models.

Certainly, our ambitions with any particular agent-based model are narrower than a massive
entertainment franchise.  We must formally specify a practical model that can
produce the kind of data we have.  At marginally less coarse level, we need a
description of the actors (agents) over time, their interactions, how those interactions
are recorded as the events comparable to our data.  We should start with an anecdotal, natural language
description, which must then be translated into exact syntax.  Traditionally,
that has been equation-based mathematics, but a computer program can also be exact.

Good equation-based models have some recognizable features, like parameter
choices that elucidate mechanics, consistency with typical domain labeling, and
forms that are easily computable.  Indeed, these models often continue to parse
very closely to a reasonable natural language description.  For some phenomena and levels of realism, however, the equation-based expressions
become to convoluted or arcane.  This is where an approach like agent-based modeling
becomes the appropriate exact abstraction.  Those features of traditional models remain desirable, however.
This sort of programming was initially described by Knuth as [\"literate programming\"][knuth1984literate],
and essentially aims to achieve code that is as close to a natural language story
as practical.  The intent being that people who were algorithmically inclined -
scientists, engineers, mathematicians - would be able to comprehend what the
program represented without having to be expert in the hard syntax constraints.
For more complex programs, the more complex code required can be enhanced for
these lay readers by adopting [test-driven design (TDD)][janzen2005test] practices.  The test code
can then communicate additional essential perspective on what various pieces are *meant* to
do.  Finally, for commonly recurring problems, adopting generic solution
templates - typically called [design patterns][gofbook] -
can do for a program what tropes do for television: provide huge context with compact
shorthand.  Though we do not delve into the details, the implementation we provide
shortly embraces several of these patterns, most notably Chain of Responsibility and Strategy.

There have been several decades of trends driving those software engineering
practices, independent of any directed impetus to enhance agent-based modeling.
Initially, most general purpose languages reflected
what actually occurred in computer memory with the syntax improvements limited to
abstractions for convenient creation and manipulation of numerical types, and
for flow control.  The [development and promulgation][dahl1966simula] of object-oriented concepts
shifted how we reasoned about programs, leading to languages and libraries
designed for those concepts.  Modern programming languages bring even-more-natural
language syntax while maintaining the precision necessary to direct a computer.
We will demonstrate using this approach for agent-based modeling using [Scala][scala]
and the Actor library developed by [Akka][akka].

## Aside: Augmenting Empirical Data

An easy approach to validating network-based covert group detection techniques
would be to take some empirical network, add a covert group network, and
then add some random connections between the two.  Remix a statistically satisfying
number of times, and presto, suitable subject for analysis?

Recall the Montreal data schema:

| user | location | log in time | log out time |
|:====:|:========:|============:|=============:|
| ABC  | 123      | mdy:hms     | mdy:hms + $\delta$ |
| etc  | etc      | etc         | etc |

Which exactly translates to a *time-dependent, bipartite network*.  If one collapses
on location, that would produce a *time-dependent, unipartite network*.
Then using a reasonable window, *e.g.* a day, one could aggregate events into a daily
time series of networks.

This is the way we will use the Montreal data, because this a typical starting
point: an existing daily series for an empirical social network, to which we add
a similar, synthetic times series of covert member network representations (also
sourced elsewhere), and then test our detection scheme.  Instead of specific
covert networks, we will be simulating covert group interaction events instead.
A detailed review of that data, which we can unfortunately not include in this publication
due to data release issues, would serve to further highlight why the event-based
approach is preferrable.  There are many aspects of the dataset which indicate
that accepting the data as what it presents itself to be - the presence of people
at locations - is naive, but that an approach that discards outliers would also
be censoring valuable information.

Why data augmentation?  For starters, convenience - it eliminates the need to simulate
the background and guarantee realistic features.  It also encourages tailoring
approaches that are suitable to actual data that might be available for these
detection methods.  Finally, it is the nature of the particular groups that we
are looking to find that they are rare in the wild, thus is unlikely that one
is actually present in the data and would interfere would analysis.

When we augment the data with the covert groups, we impose a few additional
constraints.  The point of this analysis is to particularly characterize network
approaches when, presumably, those approaches are required.  If the covert group
were obviously distinct in some other fashion (bizarre login times, anomalously
frequent usage or location counts, *etc*.), then there would be no point to a specifically network-based
analysis and we should expect the results to be uninteresting anyway.  The
R script `investigate.R` in the source for this publication covers calculation
of ensuring that the covert members look like normal users in terms of
frequency of visits and number of locations visited.

## An Implementation

As discussed earlier, the Montreal data series is a time-sensitive, bipartite graph
derived from particular users accessing particular hotspots for particular times.
Our goal is characterize a detection scheme against a covert group generating that kind of data.

> ## Using the Montreal Data
>
> Recall that the Montreal data is unique users joining and leaving unique
hotspots, and that close examination of this data indicates there is regular
turnover (immigration and emigration) in users and locations.  For our model, we
will assume that the background actually has a constant number of actual
locations, and that turnover represents those locations adopting or quitting the
municipal service.
>
> For users, we assume that their turnover represents entering and leaving the
system.  Thus, the non-covert population depends on the window of the view.  We
might reasonably conclude that some changes represent identical users changing
authentication credentials.  However, for simplicity we ignore that possibility
in this model.

As such, our simulation agents will need to, as part of their routine, create similar
access records, both as a product of individual behavior and group membership.

The events generated by these agents will then need to be integrated with the
empirical dataset, flattened into a time series of unipartite networks, and
the community detection algorithm run against that time series.  The script for
launching supercomputer runs on a PBS-based system with Torque arrays enabled is
contained in the repository, `run.pbs`, and the community detection script is
`analyze.R`.  That code is largely bookkeeping commands, but the source for
the agents highlights our points about modeling-in-code.

We can start with their declaration:

{% highlight scala %}
Agent extends TimeSensitive with Travels[TravelData] with CSVLogger[TravelData]
{% endhighlight %}

and

{% highlight scala %}
Universe extends TimeSensitive with PoissonDraws with CSVLogger[TravelData]
{% endhighlight %}

which can be understood as *an Agent is time sensitive and travels*, the *Universe is time sensitive and uses
a Poisson process generator* and the whole simulation
records *travel data* to the csv format.  These illustrate some of the broad categories that
agent `trait`s could fall into:

 - simulation backbones, such time-based iteration
 - re-useable state and dependencies, such as location-visiting
 - output, such as recording data to csv files
 - mathematical utilities, such as probabilistic generators

The abstract implementation of these traits and then their explicit incorporation
in the simulation entities is worth examining.  We elide code that exists to
enable distributed simulation because it could be automatically generated and is
not informative about the modeling process.

{% highlight scala %}
trait TimeSensitive {
  // ...
  protected def _tick(when:Int) = when
}
{% endhighlight %}

Quite boring, right? It receives a time, and replies with that time.
Recall, however, that traits are meant to be consumed:

{% highlight scala %}
class AgentImpl(visProbPerTick: Probability, /*...*/) extends Agent {

  override def _tick(when:Int) = {
    if (!_traveled && (Math.random < visProbPerTick)) {
      // if I haven't already been directed to travel, I might randomly do so
      // ...
    } else {
      _clearTravel
    }
    super._tick(when)
  }
}
{% endhighlight %}

Here we provided agent behavior by extending the default behavior.  In
this particular case, we have intercepted the call, stochastically had
the agent visit a location, then returned to behavior defined elsewhere by
closing with `super._tick(when)`.

Using Scala traits, we can focus on exactly what we are modeling:
what the agent does relative to the passage of time in the simulation.  There
are only two lines devoted to the syntax requirements (the `override def ...`
and `super._tick(when)`) as compared to the content devoted to the model
mechanics.  We have also kept the details necessary to make this code functional
in an asynchronous setting (*e.g.*, in cluster computation) in the trait itself,
and out of the way of the researcher producing the simulation (and thus away
from inadvertent mistakes).  Finally, using a build tool (such as `sbt` or
`make`) or the developmental `macro` feature in Scala could reduce that
footprint even further, though we avoided these capabilities to best balance our
discussion of model choices and the gory details of model implementation.

If this agent had other traits that were also `TimeSensitive`, then the chain
of execution would simply continue on those until finally hitting the root trait.  For example,
we might want an agent that alters behavior with age, so we need a way of keeping
track of age:

{% highlight scala %}
trait Age extends TimeSensitive {

  private[this] var _age = 0
  def age = _age

  override def _tick(when:Int) = {
    _age += 1
    super._tick(when)
  }

}
{% endhighlight %}

This `agent.age` could then be used to, say, modify an agent's tendency visit places.

Researchers experienced with the aforementioned gory details of simulation will
have noticed by now that `Age` shows some of the weaknesses in our
`TimeSensitive` implementation, *e.g.* what happens if `agent.tick(when = 1)` is
called repeatedly?  An agent with `Age` would grow older by multiple increments,
despite actually being directed to respond to the same time-tick.

That sort of command is an easy error to make in an entirely local simulation,
so we ought to be addressing it purely from the standpoint of having simulation
components that highlight logical errors.  However, once we start simulating
agents on multiple nodes in a super-computer setting, it becomes a necessary
concern even if our simulation is error-free.

We have elected a simpler implementation to avoid distraction from other
points.  But the structure of our implementation - *i.e.*, in a `trait` with
a method that the adopting agent (and potentially other `trait`s of that agent)
links into a chain of execution - means that we can fix this problem in the `trait`.
With no changes to *scientific* meaning of this model, or other models re-using
this trait, we can \"upgrade\" this aspect to do that error handling.

With that introductory discussion covered, we will more succinctly introduce the
other traits we used in our particular implementation.

{% highlight scala %}
trait Travels[ResultType] {

  private[this] var traveled : Boolean = false
  def _traveled = traveled
  def _clearTravel = traveled = false

  def travelResult(location:Int, ts:TimeStamp) : ResultType

  // ...

  protected[this] def _travel(
    location: Int,
    ts: TimeStamp
  ) : ResultType = {
    traveled = true
    travelResult(location, ts)
  }

}
{% endhighlight %}

This provides for agents visiting locations at particular times, and tracks if
they have traveled \"recently\".  We use this for a simple agent state tracking
capability.  Note that state information can actually be encapsulated in the
trait - agents with this trait do not have to also implement tracking for their state.

These traits are sufficient to fully define our agent from a model perspective:

{% highlight scala %}
class AgentImpl(/* ... */) extends Agent {

  // ...

  override def travelResult(location: Int, ts: TimeStamp) = TravelData(-1, id, location, ts)

  override def _tick(when:Int) = {
    if (!_traveled && (Math.random < visProbPerTick)) {
      log( _travel( location = randomLocation, ts = randomHour ).copy( when = when ) )
    } else {
      _clearTravel
    }
    super._tick(when)
  }
}
{% endhighlight %}

The agent can be directed to travel, but if it has not, it randomly visits the
normal haunts at some stochastic rate.  In addition to the components of the
scientific model, there is also the algorithmic infrastructure in the
`CSVLogger` trait has desirable features.

That trait is written to be re-usable for any type of agent that might need to
record data, but it is also written in a way that a different sort of logger
(*e.g.*, SQL database) or different format (*e.g.*, JSON) could be written in an
analogous trait and the simulation could be re-run to write to a different data store by merely
changing the trait from `CSVLogger` to, say, `SQLogger`.

Finally, rather than use these agents directly in `main` method, we use a `Universe`
\"agent\".  In agent-based simulations, we have detailed agents inside some boundary
conditions.  Commonly, however, there are also exogenous forces that we need
to evolve as part of the simulation.  That is, the boundary is not a collection
of constants, but also an entity requiring updates as the simulation proceeds.
When a simulation calls for both micro-simulation (*i.e.,* the agents) and an
\"external\" macro-simulation, then having a `Universe` entity is an practical
representation.  Our `Universe` is:

{% highlight scala %}
class Universe(/* simulation parameters */)
  extends TimeSensitive with PoissonDraws with CSVLogger[TravelData] {

  // ...

  val agents : Seq[Agent]
    = Universe.createAgents(groupSize, locationCount, meetingLocations, avgLocs, visProb, fh)

  var daysToNextMeeting : Int = nextDraw()
    // initial days-to-next-covert meeting

  override def _tick(when:Int) = {

    if (daysToNextMeeting < 0) {
      daysToNextMeeting = nextDraw()
    } else {
      if (daysToNextMeeting == 0) { // time to meet
        val loc = shuffle(meetingLocations).apply(0)
        val ts = TimeStamp(nextInt(9)+8, nextInt(60), nextInt(60) )
        // choose place, time

        Await.result(
          Future.sequence(
            shuffle(agents).take(2).map( agent => agent.travel(loc, ts) )
            // randomly draw 2 agents to meet at a specific place, time
          ),
          400 millis
        ) foreach {
          res => log(res) // record results
        }

      }
      daysToNextMeeting -= 1
    }

    agents foreach { a => a.tick(when) }
    super._tick(when)
  }

}  
{% endhighlight %}

We use `Universe` as the root of the simulation, building the agents within
itself, running the state of the external world - specifically, how often the covert group
receives orders to meet - and handing off the `ticks` that drive the simulation.
There is also a final utility trait, `PoissonDraws`, which handles the
stochasticity for the external world:

{% highlight scala %}
trait PoissonDraws {

  val expectedK : Double

  lazy val poissonK : Iterator[Int] = DiscreteCountStats.poissonK(expectedK)

  def nextDraw : Int = poissonK.next()

}
{% endhighlight %}

This trait represents a particular strategy for choosing the duration between
meetings.  If we wanted to use a different distribution (*e.g.*, binomial or
exponential), then we could use a `Universe` with an alternative trait.

Using these agents, we can generated a synthetic time series of events to augment
the empirical data set based on a parameter sweep of meeting frequency, size of the
group, and number of meeting locations.  The details of that are available in
the `Demo.scala` file in the source directory.

## The Detection Test

Now we consider a simple, but non-trivial community detection method against the
augmented data.  This algorithm is not intended to detect a particular network
signature of the covert group; any model is unlikely to have a realistic signature anyway.
Instead, we analyze the detector performance assuming
that some other technique accurately identifies a covert member at random, and then that random
member's associated community is then implicated as the covert group.

How should we think about this performance?  We obviously want to characterize
placing all of the covert group, and only members of that group, into a single
community as perfect.  The worst possible performance would be completely
dispersing the covert group into different communities, and for each community
to have a covert group member.  The perfect case has a True Positive Rate ($TPR$)
of 1 - *i.e.* is absolutely sensitive - and a False Positive Rate ($FPR$) of 0 -
*i.e.* is absolutely specific.  The worst case has a $TPR = 0$, but its not
immediately obvious how to calculate FPR; the average community size is of
course the total background population divided by the number of covert group
members.  If we use the total population as the denominator, then the peak
$FPR != 1$, and indeed is determined by the size of the covert group. There
is some argument for having an error rate that would be independent of the
covert group size, but we need not obsess on mathematical elegance for this
demonstration.  We thus have our formal TPR and FPR:

\begin{equation}
TPR = \sum_i\frac{n_i-1}{n_t-1} \\
\end{equation}
\begin{equation}
FPR = \sum_i\frac{n_i-1}{n_t-1}\frac{b_i}{b_t}
\end{equation}

With $TPR, FPR$, and tunable process for community detection, we can create Receiver
Operating Characteristic (ROC) curves, and associated statistics, *e.g.* discrimination.
For our analysis, however, we did not attempt to tune detection.

Since we have a time-dependent process, and in general we are interested in real
time detection, we should of course be interested in the time evolution of the
efficacy of detection.  This would provide us some qualitative insight on
trade-offs between risk of the covert group executing some goal, opportunity
cost (in terms of missed covert members), and downside (in terms of collateral
population implicated).  Our initial demonstration analysis produced curves that
contain this data, though we make no attempt to do subsequent meta-analysis like
the trade-off consideration.

If we had more basis for informing the model of the covert group, we could
likewise propose detectors that used the more detailed data produced by the
simulation, and revise our definition of what detection entailed.  If we had
reliable data about covert group signature, then perhaps we might update our
criteria to be correctly identifying the particular community, rather than
couching performance in terms of another assumed process.

## Results

## Afterthoughts

## Acknowledgements

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
[wigner1960unreasonable]: <http://math.northwestern.edu/~theojf/FreshmanSeminar2014/Wigner1960.pdf> "Unreasonable effectiveness of Mathematics in the Natural Sciences."
[cran]: <http://cran.us.r-project.org/> "The Comprehensive R Archive Network"
[scala]: <http://www.scala-lang.org/> "The Scala Language"


## HOLD SECTION

###Modeling in Code

However, the traits of good equation-based models have analogies in code,
particularly modularity and clear mechanism.  These two were difficult to put into
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

Providing a complete overview of that discipline is best left to other venues.
We will instead focus on few lessons from a [seminal work in this area][gofbook],
a particular technique known as [Test-Driven Design (TDD)][janzen2005test].

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

Chain of Responsibility
: concept allows single event to trigger many responses from different
aspects of agent behavior.

Test-driven design is also a natural fit for scientific modeling.  As
[Janzen and Saiedian highlight in their review][janzen2005test], test driven design
encourages incremental development of narrow, independent behaviors, which is
consistent with [iterative model evalution practices][koopman].  As new
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
