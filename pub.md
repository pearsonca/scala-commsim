---
title: Qualifying Network-based Detection Schemes Against Embedded Synthetic Groups in Empirical Data
authors:
 - name: Carl A. B. Pearson
   email: cap10@ufl.edu
   corresponding: 1
 - name: Burton H. Singer
   email: bhsinger@ufl.edu
 - name: Edo Airoldi
---

##Abstract

Judging performance of various network analysis schemes to detect covert populations
is hard.  One way to gain confidence about the performance is to take real empirical
data, synthesize the covert population in various ways, insert them into data, and then run detection
scheme against augmented dataset.

##Introduction

##Data Set

##Methods

The Montreal Data comprises (1) user ids logging into (2) location ids at (3) start
times until logging out at (4) stop times.

If we are to validate methods at detecting a particular population in such data,
that population must be known.  For a population that deliberately conceals itself
from observation, ...

A synthetic population must also produce this behavior.  Covert groups, by
definition, attempt to conceal their identity.  For the purposes of this
validation approach, we will assume that the group members "act like" general
population. That is, the number of locations they visit, the rate that they use
the system, their duration of usage, *etc* - these should similar to the rest of
the population. Of course, particular covert groups will have varying degrees of
success, and actual groups may well produce a signal within the data which does
not require network-based approaches to detect.

How do we specifically replicate the observed behavior?

First, we will trim the empirical population to a subset relevant to our study:
individuals with repeated appearances and locations with repeated visitors.  The
goal of our validation is to assess methods for detecting groups behind repeated
visits of individuals.  One off visits are irrelevant.

Second, we will trim other entries inconsistent with a meeting between individuals: entries with a duration
longer than a day, TODO other?.

With the trimmed data set, we compute a few values:

 - what is a location's average instantaneous share of users?
 - what is the distribution of unique locations that a user visits?
 - how skewed is their preference for visiting locations?
 - what is their distribution of visit start times?
 - what is their distribution of visit durations? (TODO: given visit start time?)

Now, when we create our synthetic population, we can make it mimic the observed
population in credible ways:

 - group members will visit locations proportional to the way the background
 visits locations, and will have a total number of haunts distributed like the
 background population's unique appearances is distributed.
 - when visiting a location, group members will tend to appear at times like
 the background population appears, and tend to stay for similar durations.

Ways in which this population remains unrealistic:

 - the hotspot locations are of course locations in real geographical space,
 with real business identities.  This suggests some possibilities for assortment
 which are unaccounted for in our synthetic population: people will tend to frequent
 places that in their preferred part of town, and people will tend to have a preferred
 location for a particular category (e.g., a preferred coffeeshop, library, bar)
 to the exclusion of other entries in that category.
 - we have ignored some dependencies in the data - e.g., that certain locations
 have visitors only in the evening or only during the day.  This defect could
 potentially be resolved by having the simulation "see" the observed data as it
 proceeds and draw accordingly, or by having more detail in the locations.

Finally, we model covert aspect of the group by having an external process which
directs members to meet at pre-determined locations, at certain intervals.  This
behavior must be integrated into the "normal" activity of the group, so these
locations become part of the members regular haunts and this background rate of
activity offsets their regular visits.  This requires us to speculate about the actual
nature of the group, and we must couch performance of the assessed analysis in
terms of our imagined group structure.

For the results we report, we imagine a rather unsophisticated group:

 - there is neither recruitment nor departure in the group.


##Results

##Conclusions

##Appendices
