## analysis and plots of montreal data

require(data.table); require(ggplot2); require(grid); require(reshape2)

src.dt <- fread("~/Dropbox/montreal/merged.o", header = F, sep=" ")
setnames(src.dt, c("user_id","location_id","login","logout"))
setkey(src.dt, login, logout, user_id, location_id)

max_real_duration <- 60*60*24 # at most, continuous login of a day

duration.censored.dt <- src.dt[(logout - login) <= max_real_duration,]

user.location.count.dt <- duration.censored.dt[,
	list(location_count = length(unique(location_id))),
	by="user_id"
]
valid.users <- user.location.count.dt[location_count > 1,user_id]

user_and_duration.censored.dt <- duration.censored.dt[user_id %in% valid.users,]

locs.dt <- user_and_duration.censored.dt[,
	list(
		unique_users = length(unique(user_id)),
		total_login_time = sum(logout - login)),
	by="location_id"
] # unique users per location

min_total_login <- 60*60 # 1 hour

invalid.locs <- locs.dt[(unique_users == 1) | (total_login_time < min_total_login), location_id] # locations with only one user, or total log in time < 1 hour

loc_user_duration.censored.dt <- user_and_duration.censored.dt[!(location_id %in% invalid.locs),]
loc_user_duration.censored.dt[,user_id := .GRP, by=user_id]
loc_user_duration.censored.dt[,location_id := .GRP, by=location_id]


ggplot(loc_user_duration.censored.dt[,list(first=min(login), last=max(logout)),by=user_id]) + theme_bw() +
  aes(ymin=user_id+1-0.5, ymax=user_id+1+0.5, xmin = first, xmax=last) + geom_rect()

loc_view.dt <- loc_user_duration.censored.dt[,
  list(
    unique_users = length(unique(user_id)),
    total_login_time = sum(logout - login),
    mean_login = mean(logout - login),
    sd_login = sd(logout - login)
  ),
  by="location_id"]

user_view.dt <- loc_user_duration.censored.dt[,
	list(
		unique_locs = length(unique(location_id)),
		total_login_time = sum(logout - login),
		mean_login = mean(logout - login),
		sd_login = sd(logout - login)
	),
	by="user_id"
]

break_categorization <- function(dt, target, output) {
	bks <- hist(dt[[target]], plot = F)$breaks
	lbls <- paste(head(bks, -1), tail(bks, -1), sep="-")
	dt[[output]] <- sapply(dt[[target]], function(n) factor(lbls[which.max(n <= bks)-1], levels=lbls))
	dt
}

#loc_count_breaks <- hist(user_view.dt$unique_locs, plot = F)$breaks

#loc_count_cats <- paste(head(loc_count_breaks, -1), tail(loc_count_breaks, -1), sep="-")

#user_view.dt[,loc_count_cat := sapply(unique_locs, function(n) which.max(n <= loc_count_breaks)-1)]
#user_view.dt$loc_count_cat <- factor(loc_count_cats[user_view.dt$loc_count_cat], levels=loc_count_cats)

user_view.dt <- break_categorization(user_view.dt, "unique_locs", "loc_count_cat")

# sum(loc_user_duration.censored.dt[,length(unique(location_id)),by="user_id"]$V1 == 1) == 0 # => no more user ids to censor

total_login_duration <- sum(as.numeric(loc_view.dt$total_login_time))
loc_view.dt[,proportion := total_login_time/total_login_duration]

nets.dt <- loc_user_duration.censored.dt[, list(first=min(login),last=max(logout)), by=c("user_id","location_id")]

melt_nets.dt <- melt(nets.dt, id.vars = c("user_id", "location_id"))
melt_nets.dt$inc <- ifelse(melt_nets.dt$variable == "first", 1, -1)
setkey(melt_nets.dt, value, user_id, location_id)

melt_nets.dt[,net_users := cumsum(inc), by="location_id"]
melt_nets.dt[,net_locs  := cumsum(inc), by="user_id"]

acc_loc_time <- melt_nets.dt[,
	list(ave_concurrent_locs = 
	  sum(diff(value)*head(net_locs,-1)) /
	  sum(diff(value)*(head(net_locs,-1)>0)),
	  unique_locs = sum(abs(inc))/2
	), # exclude time intervals w/ no presence
	by="user_id"]
acc_loc_time <- break_categorization(acc_loc_time, "ave_concurrent_locs","con_locs_cat")
acc_loc_time <- break_categorization(acc_loc_time, "unique_locs","unique_locs_cat")

ggplot(acc_loc_time[ave_concurrent_locs >= 2,]) + theme_bw() +
	aes(x=ave_concurrent_locs, fill=unique_locs_cat) + geom_bar() +
	scale_x_log10() #+ scale_y_log10()
#sample(loc_view.dt$location_id, 5, F, loc_view.dt$proportion)

p.loc <- ggplot(loc_view.dt) + theme_bw()
p.user <- ggplot(user_view.dt) + theme_bw()

p.user + theme(panel.margin = unit(0.5, "lines")) +
	aes(fill=loc_count_cat, x=total_login_time/60) +
	facet_grid(loc_count_cat ~ ., scales="free_y", shrink=T, drop=F) +
	geom_bar() + scale_x_log10(name="total login time per individual, in minutes") +
	ylab("number of individuals") +
	scale_fill_discrete(name="locations per individual", drop=F) +
	scale_y_continuous(breaks=function(lims) c(head(lims,1), tail(lims, 1)), expand=c(0,0))

p + aes(x=(logout - login)) + geom_bar() + geom_vline(x=150, color="red") + scale_x_log10()

#####
p + aes(x=unique_users) + geom_bar() + scale_x_log10()
p + aes(x=total_login_time) + geom_bar() + scale_x_log10()

src.dt[,length(unique(user_id)),by="location_id"] # unique users per location
src.dt[,length(unique(location_id)),by="user_id"] # unique locations by user

regular_users <- src.dt[,list(regular = length(location_id) > 1), by="user_id"] # users w/ more than one login
regular_users <- regular_users[regular == T, user_id, keyby="user_id"]$user_id