require(data.table); require(ggplot2)

plotter <- function(target) {
  dt <- fread(target)
  setnames(dt, c("user_id","location_id","login","logout"))
  ggplot(dt) + aes(ymin=floor(login/24/3600), ymax=ceiling(logout/24/3600), alpha=(logout-login)/3600, x=paste("S",user_id)) +
    theme_bw() + geom_linerange(size=10) + coord_flip() + xlab("COVERT GROUP MEMBERS") + ylab("day")
}

plotter("../output/mids-size-6/covert-result-1.csv")
plotter("../output/mids-size-6/covert-result-2.csv")
plotter("../output/mids-size-6/covert-result-3.csv")
