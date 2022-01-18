# hsm-stateless

Hierarchical State Machine (HSM) whose representation is called [Statecharts](https://statecharts.dev) is a precious help for designing and implementing complex systems.

This repository offer an implementation in Java based on the design of the book ["Practical Statecharts in C/C++"](https://www.state-machine.com/psicc) available for download as [pdf](https://www.state-machine.com/doc/PSiCC.pdf).
This implementation is respectful notably to the design called by the author of "Implementing Behavioral Inheritance" (see also the Object-Oriented Analogy page 16), and tested against the "Test harness for QHsmTst statechart" page 100 in the project unit tests which gives also a good example of usage.

The purpose is not to get a compliant and powerful implementation of any standard (not UML nor W3C) but to offer a practical tool for the Java developer.

This implementation is in a way `stateless` as all side effects are deported to a custom class outside of the library by using a generic C class for Context.

Feel free to comment or contribute.



