require(data.table); require(ggplot2)

plotter <- function(target) {
  dt <- fread(target)
  setnames(dt, c("user_id","location_id","login","logout","reason"))
  ggplot(dt) + aes(ymin=floor(login/24/3600), ymax=ceiling(logout/24/3600), alpha=(logout-login)/3600, x=paste("S",user_id), color=reason) +
    theme_bw() + geom_linerange(size=10) + coord_flip() + xlab("COVERT GROUP MEMBERS") + ylab("day") +
    scale_color_manual(values = c(background="black",covert="red"))
}

plotter("../output/mid-med-middle-6/covert-set-1.csv")
plotter("../output/mid-med-middle-6/covert-set-2.csv")
plotter("../output/mid-med-middle-6/covert-set-3.csv")
