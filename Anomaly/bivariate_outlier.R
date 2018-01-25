library(car)
library(caret)
library(spatstat)

#
# Values use in testing
#
infile <- "C:/RhoVer/Working/test-in.csv"
outfile <- "C:/RhoVer/Working/test-out.csv"
paramfile <- "C:/RhoVer/Working/test-param.csv"
firstDataCol <- 4
sdResidual <- 2
numNearestNeighbors <- 5

# Configure input data formats.  All columns to the left
# of the first one being checked will be considered ID fields
# and treated as CHARACTER.  Columns being checked will be
# treated as NUMERIC.
classes <- character()
for (i in 1:(firstDataCol - 1)) {
  classes <- c(classes, "character")
}
classes <- c(classes, "numeric")
classes <- c(classes, "numeric")

# Read in input
df <- read.csv(infile, colClasses = classes)

# Fit a linear model
X <- df[, firstDataCol]
Y <- df[, firstDataCol + 1]
fitY <- lm(Y ~ X)

# Test if data are heteroschedastic
areHet <- ncvTest(fitY)$p <= .05

# Compute residuals
if (areHet) {
  fitBct <- BoxCoxTrans(Y)
  Y2 <- predict(fitBct, Y)
  fitY2 = lm(Y2 ~ X)
  res <- abs(residuals(fitY2))
} else {
  res <- abs(residuals(fitY))
}

# Compute residual cutoff value
cutoffRes <- mean(res) + sdResidual * sd(res)

# Make initual outlier calls based on residual cutoff
outlierIndex <- res > cutoffRes

# Compute mean distances to K nearest neighbors
distMatrix <- nndist(X, Y, k=1:numNearestNeighbors)
