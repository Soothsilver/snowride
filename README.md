# Snowride
A faster RIDE-like IDE for Robot Framework test suites 

[![Build Status](https://travis-ci.com/Soothsilver/snowride.svg?branch=master)](https://travis-ci.com/Soothsilver/snowride)

[Download version 1.8.](https://bintray.com/snowride/snowride/download_file?file_path=cz%2Fhudecekpetr%2Fsnowride%2Fsnowride%2F1.8%2Fsnowride-1.8-jar-with-dependencies.jar)

Snowride is inspired by [RIDE](https://github.com/robotframework/RIDE) and copies many elements of its user interface but it adds features RIDE doesn't have and is faster.

**Screenshot.** 
![Screenshot](screenshots/Release1.PNG)

**Download.**
As a prerequisite, you must have Java 8 installed. Snowride doesn't work with any other version of Java. Use the "Download" link at the top of this readme file to download the executable JAR file, then run it.

**Design principles of *Snowride*:**
* **Responsive.** Every operation should happen immediately. Snowride should load within a second. 
A test suite that contains thousands of tests should load within a second. Clicking any button or pressing any
key should have a result in the very next monitor frame. Snowride should never appear "frozen" or need to show
progress bars because an operation takes too long.
* **Bug-free.** There should be no bugs in the software, it should be absolutely dependable. Fixing bugs should have priority over adding new features.
* **Efficient.** Stuff that you need to do often and repeatedly should be doable as quickly as possible, via keyboard
shortcuts, smart autocompletion, inspections, quick fixes, or good navigation.
* **Beautiful.** You should want to spend time in Snowride just because you will like looking at it.

**Advantages over other Robot Framework IDEs:** 
* Very fast 
* Doesn't freeze up
* Automated repeated testing
* Search Anything-style autocompletion
* Fast test runner
* Skeuomorphically pretty ^^
* Single file executable
* Quality-of-life efficiency features

**Test runner screenshot:**
![Screenshot 2](screenshots/Release2.PNG)

# Contributing
Submit an issue or a pull request or request contributor access to the repository.

I'll be happy to have your contribution.
