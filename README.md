# JeakBot TeamSpeak 3 Plugin Framework  

## TS3 Servers on the next level!

The JeakBot-Framework connects to the TeamSpeak server using the _TS3 server query interface_.  
Java plugins can use the API to interact with the TeamSpeak server using the JeakBot-API.  
Plugins can be programmed in a way that developers may be familiar from [the Sponge plugin API for Minecraft](https://spongepowered.org) as the projects idea is inspired by Sponge.  

## Badges
|Type/Name|Badge|URL|
|---|---|---|
|License            |![ShieldsIO](https://img.shields.io/github/license/jeakfrw/core-framework.svg?color=success&style=flat-square)[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fjeakfrw%2Fjeak-framework.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fjeakfrw%2Fjeak-framework?ref=badge_shield)
|[bleeding-1.X.X/LICENSE](https://github.com/jeakfrw/core-framework/blob/bleeding-1.X.X/LICENSE)
|Language           |![ShieldsIO](https://img.shields.io/github/languages/top/jeakfrw/core-framework.svg?style=flat-square)|
|Latest (stable)    |![ShieldsIO](https://img.shields.io/github/tag/jeakfrw/core-framework.svg?color=success&style=flat-square)  |[/releases](https://github.com/jeakfrw/core-framework/releases)
|Latest (bleeding)  |![ShieldsIO](https://img.shields.io/github/tag-pre/jeakfrw/core-framework.svg?color=yellow&style=flat-square) |[/releases](https://github.com/jeakfrw/core-framework/releases)
|CI (1.X.X-stable)  |[![buddy pipeline](https://app.buddy.works/m-lessmann/core-framework/pipelines/pipeline/192846/badge.svg?token=22548d502f11240ea437ccc14a4348c352915b0cf82518920be9d2c98bdcb9dd "buddy pipeline")](https://app.buddy.works/m-lessmann/core-framework/pipelines/pipeline/192846) |[View on Buddy](https://app.buddy.works/m-lessmann/core-framework/pipelines)|
|CI (1.X.X-bleeding)|[![buddy pipeline](https://app.buddy.works/m-lessmann/core-framework/pipelines/pipeline/192314/badge.svg?token=22548d502f11240ea437ccc14a4348c352915b0cf82518920be9d2c98bdcb9dd "buddy pipeline")](https://app.buddy.works/m-lessmann/core-framework/pipelines/pipeline/192314) |[View on Buddy](https://app.buddy.works/m-lessmann/core-framework/pipelines)
|Automated Code Rev |[![CodeFactor](https://www.codefactor.io/repository/github/jeakfrw/core-framework/badge)](https://www.codefactor.io/repository/github/jeakfrw/core-framework)|[View on CodeFactor](https://www.codefactor.io/repository/github/jeakfrw/core-framework)|
|Security (Snyk)    |[![Known Vulnerabilities](https://snyk.io/test/github/jeakfrw/core-framework/badge.svg)](https://snyk.io/test/github/jeakfrw/core-framework) [1,2]
|Security (WhiteSrc)|See "Security" tab|[/network/alerts](https://github.com/jeakfrw/core-framework/network/alerts)
|OS-Support         |![ShieldsIO](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20MacOS-informational.svg?style=flat-square)    |
|Status             |![ShieldsIO](https://img.shields.io/maintenance/yes/2019.svg?style=flat-square)
|Discord            |![ShieldsIO](https://img.shields.io/discord/533021399560880141.svg?style=flat-square)|[Discord Invite](https://discord.gg/DPYR5aB)|  

> [1]: [CVE-2018-10237](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2018-10237) has been ignored as we do not use Guava for data deserialization. The vulnerability is introduced by org.reflections which we use for classpath scanning. (The attack vector is over the network where Reflections and thus Guava is not used.) __The reflections update for this CVE seems to be incompatible at the moment and needs to be investigated at some point.__   
> [2]: [CVE-2019-14379](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2019-14379) has been ignored as the vulnerability only applies to environments where ``ehcache`` is installed which is not the case for the default supported Jeak build and distribution.  
_Note: Although we ignore vulnerabilities that do not apply to our distribution environment, we will apply patches when possible just to address concerns and deviated environments._  

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
  
---
# _Special thanks! / Credit_
We would like to thank the companies and projects that help us develop, maintain, document, deploy and distribute the project.  
They are really making work easier for us! :)  
(Our distribution archives also include information of the libraries in use.)
  
### CI / CD: Buddy - _Build better apps faster_.
Buddy allows us to use their Open Source project plan for both the framework and the [Confort](https://github.com/MarkL4YG/confort) library.  
Their unique way of CI/CD configuration eliminates the need of unclear and verbose config files and we love the editor-approach.  
See [https://buddy.works/](https://buddy.works/) for more!  
<a href="https://buddy.works"><img src="https://assets.fearnixx.de/3rdparty/buddy.works/logo-blue.svg" height="100" width="200" alt="Buddy.Works logo."></img></a>
  
### Docs: ReadMe.IO
ReadMe.IO granted us their OSS project plan to enable us using nearly every feature of their documentation platform for free!  
If you love documentation as much as we do, take a look over at: [https://readme.io/](https://readme.io/)  
<a href="https://readme.io"><img src="https://readme.com/static/brandkit/readme-blue.svg" height="50" width="200" alt="ReadMeIO Logo"></img></a>

### Nexus: Sonatype
With Nexus 3 OSS, Sonatype allows us to easily maintain repositories of different types for FearNixx and Jeak which greatly improves our ability to develop and organize software.  
See [https://www.sonatype.com/nexus-repository-oss](https://www.sonatype.com/nexus-repository-oss) for more information.
  
### Repositories: GitHub & GitLab
We're sure most of you know both platforms but we wanted to explicitly state our thanks to both platforms for allowing us to collaborate, organize and plan more than just software development! :)  
While the framework is located on GitHub, plugins by FearNixx are hosted over at GitLab.  
  
### IDE: JetBrains
We use and love IntelliJ IDEA for development.  
We want to thank JetBrains for making the community version free to use and allowing some of us to use educational licenses for our free projects! :)
  
### Code collaboration / discussion
We use CodeStream to collaborate on and discuss code, concepts and bugs.  
CodeStream kindly allowed us to use their free open source plan so anyone can join and discuss with us! :)  
<a href="https://codestream.com"><img src="https://assets.fearnixx.de/3rdparty/codestream/codestream-light.svg" height="50" width="200" alt="CodeStream Logo"></img></a>  
(_We currently only have the light-version of their logo - but dark theme is cooler anyways :sunglasses:_ )
  
### Git: GitKraken
Some of us use GitKraken to interact with and manage Git.  
GitKraken allows free use for open source projects and we thank them for that. :)  

### Server administration: Pterodactyl  
On our servers, we utilize the free application server management software [Pterodactyl](https://pterodactyl.io).  
Managing our test systems and environments would be way harder without it!

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fjeakfrw%2Fjeak-framework.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fjeakfrw%2Fjeak-framework?ref=badge_large)