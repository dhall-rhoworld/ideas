library(sas7bdat)
library(corrplot)
library(car)
library(DMwR)
library(spatstat)
library(caret)
library(lmtest)

# Read in vital signs data
path = "S:/RhoFED/ICAC2/PROSE/Statistics/Data/Complete/vsgp.sas7bdat"
#path = "S:/RhoFED/ICAC2/PROSE/Statistics/Data/Complete/ueh.sas7bdat"
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
#m <- findCorrelatedVariables(data[,c(1, 13, 14)])
m <- findCorrelatedVariables(data)

outliers <- findBivariateOutliers(data, 31, 23)

cor(data[,12], data[,11], use="pairwise.complete.obs")

m <- data.frame(5,6)

m <- data.frame()
nrow(m)

is.numeric(data[,9])
sapply(data, is.numeric)

source("outlier_scout.R")
isTrulyNumeric(data, 13)

unique(data[,11])

colnames(data)[9]
grepl("date|month|year", colnames(data)[9], ignore.case = TRUE)

grepl("date", "date")

length(unique(na.omit(data[,11])))

# Create dataframe with numeric variables only
cols.numeric = which(sapply(data, is.numeric))
data.numeric = data[, cols.numeric]

# Generate correlation matrix
matrix.cor = cor(data.numeric, use="pairwise.complete.obs")
matrix.cor[is.na(matrix.cor)] = 0

# View correlations
corrplot(matrix.cor, method="circle", type="lower")

# Find all pairs of associated variables
cutoff.assoc = 0.5
num.vars.assoc = sum(abs(matrix.cor) >= cutoff.assoc) / 2 - nrow(matrix.cor)
assoc.var.1 = vector(length = num.vars.assoc)
assoc.var.2 = vector(length = num.vars.assoc)
p = 0
for (i in 2:nrow(matrix.cor)) {
  for (j in 1:(i-1)) {
    if (abs(matrix.cor[i,j]) >= cutoff.assoc) {
      p = p + 1
      assoc.var.1[p] = i
      assoc.var.2[p] = j
    }
  }
}

# Process each pair of correlated variables
for (i in 1:num.vars.assoc) {
  colname.1 = colnames(data.numeric)[assoc.var.1[i]]
  colname.2 = colnames(data.numeric)[assoc.var.2[i]]
  message(sprintf("Processing variables %s and %s", colname.1, colname.2))
  
  # Generate new data frame
  df = data.numeric[c(assoc.var.1[i], assoc.var.2[i])]
  colnames(df) = c("X", "Y")
  
  # Remove rows with NaN
  df = na.omit(df)
  
  message("Num data points: ", length(df))
  print(df)
  
  tryCatch({
    
    # Create linear model of Y variable
    model.y = lm(Y ~ X, data = df)
    
    # Test if data are hetereoschadistic
    is.het = ncvTest(model.y)$p <= .05
    
  }, warning = function(w) {
    print(w)
  }, error = function(e) {
    message("There is an error: ", e)
    message("Moving to next variable pair")
    next
  })
  
  # Generate initial outlier index
  if (is.het) {
    message("   Data are hetereoschadistic.  Performing BoxCox transformation.")
    model.bct = BoxCoxTrans(df$Y)
    df["Y2"] = predict(model.bct, df$Y)
    model.y2 = lm(Y2 ~ X, data = df)
    res = abs(residuals(model.y2))
  }
  else {
    res = abs(residuals(model.y))
  }
  cutoff.res = mean(res) + 2 * sd(res)
  if (is.het) {
    cutoff.alt = (max(df$Y2) - min(df$Y2)) * 0.1
  }
  else {
    cutoff.alt = (max(df$Y) - min(df$Y)) * 0.1
  }
  message(sprintf("   Cutoff: %s, Cutoff Alt: %s", cutoff.res, cutoff.alt))
  if (cutoff.res < cutoff.alt) {
    message("   Low magnitude of variance.  Using alternate cutoff for first phase of outlier detection.")
    cutoff.res = cutoff.alt
  }
  outlier.index = res >= cutoff.res
  
  # Remove index of putative outliers that are in high-density regions
  matrix.dist = nndist(df$X, df$Y, k=1:5)
  mean.dist = rowMeans(matrix.dist)
  cutoff.dist = mean(mean.dist) + 2 * sd(mean.dist)
  outlier.index = outlier.index & mean.dist >= cutoff.dist
  
  num.outliers = sum(outlier.index)
  message("   Num outliers: ", num.outliers)
  if (num.outliers > 0) {
  
    # Generate inlier and outlier data frames
    inlier.index = !outlier.index
    df.outliers = df[outlier.index,]
    df.inliers = df[inlier.index,]
    
    print(df.outliers)
    
    # Plot inliers and outliers
    plot(df$X, df$Y, col="white", xlab=colname.1, ylab=colname.2)
    points(df.inliers$X, df.inliers$Y, col="green")
    points(df.outliers$X, df.outliers$Y, col="red")
    abline(model.y)
    if (is.het) {
      
    }
    else {
      abline(a = model.y$coefficients[1] + cutoff.res, b = model.y$coefficients[2], lty="dotted")
      abline(a = model.y$coefficients[1] - cutoff.res, b = model.y$coefficients[2], lty="dotted")
    }
    #break;
  }
  
  #break;
}

# Plot BoxCox transformed data
bcmod = BoxCoxTrans(reg.df$Weight_Avg)
y = predict(bcmod, reg.df$Weight_Avg)
boxcox.lm = lm(y ~ reg.df$Height_Avg)
plot(reg.df$Height_Avg, y)
abline(boxcox.lm)

# Construct variables for regression
reg.df = cbind(data[c("RecruitID", "event")], data.numeric[c("Height_Avg", "Weight_Avg")])
reg.df = na.omit(reg.df)
x = reg.df$Height_Avg
bcmod = BoxCoxTrans(reg.df$Weight_Avg)
y = predict(bcmod, reg.df$Weight_Avg)
reg.model = lm(Weight_Avg ~ Height_Avg, data = data.numeric)
reg.model = lm(y ~ x)

reg.model = lm(Weight_2 ~ Weight_1, data = data.numeric)
is.het = ncvTest(reg.model)
bptest(reg.model)

# Compute slope of BoxCox transformed data projected back into non-transformed data space
x1 = min(reg.df$Height_Avg)
x2 = max(reg.df$Height_Avg)
line.ends = data.frame(x = c(x1, x2))
boxcox.x1 = predict(boxcox.lm, line.ends)

# Generate initial outliers list using Cook's distance
#cdist = cooks.distance(reg.model)
#cutoff.cdist = mean(cdist) + 2 * sd(cdist)
#outlier.index = cdist >= cutoff.cdist

# Generate initial outliers list using residuals
resids = abs(residuals(reg.model))
cutoff.resids = mean(resids) + 2 * sd(resids)
outlier.index = resids >= cutoff.resids

# Remove outliers in regions dense with data points
dist.mat = nndist(reg.df$Height_Avg, reg.df$Weight_Avg, k=1:5)
dist.mean = rowMeans(dist.mat)
cutoff.dist = mean(dist.mean) + 2 * sd(dist.mean)
outlier.index = outlier.index & dist.mean >= cutoff.dist
#outlier.index = dist.mean >= cutoff.dist
inlier.index = !outlier.index

outliers.df = reg.df[outlier.index,]
inliers.df = reg.df[inlier.index,]
plot(reg.df$Height_Avg, reg.df$Weight_Avg, col="white")
points(inliers.df$Height_Avg, inliers.df$Weight_Avg, col="green")
points(outliers.df$Height_Avg, outliers.df$Weight_Avg, col="red")
#abline(reg.model)
abline(lm(Weight_Avg ~ Height_Avg, data = num_data))

df1 = data.frame(c(1,2,3,4,5), c(2,4,6,8,10))
colnames(df1) = c("X", "Y")
fit1 = lm(Y ~ X, data = df1)
df2 = data.frame(c(2.5, 4.5))
colnames(df2) = c("X")
predict(fit1, df2)

x <- rnorm(15)
y <- x + rnorm(15)
predict(lm(y ~ x))
new <- data.frame(x = seq(-3, 3, 0.5))
predict(lm(y ~ x), new, se.fit = TRUE)


min.x = min(df$X)
max.x = max(df$X)
df.points <- data.frame(X = c(min.x, max.x))
df.points$Y <- predict(fit.y2, df.points)
df.points$YHi <- df.points$Y + cutoff.res
df.points$YLo <- df.points$Y - cutoff.res
df.points$YHi <- (df.points$YHi * lambda + 1)^(1.0 / lambda)
df.points$YLo <- (df.points$YLo * lambda + 1)^(1.0 / lambda)
print(df.points)
outlierData$slope.hi = (df.points$YHi[2] - df.points$YHi[1]) / (df.points$X[2] - df.points$X[1])
outlierData$slope.lo = (df.points$YLo[2] - df.points$YLo[1]) / (df.points$X[2] - df.points$X[1])
outlierData$intercept.hi = df.points$YHi[1] - outlierData$slope.hi * df.points$X[1]
outlierData$intercept.lo = df.points$YLo[1] - outlierData$slope.lo * df.points$X[1]
message("Slope hi: " ,outlierData$slope.hi)
message("intercept hi: ", outlierData$intercept.hi)
message("Slope lo: " ,outlierData$slope.lo)
message("intercept lo: ", outlierData$intercept.lo)

par(mfrow=c(2, 4))
boxplot(data[,11], main="Systolic")
boxplot(data[,12], main="Diastolic")
boxplot(data[,13], main="Pulse Rate")
boxplot(data[,14], main="Resp Rate")
boxplot(data[,15], main="Temperature")
boxplot(data[,20], main="Avg Height")
boxplot(data[,23], main="Avg Weight")
