# Hot-Potato
This is the companion application to the [How Gilt Avoids Building a Distributed Monolith]() presentation originally held on June 21, 2016 at a NYC Scala University Meetup.  Join the NYC Scala University group [here](http://www.meetup.com/New-York-Scala-University)!

Learn more about Apidoc [here](http://apidoc.me/doc/).

# Scala Player
The scala implementation of a Hot-Potato player.  A startup script has been provided for easy instantiation of new players.  Your system must have a zookeeper server running on `localhost:2181` and `activator` in your PATH variable.

For instance, to start a 5 player game:

```
> cd scala-player
> ./bin/start.sh 5
> # Once Play applications have started
> curl -X POST -d @seed.json localhost:9000/potatoes --header "Content-Type:application/json"
```

# Other Players
Haven't made them yet :D