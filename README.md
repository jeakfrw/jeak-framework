# JeakBot TeamSpeak 3 Plugin Framework  

## TS3 Servers on the next level!

The JeakBot-Framework connects to the TeamSpeak server using the _TS3 server query interface_.  
Java plugins can use the API to interact with the TeamSpeak server using the JeakBot-API.  
Plugins can be programmed in a way that developers may be familiar from [the Sponge plugin API for Minecraft](https://spongepowered.org) as the projects idea is inspired by Sponge.  

## Badges
|Type/Name|Badge|URL|
|---|---|---|
|License            |![ShieldsIO](https://img.shields.io/github/license/jeakfrw/core-framework.svg?color=success&style=flat-square)|[bleeding-1.X.X/LICENSE](https://github.com/jeakfrw/core-framework/blob/bleeding-1.X.X/LICENSE)
|Language           |![ShieldsIO](https://img.shields.io/github/languages/top/jeakfrw/core-framework.svg?style=flat-square)|
|Latest (stable)    |\<tbd\> |[/releases](https://github.com/jeakfrw/core-framework/releases)
|Latest (bleeding)  |![ShieldsIO](https://img.shields.io/github/tag-pre/jeakfrw/core-framework.svg?color=yellow&style=flat-square) |[/releases](https://github.com/jeakfrw/core-framework/releases)
|CI (1.X.X-stable)  |\<tbd\> |[View on CircleCI](https://circleci.com/gh/jeakfrw/core-framework)
|CI (1.X.X-bleeding)|[![CircleCI](https://circleci.com/gh/jeakfrw/core-framework/tree/bleeding-1.X.X.svg?style=svg)](https://circleci.com/gh/jeakfrw/core-framework/tree/bleeding-1.X.X)   |[View on CircleCI](https://circleci.com/gh/jeakfrw/core-framework)
|Automated Code Rev |[![CodeFactor](https://www.codefactor.io/repository/github/jeakfrw/core-framework/badge)](https://www.codefactor.io/repository/github/jeakfrw/core-framework)|[View on CodeFactor](https://www.codefactor.io/repository/github/jeakfrw/core-framework)
|Security (Snyk)    |![ShieldsIO](https://img.shields.io/snyk/vulnerabilities/github/jeakfrw/core-framework.svg?style=flat-square) [1] |[View on Snyk](https://app.snyk.io/org/markl4yg/project/e353f8ff-d5b9-4599-bfb9-79701ee1c82a)
|Security (WhiteSrc)|See "Security" tab|[/network/alerts](https://github.com/jeakfrw/core-framework/network/alerts)
|OS-Support         |![ShieldsIO](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20MacOS-informational.svg?style=flat-square)    |
|Status             |![ShieldsIO](https://img.shields.io/maintenance/yes/2019.svg?style=flat-square)
|Discord            |![ShieldsIO](https://img.shields.io/discord/533021399560880141.svg?style=flat-square)|[Discord Invite](https://discord.gg/DPYR5aB)|  
[1]: [CVE-2018-10237](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2018-10237) has been ignored as we do not use Guava for data deserialization. The vulnerability is introduced by org.reflections which we use for classpath scanning. (The attack vector is over the network where Reflections and thus Guava is not used.)
  
# Links
* [Project license](./LICENSE)
* [Contribution guidelines](./CONTRIBUTING.md)
* [Changelog](./CHANGELOG)
* [Releases](https://github.com/jeakfrw/core-framework/releases)
* [Documentation](https://jeakbot.readme.io/)
* [Current logo](https://github.com/jeakfrw/core-framework/blob/bleeding-1.X.X/assets/JeakBot-Beta.png)
# Contact
If you want to engage with the developers/community, feel free to join us:

* Discord Server: https://discord.gg/DPYR5aB
* E-Mail contact: support@fearnixx.de

For details about the issue section of the GitLab project. See [CONTRIBUTING.md](./CONTRIBUTING.md)

# Help the project
If you want to help the project without directly contributing on GitHub, you can donate.  
Every donor who donated at least 5 € in the past 12 months will be granted the ``Donor`` role on our Discord.
So make sure to include your Discord name in your donation message :).

[![ko-fi](https://www.ko-fi.com/img/donate_sm.png)](https://ko-fi.com/F1F0OL0V)
