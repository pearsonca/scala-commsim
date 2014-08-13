require(data.table)
require(igraph)
args <- commandArgs(trailingOnly = T)
cnt <- args[1]
inter <- args[2]
locs <- args[3]

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

resolve(cnt, inter, locs)