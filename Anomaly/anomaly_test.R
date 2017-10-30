library(sas7bdat)
library(car)
library(DMwR)
library(spatstat)
library(caret)
library(lmtest)
library(RMySQL)
source("outlier_scout.R")

# Read in vital signs data
path = "S:/RhoFED/ICAC2/PROSE/Statistics/Data/Complete/vsgp.sas7bdat"
data = read.sas7bdat(path)

# Replace variable names with labels
attrs = attr(data, "column.info")
num.labels = length(attrs)
labels = vector(length = num.labels)
for (i in 1:num.labels) {
  if (is.null(attrs[[i]]$label)) {
    labels[i] = attrs[[i]]$name
  }
  else {
    labels[i] = attrs[[i]]$label
  }
}
colnames(data) = gsub(" ", "_", labels)

source("outlier_scout.R")
data[findUnivariateOutliers(data, 11), 11]
data[findUnivariateOutliers(data, 12), 12]
data[findUnivariateOutliers(data, 13), 13]
data[findUnivariateOutliers(data, 14), 14]
data[findUnivariateOutliers(data, 15), 15]
data[findUnivariateOutliers(data, 20), 20]
data[findUnivariateOutliers(data, 23), 23]

con <- dbConnect(MySQL(),
                 user="rhover", password="rhover",
                 dbname="rhover", host="localhost")
on.exit(dbDisconnect(con))

rs <- dbSendQuery(con, "select study_name from study;")
dat <- dbFetch(rs)
dat
dbClearResult(rs)

query <- paste(
  "select ds.dataset_id",
  "from dataset ds",
  "join study s on s.study_id = ds.study_id",
  "where s.study_name = 'PROSE'",
  "and ds.dataset_name = 'VSGP'"
)

rs <- dbSendQuery(con, query)
dat <- dbFetch(rs)
dat

query <- paste(
  "insert into dataset (dataset_name, study_id)",
  "values('VSGP', 1)"
)
dbSendQuery(con, query)

file.info(path)

# Find all pairs of correlated numeric variables
findCorrelatedVariables(data, use.var.names = TRUE)

par(mfrow=c(1, 1))
o <- findBivariateOutliers(data, 20, 23, cutoff.residual = 2, cutoff.density = 0)
plotBivariateOutliers(data, 20, 23, o)

o <- findBivariateOutliers(data, 20, 23, cutoff.residual = 3, cutoff.density = 0)
plotBivariateOutliers(data, 20, 23, o)

o <- findBivariateOutliers(data, 20, 23, cutoff.residual = 2, cutoff.density = 8)
plotBivariateOutliers(data, 20, 23, o)