## filter / censor the source data

require(data.table);

## load raw data
src.dt <- data.table(read.csv("~/Dropbox/montreal/merged.o", header = F, sep=" "))
setnames(src.dt, c("user_id","location_id","login","logout"))

# at most, continuous login of a day
max_real_duration <- 60*60*24 
duration.censored.dt <- src.dt[(logout - login) <= max_real_duration,]

user.location.count.dt <- duration.censored.dt[,
	list(location_count = length(unique(location_id))),
	by="user_id"
]
valid.users <- user.location.count.dt[location_count > 1,user_id]

src.dt <- duration.censored.dt[user_id %in% valid.users,]

save(src.dt, file="~/Dropbox/montreal/filtered.RData")