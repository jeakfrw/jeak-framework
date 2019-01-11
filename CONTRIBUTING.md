# Setting up a development workspace  
1. Clone the project  
2. Import the gradle project into your favourite IDE  
  * Note: Please use the gradle version configured in the wrapper task  
3. Done  

# Coding guidelines  
* Hold onto the basic oop principles  
* Submit PRs cleanly and easy to read  
  * Indicate when PRs are still WIP (submitting them in this state is okay though.)  
  * Use topic branches  
  * Split larger changes into multiple PRs (using a "main" topic branch)  
* Use documentation / contracting methods  
  * Use interfaces to provide contracts to other developers  
  * Interfaces and public methods require documentation  
    * Usage of ``{@inheritDoc}`` is encouraged  
    * Getters and Setters do not require documentation outside of interfaces  
* Remove ``@author`` tags (Contributors are tracked by GitLab)  

# Bug reports / feature requests / etc.  
* Create a new issue in the ``jeakbot-framework`` project on [GitLab](https://gitlab.com/fearnixxgaming/jeakbot/jeakbot-framework)  
* Bug reports:  
  * Describe what is happening in detail  
  * Describe what you expected to happen instead  
  * Include information on your setup  
    * Framework version  
    * Installed/Enabled plugins (+ their version)  
    * Run environment  
      * Operating system and version  
      * Java edition and version  
* Feature requests
  * Describe what you would like to have included
  * Describe why the change/addition would help you and other developers
  * Evaluate pros and cons before the issue submission
* Questions
  * For questions, please use the Discord server (issues will be closed)

  