# Mind Traffic Control (Clojure Edition)

Mind Traffic Control in Clojure  

(Ported / adapted from [Mind Traffic Control in Racket](https://github.com/interstar/mtc-racket))


Mind Traffic Control is a very simple but very powerful command line tool to track tasks or "todo" items.

Despite its simplicity MTC is powerful in two ways :

* it can handle a LOT of tasks. (I have around 1300 tasks in my file at the point of writing. And my MTC has been in continuous use for about 10 years.)

* it is very simple to control, typically you can achieve what you want with a minimal number of keystrokes that are also trivial to learn.

MTC is designed to help you remember and do your tasks. Not to faff about with task management software or methodologies. So the philosophy is one of minimalism. It keeps out of your way and requires the minimum from the user to make it work. 

Unlike social media, and a lot of apps today, MTC wants to minimize the time you actually spend using it. MTC is working well for you if you look at it for less than a minute a day, but still manage to track and get your tasks done.

### Basic Usage

MTC thinks of your tasks as a queue. And by default simply shows you the "next item", ie. the item at the front of the queue.

When faced with this task you can basically do one of two things : 

* delete it from MTC, either because you've done it, or decided it no longer needs doing
* defer it to some time in the future, ie. throw it further back in the queue

You'll note that MTC makes no distinction between cancelling a task and doing it. This is a pointless distinction. MTC is ruthlessly focussed on being useful to help you track what you *need to do*. Not what you've done.

To delete this "next item" (ie. the first item in the queue) simply type `*` and hit the return key.

To defer an item you use the `/` key.

`/` and return simply throws the item to the end of the queue. But as your queue becomes longer that might be too far back, you might need to skip something right now, but keep it reasonably close to the front of the queue.

`//` throws the item 10 back in the queue (ie. brings the 10 next items in front of it)

`///` throws the item 50 back in the queue.

`////` throws the item 500 back in the queue.

As mentioned previously, I have around 1300 items in my MTC. I haven't felt the need for a command to throw items 5000 back. But if you find yourself in the position of needing it, then make a request :-) It's literally two extra lines of code to add it.



#### Er ... OK. But how do I actually add items?

Just start typing them in at the command prompt.

By default if you type any phrase over about 6 letters that isn't obviously one of the commands described above, or in the next section, it's assumed to be another todo item, and is added **on the front** of the queue. Note: new items go on the front by default. I used to add them on the end, but on the front turns out to be better. MTC is a todo-queue. But also a "stack" :-)

#### Pattern Based Manipulation of the Queue

There are times when you need to focus on a particular project.

The command `+ PATTERN` pulls all items which match PATTERN to the front of the queue.

I use the standard todo.txt conventions of having a `+` in front of project names, and an `@` in front of contexts.

So `+MTC` is my tag for items related to developing or distributing MTC. `@email` is my tag for any item that involves writing an email. 

MTC has no special commands to handle these tags. And you can use any convention that you like.

If you want to pull tags to the front of the queue, simply write an appropriate pattern and use with the "pull" command `+`.

Sometimes you have a lot of tagged items in a project, but don't want to pull them all (which will push back everything else you have in your queue). So there is also a `pull-one` command using a double plus sign.

`++ PATTERN` pulls only the first match of PATTERN to the front of the queue. 


As well as pulling items that match a pattern to the front of the queue, you can also push them back.

Sequences of the character `-` is used to push items back.

For example :

`-- PATTERN` finds all items matching PATTERN and puts them starting 10 back.

`--- PATTERN` does the same but pushes all matches 50 items back.

`---- PATTERN` pushes 500.

NB : if you have some matches further back in the queue these will actually be pulled forward.

The typical usage of this pushing mechanism is when you are working on a project but know that for some reason you are currently blocked on it. For example, waiting for someone else's input.

You don't want to forget the project, but you know you must put it out of the way for the moment and concentrate on items not related to the project. 

Remember that the point of MTC is not to treat you like an idiot. MTC assumes you know *roughly* what you need to be doing and what's important. It's to allow you to keep the details of those projects in a way which is easy to access when needed (by pulling forwards) and easy to forget about when not needed (by pushing backwards).

#### Editing Items

Items are short. Typically only about 5 to 10 words. So it's rare you need to edit them. Just delete and rewrite.

But occassionally it *is* useful to extend an item with a bit of extra information.

Eg. you want to turn

     Call Steve 
     
into 

     Call Steve on 04324 73873


Do this with the `e` command. Ie. `e on 04324 73873` will append the string to the next item.

#### Showing the Queue

MTC discourages you looking at the queue entirely. But sometimes you need to, so we have the command `l` to list all items.

As with the defer and push commands, list also has variants.

`l` shows the list of all items in the queue

`ll` lists just the first 10 items from the queue

`lll` list the first 50 items

`llll` lists the first 500 items.

This lets you peek ahead. Typically when you want to remind yourself if there are any other projects which might be urgent.

MTC has no explicit notion of priority or urgency. In usage, just keep urgent / high priority items near the beginning of the queue.

As of version 0.2.0 we have now added a "search" or "query" option which is not the same as pulling.

`? PATTERN` is a way to search and show all items that contain the PATTERN **without** pulling them to the front of the queue.


#### IMPORTANT - SAVE YOUR WORK 

MTC works on the queue in memory. You need to explicitly save it back to the todo.txt file.

Use the command `s` to save.

You can quit MTC using the `q` command. When you quit, **MTC does NOT automatically save your list***. However it DOES save it in a file in the same directory with the **tmp_** prefix. If you find you quit by mistake without saving, you can recover the state at which you quit in that file.

#### Other Features

`r NUMBER` - reverses the list of the top NUMBER of items.

Sometimes you just copied five todos off a list you had written elsewhere. It's nice to capture these options in MTC, but they're  *backwards*!!! The list you copied was in order. But because MTC adds each new items to the front of the queue, they're now in reverse order. This is a nuissance. So `r 5` to the rescue. It reverses the top five items of the queue. And now they're in the order you expect.

Finally, the command `c` gives you a count of how many items you have in the queue. Which is something you sometimes want to know.


## Quick Start

Create a todo.txt file ... a simple text file with a list of todo items.

Then run from repository with :

    lein run PATH/TO/todo.txt


Or build a standalone JAR file with 

    lein compile
    lein uberjar
    
and run it with :

    java -jar target/mtc-clj-0.2.0-SNAPSHOT-standalone.jar PATH/TO/todo.txt
    
    
## License

Copyright © 2018 Phil Jones

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
