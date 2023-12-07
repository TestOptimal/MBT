# TestOptimal / MBT
[TestOptimal](https://testoptimal.com) Model-Based Testing (MBT) - intelligent test design and test automation

Model application-under-test (AUT) with state diagram or BPM, let TestOptimal generate test cases from the model with your choice of sequencer to achieve desired test coverage.

To perform test automation, write test script ([groovy](https://groovy-lang.org/testing.html)) for transitions (and/or states) to drive AUT as TestOptimal executes the test cases for you.

## Tell me more

[This short video](https://www.youtube.com/watch?v=n6tZTlgHULc) gives a brief overview of TestOptima in action, or check out [list of tutorials](https://testoptimal.com/v6/wiki/doku.php?id=tutorial:tutorial_lst).

More information available at [wiki pages](https://testoptimal.com/wiki).

There are a few DEMO models for you to play with it out of the box.  


## Getting Started

To get started, first [download/install TestOptimal](https://testoptimal.com/#Download) (JDK 17 required) and start up TestOptimal server to run in the background. Or simply run it as a docker container:

    docker run -p8888:8888 testoptimal/mbt 
    
Then point your browser at http://localhost:8888 to bring up TestOptimal IDE.






