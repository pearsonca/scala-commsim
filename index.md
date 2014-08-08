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
types, have limited empirical basis for covert groups.  As such, we should be
skeptical attempting network-based techniques on these covert groups.

If we wish to more confidently characterize the performance of these assorted network criteria, we
must acknowledge they are applied to shadows of the true phenomena.
We can make guesses about the underlying mechanisms, simulate them, and then project those
observations of those mechanics into a network representation for analysis.

We demonstrate just such an approach by augmenting an empirical dataset with
synthetic results for simulated covert groups.  We discuss the results and
implications for naive application of social network analyses to covert groups.

* * *

##Introduction

Some network science approaches to social phenomena seem to amount to looking
for statistically significant differences on degree distributions, paths,
centrality measures, *et cetera* between scenarios.

Network models, however, make strong assumptions - that the things we represent
as vertices are formally all the same *kind* of thing, that the interactions
between them are all of the same flavor.  Such assumptions can be reasonable,
and when they are, compact descriptions can usefully cover a broad array of
phenomena.  This is largely the basis for the success of [mathematics in the physical sciences][wigner1960unreasonable],
whether one subscribes to scientific realist or instrumentalist philosophies.

The natural and social sciences, however, consider phenomena more sensitive to
context and variation. For those phenomena, lumping elements into the same
categorical types can be an unreliable assumption.

One such questionable application is *dark networks* - the social network
representation of covert groups, often used as a means to model criminal
organizations.  By focusing on the network reduction of these groups and
associated network metrics, we may forget that the network is not the phenomena.
The phenomena is the interactions between and individual changes in the members
of that covert enterprise over the life of their collaboration, and the effect
those have internal and external to that organization.  Indeed, these are the
observations we actually make - not some network - and the outcomes we actually
care to understand.  Further complicating such a treatment, these groups exist
as foreground alongside a background population, with the populations largely
indistinguishable.

We should expect that representing such groups as a concise set of equations is
an implausible task, and as such we propose that concise mathematical models are
the wrong formal representation.  Rather, concise *programmatic* models are a
useful and practical representation.  This approach is not new; it is essentially
advocating for agent-based modeling.  We reiterate some insights from the
software engineering discipline on how to proceed in a reuse-able, replicable
fashion, and discard some past emphasis on embedding those agents in a network.

To motivate this discussion of the *dark network* approach, with a large
empirical dataset and its interpretations, using shorthand like *the Montreal
data* or *the Montreal network*.  For longer notes, we will embrace an aside
format as follows:

>##The Montreal Municipal WiFi Service Data & A Basic Model
>
>In a [forthcoming publication][montreal], epidemiological modelers use data on
access to the Montreal Municipal WiFi service to build a contact network and
then consider the spread of flu-like pathogens on that network.  That work
focuses on a theoretical epidemiological question - whether or not a unique
network structure could explain a specific kind of epidemic dynamic.  The
relationship between the data and that network model, however, is ideal for
exploring many of the issues present in attempting to analyze covert social
groups with dark networks.
>
>The anonymized raw data is straightforward to understand.  Users have log on
and log off times at WiFi hotspots associated with the service.  How the data
are translated into a network for the epidemiological analysis is also simple:
users that logged into the same location at the same time are joined by an edge.
Those edges are then aggregated into a contact network, removing duplicates and
self loops.
>
>The data spans roughly five years, a few hundred thousand entries of login data
with around 200k users and roughly 350 hotspot locations.

Ultimately, we will use the *Montreal data* to highlight some of the issues with
network-based approaches in a simulation, and discuss what sort of steps should
be taken with *dark network* research questions.

## Modeling in Code

[Snijders *et al.*][Snijders201044] argue for agent-based models, but argue
explicitly for avoiding the *event based* perspective.  We propose that the
transient *events* (*e.g.*, individuals meet), *state changes* (*e.g.*, an
individual gains or expends money), and *observation* process of how those are
recorded (*e.g.*, people appear together at particular locations) are precisely
what we should focus on modeling.

Implementing an agent from this perspective requires a model that is inherently
possible to reason about and argue with.  The events and state changes should
correspond to phenomena we could observe, and likely do observe, if perhaps not
very easily for the covert members.  We have models of such individual and
small-group behavior, even if perhaps those are simply story-telling models.
Likewise, we can engage critically with an explicitly modeled observation
process. Are we monitoring wifi logins?  We can grapple with the details of such
a system: how are accounts obtained?  Do people rotate accounts?  Do businesses
rotate accounts? Maybe we cannot practically answer these questions, and maybe
we do not include all of them in our model - but we can at least acknowledge
them.  These sort of challenges reflect those traditionally possible with good
equation-based models.

None of this discounts the value of network science techniques.  We suggest that
instead one must be cautious in their application when there is not a very
complete, clear, and uniform translation of observed phenomena into a network, for
the previously discussed formal issues with network representations.
One way to take that caution is to step back into the messier details - the
events, states, and observations - and simulate those as a ground truth.  When
we do that, we must clearly state the ways we believe the system might work. We
may take those simulation results, and then translate them into networks. Again,
to accomplish that, we must clearly state how we believe our observations relate
to the real phenomena and how we aggregate our observations into reduced
measures.  Finally, we may perform our network science analyses with some
confidence about what their results mean because we can compare those to the
simulation inputs where we know truth (*model* truth, that is).

If we are especially careful in our implementations of these models, we can isolate
particular aspects, and then reuse them.  We might have, for example, agents that perform
arbitrage.  That implementation, carefully abstracted, might be portable from an
initial context of stock market actors to criminal gangs.  Clearly, the inputs
are different, but if we believe the core mechanisms are the same, then
we ought to implement it once and then reuse that element.  This has dual advantages.
There is the practical matter of having more thoroughly vetted models faster.
The second is that re-use process will gradually smooth the abstraction into
what is actually conserved across those domains, and further highlight what
differs between contexts.

We demonstrate doing just this with applying simple community detection to a
covert group embedded in a larger population.  The point of this effort is less
about the results - they are predictably laughable given the simplicity of the
behavior model and detection algorithm - and more about providing some guidelines
about what constitutes a scientific approach to the problem, instead of scientism
or inapplicable, if elegant, mathematics.

Here we are going to use an augmentation model, where we take an empirical data
set as the background and add to it synthetic data as the foreground.  We are
assuming that the empirical data set has nothing interesting in it, based on the
reasoning that for rare and difficult to observe phenomena, like the activities
of covert groups, it is unlikely that these will meaningfully appear in any
untargeted sample.   The synthetic model has whatever behavior we want to explore,
*e.g.*, more or less cautious covert groups, and produces (among other measurements)
at least whatever kind of interaction we have in the empirical data set.

> ##Using the Montreal Data
>
> Recall that the Montreal data is unique users joining and leaving unique
hotspots, and that close examination of this data indicates there is regular
turnover (immigration and emigration) in users and locations.  For our model, we
will assume that the background actually has a constant number of actual
locations, and that turnover represents those locations adopting or quitting the
municipal service.
>
> For users, we assume that their turnover represents entering and leaving the
system. We might reasonably conclude that some changes represent identical users
changing authentication credentials.  However, for simplicity we ignore that
possibility in this model.
>
> We will also aggregate wifi access data to a daily interval, *i.e.* a user
accessed the system at particular location on a particular day.
>
> *Why these assumptions?*{: .todo title="yeah carl, why?"}

- covert group members (agents in the simulation) have some probability of turning over user ids
- per unit time, pairs / groups receive direction to meet around a target time
- within that window, they have exponentially distributed time-to-device log-ins,
of course with possibility of drawing times outside window

### Augmenting Empirical Data

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

Why not covert networks?

In the scenario where the networks produced from the Montreal data as described
above is treated as social network background truth for detecting covert groups
against, the modelers have not carefully compared what the network means and is
being treated as meaning.

The implicit interpretation is that the network is a sample of which people meet
at businesses on what days.

The problem, however, is that this empirical data probably does not mean that,
and that a network translation of would hide that fact.

We could not know that without access to the raw event data,
despite the forthright description of sensible transformations (not unlike what
one might see for typical network datasets).  Even if we were careful about looking
at network metrics, we would see results that typify ubiquitously cited networks:
power law degree distributions, but with actual community structure beyond
random graphs.

When we start to delve into what the event data say, the inadequacy becomes apparent.
If we think from what the data are first, login and logouts to hotspots, we can
imagine the associated human activity that produces this data, and then put some
boundaries on that image by comparing it to data.

We can start with the typical session length, about 70 minutes.  That seems
reasonable: we can imagine mixing the coffee-shop-email-checking set with the
public-library-job-hunting set.  Maybe some weekday versus weekend use.  But if
we dig any further, we find what should be unsettling results: roughly ten
percent of the entries have zero duration sessions.  What does that mean?  More
bizzarely, around one percent have a duration greater than a twelve hour working
day.  Does it make sense to treat either of those sessions as a \"person\" that can meet
with others?

If our starting point is some extant network, we can hope that the authors were
careful: they noticed these oddities, highlighted them, and provided an
alternative network time series filtering out high and zero session duration
events.  That is better than nothing, but which is the right one to use?  The event data would have to be
checked against ground truth to answer that, since maybe the times are just reporting
errors.  It is unlikely, however, that authors using convenience-sourced datasets, like the
Montreal data, since investigating would rapidly become inconvenient for the
supplier.  Alternatively, the researchers might have gone to some of the locations
in the dataset, and directly observed behavior for comparison to the dataset.  For
the Montreal data, they would have needed some specialized equipment (*e.g.*, connection
sniffers) and planned to visit multiple locations for a few weeks, and so on and so forth.  While
the prospect of a working vacation in Montreal is no doubt appealing to some,
what we want to emphasize is the reality of the nit details that apply to validating any convenience data.
A reality that makes so-called convenient data rapidly annoying.

And this is just the first of many oddities in the source (*e.g.*, the number of
locations that have one vistor ever indicates that just about everywhere in
Montreal must be going out of business).  As stated, Before we can ever treat
this series of events as serious scientific data, let alone reduce to a network
representing social contact, we would need to do some serious empirical
follow-up.  We are certain that what we find is that these oddities are rarely
actually dismissable (and even in the case   of measurement errors, may needed
to included in simulation as well), but they may represent cases that are far
outside how our covert group will present. Thus, given this thorough follow-up,
we would need to do some careful modeling of human activity to see if we could
get a model of the activity we want - in this people visiting locations, some
together and some not - to reproduce these events. At that point, we would have
a suitable basis to start serious modeling of covert member behavior, which
could then be synthetically added to the dataset, and finally an network-based.

We, unfortunately, are only theorists working with a third-hand, unpublished dataset and a deadline,
so that careful work is implausible.  Instead, we have use supposition about what
is going on in background population.  Fortunately, we do have the event data to
use.

Based on the usage statistics and personal anecdote, we could reasonably infer
that many of the users represent tourists, particular those that make a single
login.  Those will probably not influence community structure, and might even be
plausibly excluded from the false positive rate by excluding background users
that have no future logins from FPR calculations.  We will retain them, however,
to provide conservative estimates.  For locations that have short total hotspot
availability, we can assume that businesses are not created and closed
so quickly.

## An Implementation

The Scala `TypedActor` paradigm allows us to define `traits`s and then mix those
traits into our agent-based models.  We demonstrate
several broad categories of traits with our simple model embedding a covert group
in the Montreal data:

 - simulation hooks, such time-based iteration
 - re-useable state and dependencies, such as location-visiting
 - output, such as recording data to csv files
 - mathematical utilities, such as probabilistic generators

* * *

Our simulation backbone, in this case following the passage of time:

{% highlight scala %}
trait TimeSensitive {

  final def tick(when:Int) : Future[Int] = Future({ _tick(when) })

  protected def _tick(when:Int) = when

}
{% endhighlight %}

This trait seems quite boring - it receives a time, and replies with that time.
Recall, however, that traits are meant to be consumed:

{% highlight scala %}
class AgentImpl(visProbPerTick: Double, /*...*/) extends Agent {

  override def _tick(when:Int) = {
    if (!_traveled && (Math.random < visProbPerTick)) {
      // do something on this time step
      // ...
    } else {
      _clearTravel
    }
    super._tick(when)
  }
}
{% endhighlight %}

Here we provided agent behavior by overriding the default behavior.  In
this particular case, we have intercepted the call, stochastically had
the agent visit a location, then returned to behavior defined elsewhere by
closing with `super._tick(when)`.

Most notably, we have focused on what in particular the agent does relative to
the passage of time in the simulation.  There are only two lines devoted to the
program mechanics (the `override def ...` and `super._tick(when)`) as compared
to the content devoted to the model mechanics.  We have also kept the details
necessary to make this code functional in an asynchronous setting (*e.g.*, in
cluster computation) in the trait itself, and out of the way of the researcher
producing the simulation.  Finally, using a build tool (such as `sbt` or `make`) or the developmental
`macro` feature in Scala could reduce that footprint even further, though we
avoided these capabilities to best balance our discussion of model choices and
the gory details of model implementation.

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

Researchers experienced with simulation will have noticed by now that `Age`
shows some of the weaknesses in our `TimeSensitive` implementation, *e.g.* what
happens if `agent.tick(when = 1)` is called repeatedly?  An agent with `Age`
would grow older by multiple increments, despite actually being directed to
respond to the same time-tick.

That sort of command is a simple error to make in an entirely local simulation,
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

And of course, if there was sufficient adoption in Scala,
*e.g.* levels akin to the current R or Python modeling communities, we would see
a distribution system and automatic version updates improving all of the dependent
simulations.

With that introductory discussion covered, we will more succinctly introduce the
other traits we used in our particular implementation.

{% highlight scala %}
trait Travels[ResultType] {

  private[this] var traveled : Boolean = false
  protected[this] def travelResult(location:Int, ts:TimeStamp) : ResultType

  def _traveled = traveled

  def _clearTravel = {
    traveled = false
  }

  def travel(
    location: Int,
    ts: TimeStamp
  ) = Future { _travel(location, ts)  }

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
they have traveled \"recently\".

These traits are sufficient to fully define our agent from a model perspective:

{% highlight scala %}
class AgentImpl(
  val id:Int, val normLocs:Seq[Int],
  val visProbPerTick:Probability, val fh:BufferedWriter) extends Agent {

  // ...

  override def _tick(when:Int) = {
    if (!_traveled && (Math.random < visProbPerTick)) {
      // making own trip if not directed to take one
      log( _travel( location = randomLocation, ts = randomHour ).copy( when = when ) )
    } else {
      _clearTravel
    }
    super._tick(when)
  }
}
{% endhighlight %}

The agent can be directed to travel, but if it has not, it randomly visits the
normal haunts at some stochastic rate.  In addition to the components of the scientific model,
there is also some obvious algorithmic infrastructure in the `CSVLogger` trait,
which provides the `log(...)` method.  That trait is written to be re-usable
for any type of agent that might need to record data, but it is also written in
a way that a different sort of logger (*e.g.*, SQL database) or different format
(*e.g.*, JSON) could be written in an analogous trait and the simulation could
be re-run merely changing by merely changing the trait from `CSVLogger` to the
new type.

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

  def nextDraw() : Int = poissonK.next()

}
{% endhighlight %}

This trait represents a particular strategy for choosing the duration between
meetings.  If we wanted to use a different distribution (*e.g.*, binomial or
exponential), then we could use a `Universe` with an alternative trait.

Using these agents, we can generate a synthetic time series of events to augment
the empirical data set.

### Characterizing Network Detection Methods

Now we consider a simple, but non-trivial community detection method against the
augmented data.  The detector is not intended to detect a particular network
signature of the covert group; our model is unlikely to have a realistic signature anyway.
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

### Parameter Setting

If the covert group were detectable by non-network methods - *e.g.*, if they had
statistically outlying behavior on frequency or diversity of activities - then
the network analysis would be quite silly.  It may well be the case that they
are noticeably different, but we are considering cases where network analysis
methods provide valuable insight.  As such, we must inform the behavior of our
simulated covert group by those values for this particular study. More
generally, for any sort of augmentation study, we should seek to ensure that our
synthetic group mimics the background in ways that would fool whatever
techniques we would use ahead of the one we wish to validate.

> ### Key Montreal Measures
>
> The Montreal data concerns user, locations, and wifi access.  The first
analysis for interesting user activity should of course be to test for users
that have very high or low numbers of locations visited, and likewise users
that have very high or low frequency of use.  Given that the data forms a time
series, it would probably also be useful to examine the distribution of visits
- *e.g.* steady cycling between locations or highly skewed visitation, stable activity
level versus long periods of no activity between very ones - however, we are not
going into that level of detail for this study.
>
> From the Montreal data, we must then obtain some crude measures:
>
> - distribution of location counts visited by users
> - distribution of visitation rate for users
>
> From those distributions, we can select mean values for the covert group to
abide.  If we were being more sophisticated in our model, we might sample from
the distribution for our covert group.
>
> Thus, we will have our group members target their overall behavior against
these means, where the covert group activity (rate of clandestine meetings,
number of meeting locations) deduct from these background-typical behaviors.

### Results

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
[wigner1960unreasonable]: <http://math.northwestern.edu/~theojf/FreshmanSeminar2014/Wigner1960.pdf> "Unreasonable effectiveness of Mathematics in the Natural Sciences."

## HOLD SECTION

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
among mathematicians and differentiate flight attendants on private, regional, and hub-to-hub flights.

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
> We can imagine and even find indications in the data of potentially meaningful
censoring that occurs due to this homogenization.  In the context of
transmissible disease:
>
> - some of the *users* might simply be shared business connections (the data has
several high utilization users that are consistent with this sort of use)
> - some users or locations may in fact be identical, simply with new credentials
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
Ising models of phase transitions in a physical material), and sometimes it's acceptable given the
specific question (e.g., the growth of the world wide web or citation networks).
Sometimes [posterior analyses][barabasi1999] find this assumption is not
obviously precluded (though they may argue a stronger conclusion), though do not
necessarily follow up with prediction and validation.

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
time and the act of measurement having practical consequences for the subject.
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

###What's Good About Equation-Based Models?

Mathematical equations are the traditional way to accomplish the necessary
precision for scientific expression.  Those relations always rely on
assumptions, and the scientific utility of these ideas in turn rests on whether
those assumptions are accurate enough (assuming faithful execution of the
ideas).  If the idea is sound, but the assumptions are not, relaxing those
assumptions can lead to a more accurate expression, but this can result in
reducing our ability to understand and work with that expression, which means
that increased accuracy is not the same as increased utility.

Hence, we arrive at the problem with using simple mathematic structures and the
required strong assumptions, particularly homogeneity in kinds and strong observation,
with a problem space like covert groups where those assumptions are not obviously
applicable.

If we instead embrace the higher level abstraction of mathematics that general
purpose programming languages represent, then we have some chance to formally
model these groups in a way that is both precise and possible for researchers to
grok.  We are distinguishing here representing generic objects and simply
larger, more complicated systems of equations.  Certainly, computers are useful
with the latter, and we are not suggesting that models using the former would be
equation free.  However, we argue that `agents` of any appreciable complexity
would require too many equations to formally capture with that kind of mathematics,
but if we leverage the formalism of general purpose programming we can do so
with relative (if not absolute) ease.

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

###Observation in Equation-Based Models

REDO

###Modeling in Code

Given the extreme heterogeneity in kinds for most social science
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
