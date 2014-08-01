## read in synthetic files, add them source data and analyze

require(data.table)
require(igraph)
args <- commandArgs(trailingOnly = T)
args <- c(10,10,1)
cnt <- args[1]
inter <- args[2]
locs <- args[3]
# src <- "~/scala-commsim/simdata/"
# stopifnot(!is.na(src))
# setwd(src)

# memberCount <- seq(10,20,2)
# meetInterval <- seq(30,10,-10)
# meetLocations <- 1:3

# getfiles <- function(count, intervals, locations) list.files(pattern=paste(count, intervals, locations, ".+csv", sep = "-"))
# procfile<-function(filename, orig, u.offset, ref.dt) {
#   #read in table, convert times
#   res <- data.table(read.csv(filename, header = F, col.names = c("day","user.id","loc.id","login.dt")))
#   res$login = as.numeric(as.POSIXlt(paste(format(orig + res$day*24*3600), res$login.dt)), tz="EST") - as.numeric(orig)
#   res[,logout:=login+30*60]
#   res <- res[,list(user.id=user.id+u.offset, loc.id, login, logout)]
#   setkey(res, login, logout, user.id, loc.id)
#   
#   # identify which times will be trimmed, because empirical data indicates that site not using wifi at that time
#   trimtimes <- ref.dt[loc.id %in% unique(res[,loc.id]),list(first=min(login), last=max(logout)), by="loc.id"]
#   setkey(trimtimes, loc.id, first, last)
#   
#   # join those times to res, then keep only sessions that falls inside them
#   thing <- merge(res, trimtimes, by="loc.id")[login < last & first < logout, list(user.id, loc.id, login, logout)]
#   setkey(thing, login, logout, user.id, loc.id)
#   
#   thing
#   write.table(thing, gsub("\\.csv", ".sim", filename), row.names=F, col.names=F)
# }


# mapply(function(count, intervals, locations, orig, u.offset, ref.dt) {
#     fs<-getfiles(count, intervals, locations)
#     for (f in fs) {
#       procfile(f, orig, u.offset, ref.dt)
#     }
#     cat("done: ", count,":", intervals,":", locations, "\n")
#   },
#   rep(memberCount, each=length(meetInterval)*length(meetLocations)),
#   rep(meetInterval, each=length(meetLocations), times=length(memberCount)),
#   rep(meetLocations, times=length(meetInterval)*length(meetLocations)), MoreArgs=list(orig = neworig, u.offset=max.uid, ref.dt = src.dt))

## process files into user-to-user graphs - .uu ext?

getCCPairs <- function(count, intervals, locations) list.files(path = "./cc_files", pattern=paste(count, intervals, locations, ".+cc", sep = "-"))
getCUPairs <- function(count, intervals, locations) list.files(path = "./cu_files", pattern=paste(count, intervals, locations, ".+cu", sep = "-"))

readELtable <- function(tar) {
  res <- data.table(read.table(tar, header = F, col.names = c("user.a", "user.b", "login", "logout")), key = c("login", "logout", "user.a", "user.b"))
  res[user.b < user.a, `:=`(user.b = user.a, user.a = user.b)]
  res
}

src <- "~/Dropbox/montreal/merged.o"

src.dt <- data.table(read.table(src, head=F,col.names = c("user.id", "loc.id", "login", "logout")), key = c("login","logout","user.id","loc.id"))
#src.dt <- src.dt[delta != 0,]
max.uid <- src.dt[,max(user.id)]
min.login <- src.dt[,min(login)]
oldorig <- trunc(as.POSIXlt("2005-01-01", tz="EST"), "days")
neworig <- trunc(as.POSIXlt(min.login, origin = "2005-01-01", tz = "EST"), "days")
del <- as.numeric(neworig)-as.numeric(as.POSIXlt("2005-01-01"))

base.dt <- readELtable("~/Dropbox/montreal/paired.o")
base.dt[,login := login - del]
base.dt[,logout := logout - del]

slice <- function(el.dt, start.day, end.day) {
  data.table(unique(el.dt[(logout < end.day*24*3600) & (login > start.day*24*3600), list(user.a, user.b)])+1, key = c("user.a", "user.b")) 
}

resolve <- function(count, intervals, locations) {
  cc.pairs<- paste("./cc_files/",getCCPairs(count, intervals, locations),sep="") ## should be one for each sample
  cu.pairs<- paste("./cu_files/",getCUPairs(count, intervals, locations),sep="")
  covert.ids <- (1:count)+max.uid
  samples <- mapply(function(cc.file, cu.file) {
    members.wh <- file(gsub("\\.cc", "-members.csv", cc.file), open = "a")
    sizes.wh <- file(gsub("\\.cc", "-sizes.csv", cc.file), open = "a")
    cc.EL <- readELtable(cc.file)
    cu.EL <- readELtable(cu.file)
    combo.EL <- rbindlist(list(base.dt, cc.EL, cu.EL))
    apply(array(365:(5*365)),1,function(day) {
      gg <- graph(t(slice(combo.EL, day-365, day)), directed=F)
      cs <- fastgreedy.community(gg)
      members <- membership(cs)[covert.ids]
      comm.counts <- sizes(cs)[members]
      write(c(day,members), members.wh, ncolumns = count+1, append = T)
      write(c(day,comm.counts), sizes.wh, ncolumns = count+1, append = T)
      cat(day, "\n")
    })
    flush(members.wh)
    flush(sizes.wh)
    close(members.wh)
    close(sizes.wh)
  }, cc.pairs, cu.pairs)
}

# 1482

resolve(cnt, inter, locs)
# ,
# rep(memberCount, each=length(meetInterval)*length(meetLocations)),
# rep(meetInterval, each=length(meetLocations), times=length(memberCount)),
# rep(meetLocations, times=length(meetInterval)*length(meetLocations)), MoreArgs=list(orig = neworig, u.offset=max.uid, ref.dt = src.dt))


