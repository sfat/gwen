[![Gwen](https://github.com/gwen-interpreter/gwen/wiki/img/gwen-attractor.png)](https://github.com/gwen-interpreter/gwen/wiki/The-Gwen-Logo)

Gwen
====

Gwen is a [Gherkin](https://github.com/cucumber/cucumber/wiki/Gherkin) interpreter that turns Given-When-Then
steps into automation instructions and executes them for you so you don't have to do all the programming work. It has
an abstracted [evaluation engine](https://github.com/gwen-interpreter/gwen/wiki/Evaluation-Engines) allowing any type of
automation capability to be built and mixed in.
[Meta specifications](https://github.com/gwen-interpreter/gwen/wiki/Meta-Features) (also expressed in Gherkin) are used
to capture automation bindings and allow you to compose step definitions in a declarative manner.

- [Join our user community](#user-community)

### Current Status

[![Build Status](https://travis-ci.org/gwen-interpreter/gwen.svg)](https://travis-ci.org/gwen-interpreter/gwen)

- [Latest release](https://github.com/gwen-interpreter/gwen/releases/latest)
- [Change log](CHANGELOG)

### What's New?

- [Template matching](https://github.com/gwen-interpreter/gwen/wiki/Template-Matching)
- [Gwen Workspaces](https://gweninterpreter.wordpress.com/2017/12/18/gwen-workspaces/)

Why Gwen?
---------

> Gwen = [G]iven [W]hen Th[en]

We wanted to make automation easier for non developers with little to no coding experience. So we developed an
interpreter to read plain text Gherkin features as input and produce executing automation instructions as output
through automation engines that can be developed and shared.

What engines are available?
---------------------------

- [gwen-web](https://github.com/gwen-interpreter/gwen-web)
  - A web automation engine for Gwen that you can download and use to quickly automate web application testing and
    robotic online processing without having to develop any page objects or Selenium code.

Links
-----

- [Blog](https://gweninterpreter.wordpress.com)
- [Wiki](https://github.com/gwen-interpreter/gwen/wiki)
- [FAQ](https://github.com/gwen-interpreter/gwen/wiki/FAQ)

Core Runtime Requirement
------------------------

- Java SE 8 Runtime Environment

Key Features
------------

- [Gherkin grammar](https://cucumber.io/docs/reference)
- [Evaluation engines](https://github.com/gwen-interpreter/gwen/wiki/Evaluation-Engines)
- [Evaluation reports](https://github.com/gwen-interpreter/gwen/wiki/Evaluation-Reports)
- [Command line interface](https://github.com/gwen-interpreter/gwen/wiki/Command-Line-Interface)
- [REPL console](https://github.com/gwen-interpreter/gwen/wiki/REPL-Console)
- [Serial execution](https://github.com/gwen-interpreter/gwen/wiki/Execution-Modes#serial-execution)
- [Parallel execution](https://github.com/gwen-interpreter/gwen/wiki/Execution-Modes#parallel-execution) (with configurable ramp up interval)
- [Dry run execution and validation](https://github.com/gwen-interpreter/gwen/wiki/Execution-Modes#dry-run-validation)
- [Data driven execution](https://github.com/gwen-interpreter/gwen/wiki/Execution-Modes#data-driven-execution)
- [Meta features](https://github.com/gwen-interpreter/gwen/wiki/Meta-Features)
- [Composable step definitions](https://github.com/gwen-interpreter/gwen/wiki/Meta-Features#composable-step-definitions)
- [Runtime settings](https://github.com/gwen-interpreter/gwen/wiki/Runtime-Settings)
- [User settings](https://github.com/gwen-interpreter/gwen/wiki/User-Settings)
- [String interpolation](https://github.com/gwen-interpreter/gwen/wiki/String-Interpolation)
- [SQL data bindings](https://github.com/gwen-interpreter/gwen/wiki/SQL-Data-Bindings)
- [Implicit attributes](https://github.com/gwen-interpreter/gwen/wiki/Implicit-Attributes)
- [Template matching](https://github.com/gwen-interpreter/gwen/wiki/Template-Matching)

User Community
--------------

All announcements and discussions are posted and broadcast to all members in the Gwen mail group. Active users who
[join the group](https://groups.google.com/d/forum/gwen-interpreter) will also receive an invitation to our Gwen Slack
community where they can interact with other users and help each other out.

License
-------

Copyright 2014-2018 Branko Juric, Brady Wood and [Gwen contributors](#code-contributors).

This software is open sourced under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

See also: [LICENSE](LICENSE)

This project has dependencies on other open source projects. All distributed third party dependencies and their
licenses are listed in the [LICENSE-THIRDPARTY](LICENSE-THIRDPARTY) file.

Open sourced 14 May 2014 09:30 pm AEST

Contributions
-------------

New capabilities, improvements, and fixes are all valid candidates for contribution. Submissions can be made using
pull requests. Each submission is reviewed and verified by the project [maintainers](#maintainers) before being
integrated and released to the community. We ask that all code submissions include unit tests.

By submitting contributions, you agree to release your work under the [license](#license) that covers this software.

### How to Contribute

1. Fork this repository
2. Create a branch on your forked repository
3. Commit your changes to your branch
4. Push your branch to your forked repository
5. Create a pull request from your branch to here

Maintainers
-----------

- [Branko Juric](https://github.com/bjuric)
- [Brady Wood](https://github.com/bradywood)

Contributors
------------

We thank the following contributors and active users for helping to make Gwen better. You are all awesome!

### Code Contributors

The following [contributors](https://github.com/gwen-interpreter/gwen/graphs/contributors) submitted pull requests
that have been merged:

- [Jacob Juric](https://github.com/TheReturningVoid)
- [Alexandru Cuciureanu](https://github.com/acuciureanu)

### Active Users

The following users raised issues or requests that have been addressed:

- [Chris Leong](https://github.com/aztheque)
- [Martino Turturiello](https://github.com/martino-jelli)
- [bliddicoat](https://github.com/bliddicoat)
- [Andrew Gillett](https://github.com/asgillett)
- [anshu781126](https://github.com/anshu781126)
- [ketu4u2010](https://github.com/ketu4u2010)
- [Rahul9844](https://github.com/Rahul9844)

Credits
-------
- [Cucumber/Gherkin](https://github.com/cucumber/cucumber/wiki/Gherkin)
- [Scopt](https://github.com/scopt/scopt)
- [JLine](https://github.com/jline/jline2)
- [Bootstrap](https://getbootstrap.com/)
- [JQuery](https://jquery.com/)
- [Reel](https://github.com/pisi/Reel)

---
