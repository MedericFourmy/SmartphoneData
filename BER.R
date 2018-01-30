ID1=read.csv(file="ID1.txt",header = TRUE, sep=",")


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
boxplot(ID1$duree)

# eliminer les durees negatives
# ID1$duree[ID1$duree<=0]
