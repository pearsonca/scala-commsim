## script calculates and plot various event data
require(data.table)
src <- commandArgs(trailingOnly = T)[1]
src <- "~/Dropbox/epidemics4share/merged.o"
stopifnot(!is.na(src))

src.handle <- read.table(src, head=F,col.names = c("user.id", "loc.id", "login", "logout"))
src.dt <- data.table(src.handle, key = c("login","logout","user.id","loc.id"))
src.dt[,delta:=logout-login]
#src.dt <- src.dt[delta != 0,]

filtered.src.dt <- src.dt[delta != 0 & delta < 12*3600,]
time.dist <- hist(filtered.src.dt[,delta]/60, plot=F)
plot(time.dist$breaks[-1], time.dist$counts, log = "xy")

dist.of.unique.visits <- filtered.src.dt[, list(loc.count = length(unique(loc.id)), visit.count = length(unique(login)), tot.time = sum(delta)), by=user.id]
setkey(dist.of.unique.visits, loc.count, visit.count, tot.time, user.id)

dist.of.unique.users <- filtered.src.dt[, list(user.count = length(unique(user.id)), visit.count = length(unique(login)), tot.time = sum(delta)), by=loc.id]
setkey(dist.of.unique.users, user.count, visit.count, tot.time, loc.id)

#dim(dist.of.unique.visits[list(loc.count=1,visit.count=1)])
#dim(dist.of.unique.visits[!list(loc.count=1,visit.count=1)])
#dim(dist.of.unique.visits)

loc.hist <- hist(dist.of.unique.visits$loc.count, plot=F, breaks = c(0,unique(dist.of.unique.visits$loc.count)))
plot(loc.hist$breaks[-1], loc.hist$counts, log = "xy", xlab="user unique location counts", ylab="number of users with that count")

plot(loc.count ~ visit.count, data = dist.of.unique.visits, log="xy")
abline(0,1, col="red")

hist(dist.of.unique.visits$tot.time/dist.of.unique.visits$visit.count)

u.hist <- hist(dist.of.unique.users$user.count, plot=F, breaks = c(0,unique(dist.of.unique.users$user.count)))
plot(u.hist$breaks[-1], u.hist$counts, log = "xy")
# pop <- dim(dist.of.unique.visits)[1]
# mu <- 1
# lambda <- mu / 5
# exp.draws <- rexp(pop*5, mu+lambda)
# 
# gillispie <- function(x, draw) with(x, {
#   tau <- rexp(1,N*(mu+lambda))
#   
#   
# })