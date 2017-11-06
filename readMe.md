# T3ServerBot - By FearNixx Gaming

* [Source](https://gitlab.com/fearnixxgaming/t3serverbot)
* [Issues](https://gitlab.com/fearnixxgaming/t3serverbot/issues)
* [Progress](https://gitlab.com/fearnixxgaming/t3serverbot/boards)
* [ServiceDesk](#) (Not yet enabled)

## General Information
A TeamSpeak 3 Server bot framework that allows jar plugins to interact easier with TeamSpeak servers allowing both abstract and direct access to the Query Interface.  
The framework uses Sponge as a role model so some developers may already be familiar with some things.  

The framework is also designed with scalability in mind allowing both wrappers and multiple instances of bots within the same application.

## Starting the bot(s)
Currently there are only linux bash scripts to properly start the bot.  
However you can start the bot on any other java-capable platform provided that you insert the needed libraries into the classpath and start either ``de.fearnixx.t3.Main`` or your own main class.

On the very first start the Main class will construct its default config.  
While any bot will construct a default config for itself on its first start if none exists.  

(Ideally that means you'll need to start the bot twice on the very first start)

## Licensing
The license is currently not yet decided which means the license falls back to "all rights reserved"  
However - You are completely free to do the following:
* Use the binaries non-commercially 
* Use the binaries commercially !for your own community!  
  (Please do not yet sell or rent instances to others...)
* Fork the repository creating your own copies  
  (Be aware that we currently don't allow re-licensing!)
* Contact us if you really wanna do something different. There's always space to negotiate.


## Contributing
This is an Open Source Project with exactly that in mind! If you want to contribute feel absolutely free to do so.  
You can help the development with the following:
* Feedback
* Documentation
* Bug reports
* Code proposals (using PRs or patches in case of very small changes)
* Donating either to FearNixx or directly to me (MarkL4YG)

## Plugin development
Please await the upcoming docs to see how to develop plugins for the bot.

## Bot development
Please await the upcoming docs to see how to develop the framework.

## Contact
* Official contact [Fearnixx Gaming](mailto:support@fearnixx.de)
* MarkL4YG (Head developer)
  * [Twitter](https://twitter.com/MarkL4YG)
  * [Mail](mailto:mark332@fearnixx.de)
* Our TeamSpeak 3 Server: [ts.fearnixx.de](ts3server://ts.fearnixx.de)
