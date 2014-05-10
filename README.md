##Challenges to Understanding Clandestine Groups
###Overview
Implicit in the label *dark network* is a suggestion that the activity of
covert groups is driven by -- and therefore, may be understood in terms of --
network effects.  That may be true.

These network representations, however, are a remarkably simple description, and
simple models of all but the most trivial phenomena must make very strong
assumptions.  We have little basis to assume anything: we accurately gauge our
knowledge by calling them *dark*.

We may, however, use approaches that have demonstrated performance in the
natural sciences -- namely explicit inclusion of the observation process, and
iterated parameter fitting and assumption relaxation.  Natural science phenomena are often close enough to
physical science phenomena for largely numerical approaches, but we argue that
the complexity of social science phenomena demand a formal expression more in
the vein of general purpose programming.

We close with a practical demonstration.

Network science approaches to social phenomena often reduce to comparing various
centrality measures: between groups, between times, aggregating on different
interactions, *et cetera*.  The network, however, is only a representation.
Unless that network explains (and preferably, predicts) some phenomena, it has
no meaning beyond formal fancy, and those centrality metrics must recognized to
be similarly uninformative.

As a remedy, we discuss two perspectives: first, naturalism expressed via
formal models, and second, the formal incorporation of the observation process.
The first philosophy is perhaps most embraced by physical scientists, encouraged
no doubt by the theoretical (certainly not always practical) ease with which
problems in that domain can be carefully constrained.  Those constraints are
largely implausible for natural scientists, hence modelers in that domain must
also incorporate unknowns for the observation process.

Network representations and subsequent analyses are of course models, but we
should anticipate to do little better than interpolation when we fail to connect
that representation to meaningful interactions.  Furthermore, when we ignore the
observation process -- known to be flawed in the case of clandestine actors --
we imperil our ability to even interpolate.

Using these perspectives, we argue for a network science perspective that
thinks less of the network as the phenomenon, and acknowledges that the network
is an observed distillation of social processes.  We discuss a framework for
applying this approach, and demonstrate using it.

###The Physical and Natural Scientific Traditions
Like the social sciences, the physical and natural sciences are concerned with
explanation and prediction.  They cycle from observation, to explanation, to
prediction, and back to observation.  Of course, any of those steps may serve as
the entry point, and there's often back and forth, but the general trend is
round that inductive-deductive loop for all three broad disciplines.

We give them different names, however, because they are different in meaningful
ways.  Of course their subject matter differs, but if that were the end of it,
we would just be talking about a matter of taste.  No, that subject matter gives
rise to practical differences.

The physical sciences have it the easiest, in a scientific sense.  The kilogram,
meter, and second are the same in North America (jokes about Imperial units
aside), Europe, and Asia.  They are the same on the Moon and Mars and
any-where or -when else we have managed to look.  As are the fundamental effects
in play (some caveats to do with scales aside).  This makes the observation part
of the loop philosophically easy (not always practically so, *e.g.* the
Large Hadron Collider).  While we may misinterpret those observations, experience
suggests we do so consistently.  So, if we someday visit the exo-planets we are
observing now, they may well be some other thing -- but we could confidently
modify our predictions to that other thing, without having to do much re-
interpretation of the observations.

These simplifying constraints may explain in part why the physical sciences are
so thoroughly invested in the mathematical modeling - because it is
philosophically easy (again, fiddly in practice, but the grasping the concept is
easy) to put useful quantities in and get useful quantities out.  Of course,
that explanation is a question for social science, which does not have the same
advantages.

The natural sciences, however, share some of these advantages, since many of
observations that are pertinent to natural science overlap with those in the
physical sciences (e.g., temperatures).  However, the natural world does not
share the physical sciences homogeneity in kinds -- while hydrogen and oxygen
may make up water the world over, there is a much greater diversity of life
consuming it, and substantial individual heterogeneity even within species.
Observations are thus less obviously reliable.  Homogeneity in kind means that
any hydrogen atom is fine (give or take a nucleon), but high heterogeneity
means the scientist must take care considering how the data arrived -- are the
field specimens a random sample?  Or did the collection process tend to select
individuals with less aversion to humans?  If there was selection bias, how does
that matter relative to the conclusions being drawn?

For many problems in the natural sciences, the mechanism of the observation
process can be reasonably represented.

###Naturalism Expressed in Code
Our scientific results are not measured by some idealized ability to explain and
predict, but by scientists using them to those ends.  Certainly, some of that is
driven by advertisement of the ideas -- publication, participation in workshops
and conferences, *et cetera* -- as does what is actually offered by those ideas.

But, subtly, how those ideas are expressed -- *i.e.*, the mathematics and code
-- is also critical.  

the level of phenomenological complexity in social sciences demands that, for
any non-trivial / non-toy example, the only suitable formal representation will
be in code.  This is not just a matter of numerical necessity (e.g., the
number of arithmetical steps), but more a matter of requiring a more sophisticated
language.

###Scala Constructs
One of the under-appreciated benefits of modeling with code is the exacting
syntax; like mathematical representations (which most any code is formally a
subset of), we must commit to a precise practical meaning.  This guarantees that
a particular set of inputs (including any randomization seeds) consistently
produces the same outputs.

However, as with mathematical models, we have choices about the form of that
expression.  In mathematical expressions, we often have choices about notation
(*e.g.,* $\overline{a}$ versus $\braket{a}$, $\dot{x}$ versus
$\frac{\del x}{\del t}$), about variable names ($x, y, z$ versus $i,j,k$), and
choices about specific forms (*e.g.*, the various parameterizations of
probability distributions).  Like choices we make in natural language, these will
enlighten or obscure, help one audience while hindering another, and prove more
or less useful depending on the application.

The same is also true of our choices in code.  Until recently, the
notation options were restricted to be written quite like what the computer was
exactly doing.  Which is to say, it was quite a lot like pure mathematical
expression, and as argued earlier, though the code is indeed math at its root,
actually expressing it as math fails to compress into human-usable expressions.
This is not burdensome to comprehension when addressing a model that is little
more than a gigantic maths exercise, but becomes quite so when expressing other
problems.

Software engineering has acknowledged this for several decades now: the formal
construct of a word processor have naught to do with linear algebra, and thus
the notation, naming, and implementation language has evolved to best serve the
problem.  Social scientists may find valuable philosophical perspective from the
physical and natural sciences, but as to practical tools to use, they would be
better served heeding the development techniques of, say, the videogame industry.

In keeping with those challenges, that discipline has developed languages which
can be written and read much more naturally, while still maintaining sufficient
precision to unambiguously instruct a computer.  Traditionally, domain specific
languages had more natural-language like syntax, but restricted to their particular
domains, and these languages were typically not flexible enough to be practical
for general purpose programming.  However, there is now a language which is general
purpose, is cross-compatible with an existing widely used language, has agents
built into the standard library, and was designed with the explicit goal being
a more natural language: Scala.

We will not dwell on Scala's syntactic details, because we feel that the code
snippets in our demonstration section will adequately convey how natural it is
to write.

###Modeling Observation

###Practices from Software Engineering

####Test Driven Design
[TDD][janzen2005test] = looks exactly like Koopman's inference loop

####Design Patterns for Modeling
basic unit of simulation is the agent

want to code in a way the minimizes rework -- not just for practical reasons,
but so that we may be confident of the foundation.  Reuse means we gain all the
confidence in the past work that has accrued, but also in revisiting the old code
we may fix bugs, etc.  if we've been working in the open, repeatable science mode,
then we should be able to easily revisit past work that may have been effected by
the bug.

how do we encourage reuse?  by building pieces in a modular way.  this has a number of
advantages -- easier to test, easier to get domain experts to reason about their
piece, easier to upgrade pieces (both computationally and representationally).
The principle disadvantage appears when we want those pieces to talk to each other.

modular pieces - design patterns book suggests many ways to achieve these ends.
We suggest one based strongly on those concepts (mixing chain of responsibility,
delegation, and state) and another based on those concepts, but relying
on the scala syntax and multiple inheritance resolution.  We prefer the latter,
since it achieves many of the desirable aspects of reuse without requiring
as much tinkering to get up and running.  It does not preclude some problems (e.g.,
namespace collisions), but the sort of modeling care needed to avoid those is far
lower than the sort of care to properly apply the design patterns.

####

###Demonstration
We demonstrate using the framework to create individual-based models, incrementally composed
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

[janzen2005test]: http://digitalcommons.calpoly.edu/cgi/viewcontent.cgi?article=1034&context=csse_fac ""
