# Setting up a development workspace  
1. Clone the project  
2. Import the gradle project into your favourite IDE  
   __Note__: Please use the gradle version configured in the wrapper task  
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

# Release workflow

## Steps for a new release
 1. Merge request from ``bleeding-<major>.X.X`` to ``release-<major>.X.X``
 2. CI checks __must__ be green (including tests)
 3. Plugin-tests __must__ be successful (manual running required - TS3 is not mocked!)
 4. (Pre-Releases can now be tagged onto the bleeding branch)
 5. Actual merge onto the release branch.

## Notes
* CI publishes artifacts to Nexus, manual publishing is not allowed.
* Snapshot artifacts are published to ``jeakbot-snapshots``.
* (Pre-)Release artifacts are published to ``jeakbot-releases``.
* (Pre-)Releases may not be re-published! (Enforced by Nexus)
* Snapshot artifacts are automatically deleted when unused for longer periods. (Auto-clean by Nexus)
* (Pre-)Releases may only be deleted (de-published) for security concerns.