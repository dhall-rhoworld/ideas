library(sas7bdat)
library(car)
library(DMwR)
library(spatstat)
library(caret)
library(lmtest)
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

# Find all pairs of correlated numeric variables
findCorrelatedVariables(data, use.var.names = TRUE)

par(mfrow=c(1, 1))
o <- findBivariateOutliers(data, 20, 23, cutoff.residual = 2, cutoff.density = 0)
plotBivariateOutliers(data, 20, 23, o)

o <- findBivariateOutliers(data, 20, 23, cutoff.residual = 3, cutoff.density = 0)
plotBivariateOutliers(data, 20, 23, o)

o <- findBivariateOutliers(data, 20, 23, cutoff.residual = 2, cutoff.density = 8)
plotBivariateOutliers(data, 20, 23, o)