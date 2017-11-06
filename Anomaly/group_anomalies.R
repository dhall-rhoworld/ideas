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

numericData = data[,c(24, 11, 12, 13, 14, 15, 20, 23)]

numericData <- na.omit(numericData)

aggs <- aggregate(numericData, by=list(numericData$Site), FUN=mean)
aggs2 <- aggregate(numericData, by=list(numericData$Site), FUN=sd)

aggs <- aggs[,-1]

m <- as.matrix(scale(aggs))

heatmap(m, Colv=T, Rowv=T, scale="none")

distances <- dist(m)

