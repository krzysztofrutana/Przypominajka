<!--
*** Thanks for checking out this README Template. If you have a suggestion that would
*** make this better, please fork the repo and create a pull request or simply open
*** an issue with the tag "enhancement".
*** Thanks again! Now go create something AMAZING! :D
***
***
***
*** To avoid retyping too much info. Do a search and replace for the following:
*** github_username, repo_name, twitter_handle, email
-->





<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->

<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/krzysztofrutana/OCR-Desktop">
    <img src="app/src/main/ic_przypominajka-playstore.png" alt="Logo" width="200" height="200">
  </a>

  <h3 align="center">Przypominajka</h3>

  <p align="center">
    My first android application and my first Java program.
The application allows you to add events for a specific day of the month (if the 31st day of the month is specified, 
the application automatically sets the last days of the month), cyclically every few days, weeks or months, or once on a selected date. 
The whole is displayed in the form of an original calendar with color markings of a given event (which is selected at the stage of adding an event).


<!-- TABLE OF CONTENTS -->
## Table of Contents

* [About the Project](#about-the-project)
  * [Built With](#built-with)
* [Plan for the future](#plan-for-the-future)
* [Getting Started](#getting-started)
* [Installation](#installation)
* [Contributing](#contributing)
* [Contact](#contact)



<!-- ABOUT THE PROJECT -->
## About The Project

<p align="center">
  <a href="https://github.com/krzysztofrutana/OCR-Desktop">
    <img src="https://i.ibb.co/X5mdkfZ/today.png" width="400">
    <img src="https://i.ibb.co/tB2hQWH/jutro.png" width="400">
  </a>

The main window displays the author's calendar and a list of events for a specific day or by clicking on a date for a selected day. When you click on an event, detailed information about it is displayed. In the events occurring on the current day, it should be indicated whether they were done. Otherwise, notifications about events not done will be created in the future.
<br />
<br />
<br />
<br /><br />
<p align="center">
  <a href="https://github.com/krzysztofrutana/OCR-Desktop">
    <img src="https://i.ibb.co/t4sHHMp/Screenshot-1605893072.png" width="400">
  </a>


Navigation in the application is based on the slide-out menu on the left and the navigation buttons on the toolbar.
<br />
<br />
<br />
<br /><br />

<p align="center">
  <a href="https://github.com/krzysztofrutana/OCR-Desktop">
    <img src="https://i.ibb.co/3s2xVTj/w-konkretne-dni-miesiaca.png" width="400">
    <img src="https://i.ibb.co/r40mY1M/powtarzanie.png" width="400">
  </a>
  
 <p align="center">
  <a href="https://github.com/krzysztofrutana/OCR-Desktop">
    <img src="https://i.ibb.co/zRbC4RC/jednroazowe.png" width="400">
    <img src="https://i.ibb.co/JK3xWy6/przyklad.png" width="400">
  </a>


When adding a new event, it is required to enter its name and color (it is displayed in the calendar view). Description is optional. 
An event may be cyclical on a given day of the month for a given number of months (e.g. useful when paying bills). 
It can also be cyclical every few days / weeks / months (to be agreed) and apply all the time or repeat only a specified number of times 
(useful in the case of activities, e.g. performed every two days or e.g. taking medications). It is also possible to schedule a one-time event. 
Finally, the start date of the event is given. For each type of event, the time is also set, it can be default (changed in the settings) or selected by the user.

<br />
<br />
<br />
<br /><br />
 <p align="center">
  <a href="https://github.com/krzysztofrutana/OCR-Desktop">
    <img src="https://i.ibb.co/sjjTbRm/Screenshot-1603464356.png" width="400">
    <img src="https://i.ibb.co/Z8Dz1fS/szczego-y.png" width="400">
  </a>
  
The window on the left allows you to view all saved events. After clicking on the selected event, it is possible to view its details and delete the selected event. In detail window, you can also change the color of the event, time, move the next reminder to a different date or end the event (it will remain visible in the calendar view).

<br />
<br />
<br />
<br /><br />
<p align="center">
  <a href="https://github.com/krzysztofrutana/OCR-Desktop">
    <img src="https://i.ibb.co/5RqhQnD/Screenshot-1605892912.png" width="400">
    <img src="https://i.ibb.co/3fNRn31/Screenshot-1605892920.png" width="400">
  </a>
  
 <p align="center">
  <a href="https://github.com/krzysztofrutana/OCR-Desktop">
    <img src="https://i.ibb.co/VqvZ1rN/Screenshot-1605892925.png" width="400">
    <img src="https://i.ibb.co/YchkDKM/Screenshot-1605892963.png" width="400">
  </a>
  
  <p align="center">
  <a href="https://github.com/krzysztofrutana/OCR-Desktop">
    <img src="https://i.ibb.co/SK6GryB/Screenshot-1605891196.png" width="400">
    <img src="https://i.ibb.co/xfRgwwT/Screenshot-1605891682.png" width="400">
  </a>
  
  The settings allow you to change the default notification time and the time interval for checking events and creating new notifications. This tab allows you to create a local backup on your phone or Google Drive and restore it.
  <br />
<br />
<br />
<br /><br />
  <p align="center">
  <a href="https://github.com/krzysztofrutana/OCR-Desktop">
    <img src="https://i.ibb.co/tK8JRpf/Screenshot-1609519085.png" width="400">
    <img src="https://i.ibb.co/4Zm5sx1/Screenshot-1609518980.png" width="400">
  </a>
  <br />
  
  After starting, the application looks for unfinished days for all events. When some days are not mark as finished, application shows alert dialog with information about all finded days for all events with three options: to mark all days as finished, ignore information or remind me later.
  

<!--Built With -->
### Built With

* [Android Studio](https://developer.android.com/studio)
* [Java](https://www.java.com/pl/)
* [Joda DataTime Library](https://www.joda.org/)


<!--Plan for the future -->
## Plan for the future
* For now only testing and refactor code, add comments etc.

<!-- GETTING STARTED -->
## Getting Started

Add project to Android Studio. 

## Installation

For now APK file isn't availble. In future aplication will be in Play Store. 


<!-- ROADMAP -->
## Roadmap

See the [open issues](https://github.com/github_username/repo_name/issues) for a list of proposed features (and known issues).



<!-- CONTRIBUTING -->
## Contributing

Any contributions are welcome, you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request



<!-- LICENSE -->
<!--## License-->

<!-- CONTACT -->
## Contact

Krzysztof Rutana - krzysztofrutana@wp.pl

Project Link: [https://github.com/krzysztofrutana/Przypominajka](https://github.com/krzysztofrutana/Przypominajka)


<!-- ACKNOWLEDGEMENTS -->
<!-- ## Acknowledgements--> 
<!--* []() -->

