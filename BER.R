rm(list=ls())
setwd("/Users/asus/Desktop/SmartphoneData/")
ID1=read.csv(file="/Users/asus/Desktop/SmartphoneData/ID1.txt",header = TRUE, sep=",")


# Question 1 ----
#Une attaque de type « BeginEnd » qui essaye d'identifier les points d'intérêts d'un individu
#en découvrant des "trous" dans les traces de mobilité et en considérant le point d'arrivée 
#et de départ avant ce trou comme des points d'intérêts possibles. Voir les heuristiques du 
#cours GeoPrivacy pour plus de détails. 

ID1$duree=ID1$time
ID1$duree[1]=0.
for (i in 2:length(ID1$duree)){
  ID1$duree[i]=ID1$time[i]-ID1$time[i-1]
}

R=6370000;
  
  
ID1$distance=ID1$latitude
ID1$distance[1]=0.
for (i in 2:length(ID1$distance)){
  ID1$distance[i]=R*acos(cos(ID1$latitude[i]*pi/180)*cos(ID1$latitude[i-1]*pi/180)*cos((ID1$longitude[i-1]*pi/180)-(ID1$longitude[i]*pi/180))+sin(ID1$latitude[i]*pi/180)*sin(ID1$latitude[i-1]*pi/180))
}
View(ID1)

hist(ID1$duree)
summary(ID1)


# eliminer les durees negatives
# ID1$duree[ID1$duree<=0]

# A1 correspond a ID1 pour des distances et durees courtes

A1=ID1[(ID1$distance<100) & (ID1$duree<100) ,]
View(A1)
dim(A1)

plot(A1$longitude,A1$latitude)

# On distingue deux clusters vraiments eloignees :

A11=A1[(A1$longitude<2),]
A12=A1[(A1$longitude>2) & (A1$latitude>48),]

plot(A11$longitude,A11$latitude)
kmA11.res=kmeans(cbind(A11$longitude,A11$latitude),centers = 2)
plot(A11$longitude,A11$latitude, col = kmA11.res$cluster)

plot(A12$longitude,A12$latitude)
kmA12.res=kmeans(cbind(A12$longitude,A12$latitude),centers = 3)
plot(A12$longitude,A12$latitude, col = kmA12.res$cluster)

kmA12.res$centers

help("write.table")

# permet de mettre le visualiser le fichier sur le site :
write.table(A11, file="hot_spotA11.txt", sep=",", row.names=FALSE)

write.table(A12, file="hot_spotA12.txt", sep=",", row.names=FALSE)


help(hclust)
  