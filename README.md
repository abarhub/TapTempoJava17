# TapTempoJava17

A command line ```TapTempo``` in Java 17.

## Compilation and execution

To compile, you need Maven 3.8.x and Java 17.

To build :
```shell
maven clean install
```

To run :
```shell
$ java -jar taptempojava-1.0.0.jar
Hit enter key for each beat (q to quit).

[Hit enter key one more time to start bpm computation...]

Tempo: 153 bpm

Tempo: 198 bpm

Tempo: 228 bpm

Tempo: 248 bpm

Tempo: 266 bpm

Tempo: 335 bpm

Tempo: 350 bpm

Tempo: 357 bpm

Tempo: 372 bpm

q
Bye Bye!
```

There are two languages : english and french. To force a language, use parameter `-Duser.langage` :
```shell
$ java -Duser.language=fr -jar taptempojava-1.0.0.jar
```
Valid langage are `fr` for french and `en` for english.


help :
```shell
$ java -jar taptempojava-1.0.0.jar
usage: TempoTap
 -h,--help                Display this help message.
 -p,--precision <arg>     Set the decimal precision of the tempo display.
                          Default is 0 digits, max is 5 digits.
 -r,--reset-time <arg>    Set the time in second to reset the computation.
                          Default is 5 seconds.
 -s,--sample-size <arg>   Set the number of samples needed to compute the
                          tempo. Default is 5 samples.
 -v,--version             Display the version.
```

Work on Windows, Linux and MacOS.

## Inspired by :
* [TapTempo in C](https://linuxfr.org/users/mzf/journaux/un-tap-tempo-en-ligne-de-commande)
* [List of TapTempo program](https://linuxfr.org/tags/taptempo/public)


