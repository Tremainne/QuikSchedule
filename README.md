# QuikSchedule

## Testing
Unit tests located at: QuikSchedule/app/src/test/java/group22.quikschedule

Scenario Tests located at: Quikschedule/BootstrapApplicationTest/src/androidTest/java/group22.quikschedule.test

(NOTE: Please ensure that the emulator is unlocked when running these tests to ensure that testing can be completed properly)

(NOTE: testLoadingMyLocationMaps, testNavigationDrawer, and testSignInToGoogle are for external test use.  Other scenario tests require credentials/login-specific information for internal use.)

## Proposal
We feel that there is an obvious gap that we need to bridge, between knowing one’s classes and scheduling transportation in order to be on time for said classes. We propose an app that combines the UCSD TritonLink functions with the Google Maps API. By combining the schedule from TritonLink and the directions, travel times, and bus routing of Google Maps, we should be able to optimize a schedule for people to make their classes on time. The app will use automatic GPS polling to keep track of the user’s location and update travel times as necessary throughout the day.  Not only will the product be able to aid you in being on time, but it will also provide alarms for classes, check for the shortest travel method (which could potentially include walking or biking), synchronize with your booklist and even remind you of what to bring to school (scantrons, blue books, notebooks, etc.). Future features could include knowing if your friends are in your class as well as being able to contact your peers to let them know if you’re running late or missing a scantron/blue book with automated responses.  For example, if you miss your alarm on the day of a quiz, when you wake up the app would remind you that taking the 202 bus is the fastest way to campus and that you need a scantron and a Number 2 pencil to bring to school.  If you didn’t have a scantron, with the click of a button, you could make sure your friend who lives on campus brings you an extra.

The app could also be further developed to provide knowledge of parking times on campus, which can be a detriment to any person who is trying to make it to class on time and drives a car.  Further utilizing Google’s APIs the app could also be able to synchronize your Tritonlink with your Google calendar to ensure that you are on time for everything that you have to do!  
