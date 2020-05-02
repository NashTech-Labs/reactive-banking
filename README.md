# reactive-banking
1. Run two instances of the application in separate terminals:

```
$ ./runNode1.sh
$ ./runNode2.sh
```

* Notice how the scripts override the Akka remote port.
* Observe the logs. You will know your cluster has successfully formed when the first node (running on port 2551) reports:

```
Node [akka://Loyalty@127.0.0.1:2552] is JOINING, roles [dc-default]
Leader is moving node [akka://Loyalty@127.0.0.1:2552] to [Up]
```

* The second node (running on port 2552) should report:

```
Cluster Node [akka://Loyalty@127.0.0.1:2552] - Welcome from [akka://Loyalty@127.0.0.1:2551]
```

2. At this point, we have enabled Akka Clustering in our application. This creates the foundation for other Akka features such as Akka Cluster Sharding. Try the following experiment:

```
$ ./credibility.sh -p 8000 -a sample credit 100
$ ./credibility.sh -p 8001 -a sample credit 200
$ ./credibility.sh -p 8000 -a sample debit 100
$ ./credibility.sh -p 8001 -a sample debit 200
$ ./credibility.sh -p 8000 -a sample retrieve
$ ./credibility.sh -p 8001 -a sample retrieve
```

* Since everything is working the way it is supposed to, we will have a balance of 0 points. To prove that point let's try some more debit transactions:

```
$ ./credibility.sh -p 8000 -a sample debit 100
$ ./credibility.sh -p 8001 -a sample debit 100
```

* We get consistent results.
