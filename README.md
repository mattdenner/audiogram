# audiogram

## Introduction
My girlfriend was born deaf, classified as "moderate to profound": she can hear low frequencies at a much lower volume than higher frequencies.  For instance, she can hear the rumble
of the car tires on the road but she can't hear the cat bell, even if it's right next to her ear.  One day she pulled out an audiogram, a graph that shows her response to
various frequencies, and I was curious: how could I generate one?  Actually I was more interested in "if I knew someones audiogram then could I generate filters that would
allow me to hear like them?" but that's for another day!

Anyway, armed with [Clojure](http://clojure.org/), the language I'm currently learning, and a small amount of quiet time I set about writing my own audiogram generator.  This code
is the result and the following is a walk through of the code and my thought processes at the time.

## Usage
Make sure you have supercollider installed, a pair of headphones plugged into your computer, and the volume set so that it is not too loud.

**DISCLAIMER**: I am not responsible for your hearing, you are, and this application generates sounds at various frequencies at various volumes.  At the maximum volume it could
damage your hearing and so I'm telling you now: **make sure you have the volume set low on the computer to start with and run through the program once before turning it up**.
On my Macbook Air I have the main volume set at below 50% and originally started working on this code with it nearer 10%.

To get your audiogram run `lein run` and hit `enter` whenever you hear the sound being played.  The volume of the frequency under test will be decreased or increased based on
your response, or lack of it, and the sound will only be played for a few seconds before the application moves on.  The whole process takes about 5 minutes.

## How it works
You can view testing your hearing response to a given frequency as a simple search of the volume range to find the point at which you can just hear it.  The simplest way to do
this is through bisection: play the frequency at 50% volume; if you can hear it then play it at 25%, if you can't 75%.  An audiogram is then simply an application of this
search across all frequencies: map a search function over a sequence of frequencies generating a pair for each of frequency-volume, where volume is the level at which the
frequency becomes just audible.  Those pairs can then be plotted on a graph.

This mapping is what [`audiogram.core/audiogram-for`](tree/master/src/audiogram/core.clj#66) does: given something representing a user being tested, it returns the sequence of
frequency-volume pairs.  It does a bit of partial application, because the user and the volume ranges remain the same regardless of frequency, but it is extremely simple to
read.  It uses the [`audiogram.core/bisect`](tree/master/src/audiogram/core.clj#9) function to search the volume range.

The function performing the search comes from [`audiogram.core/bist-fn-for-frequency`](tree/master/src/audiogram/core.clj#55) which is a higher-order function.  Given a user
and the frequency to test them at, it returns a function that will find the volume at which the frequency is just audible.  The neat thing here is that this function is really
just a simple question: "if a sound is played, did the user hear it or did the sound stop?"  And this can be modelled by having two [core.async](https://github.com/clojure/core.async)
channels, representing the sound and the user, and waiting for the first to have a value on it.  Again, it's extremely simple to read.

The bits that tie this together are then the [`audiogram.core/hearing-user'](tree/master/src/audiogram/core.clj#44), which is a simple core.async process that sends `true` on
a channel whenever the `enter` key is pressed, and [`audiogram.core/tone-player`](tree/master/src/audiogram/core.clj#27), that is a core.async process that plays a tone and
after a time sends `false` on a channel.

I'll admit: I was absolutely amazed at how succinct this code is.  core.async certainly made the biggest difference because it allowed me to view the sound and user as
"external" processes that would simply inform the main code when they did something; essentially they became actors.  [Overtone](http://overtone.github.io/) made generating
sounds simple, and Clojure was made it very functional: "mapping over frequencies" was the key moment for me in understanding this.

## What's next
* Converting the "volume" to decibels for meaningful interpretation;
* Generating the actual audiogram, probably with [Incanter](http://incanter.org/) although it might be overkill;
* Manipulating sounds using this information with Overtone.

## License

Copyright © 2013 Matthew Denner

Distributed under the Eclipse Public License, the same as Clojure.
