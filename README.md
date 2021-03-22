Git-StarTeam imports [StarTeam](http://www.borland.com/products/starteam/)
projects into [Git](http://git-scm.com).

![Build Status](https://travis-ci.org/planestraveler/git-starteam.svg?branch=master)

Supported Features
------------------
* Day-by-day import
* Label-by-label import
* Resume imports
* [Keyword expansion](http://documentation.microfocus.com/help/index.jsp?topic=%2Fcom.borland.stcpc.doc%2FSTARTEAM-2F047CA6-TABLEKEYWORDS-REF.html)
* Pipe data to `git fast-import` or dump to files

Limitations
-----------
* Merge history is not imported
 - Use [git-starteam-merges](https://github.com/patrick-higgins/git-starteam-merges) to reconstruct history after import

Build
-----
Git-StarTeam use an ant script to build its jar file.

    cd ~/proj/git-starteam/
    ant -Djargs.jar=/path/to/jargs.jar -Dstarteam.jar=/path/to/starteam.jar jar

StarTeam SDK versions 8.0, 10.4 and 11.0 are known to work. Version 12.5 is
not compatible.

Tune OS
-------
A StarTeam project where a large number of files were checked in at one time
might exceed maximum open file limits on your operating system.

On Linux, this can be changed with `ulimit -n 16384`. If this fails, it may
help to add the line `* - nofile 65536` to `/etc/security/limits.conf` and
then logout and back in. The `*` may be replaced with a specific username.

The default max "open files" is 16384 on Windows and likely won't need tuning.

Command Line Usage
------------------
The main Java class is `org.sync.MainEntry`. The following jars must be in your Java classpath:

* `jargs.jar`
* `starteam*.jar`
* `syncronizer.jar`

Running the command with no options will display usage information.

### Required Options
These options must be set for all runs:

* `-h <host>` StarTeam server hostname
* `-P <port>` StarTeam server port
* `-p <project>` StarTeam project
* `-v <view>` StarTeam view

### Import Modes

There are four primary modes of operation:

1.  Import latest revisions
2.  Import label by label
3.  Import day by day
4.  Import all labels

All four modes can:

* Restrict the import to a specific folder using `-f <folder>` option.
* Dump `git fast-import` data to files for debugging using `-D <dump file prefix>` option.
* Log their operations to stderr with the `--verbose` option.
* Specify the path to the Git executable with the `-X <path to git>` option.
* Specify the Git repository location with the `-W <folder>` option.
* Automatically create the Git repository (as a bare repo) with the `-c` option.
* Expand StarTeam keywords with the `-k` option.
* Select the StarTeam user and password with the `-U <user>` and `--password` options.
* Specify a file containing a mapping of user names to emails with `-m <mailMappingFile>`.
  This mapping will be used in case the user performing the conversion does not have the
  required administrator access to extract email addresses from the StarTeam server.
* Specify the domain to append to StarTeam user names with `-d <domain>`. This mapping will
  be used in case the user performing the conversion does not have the
  required administrator access to extract email addresses from the StarTeam server, and
  an explicit mapping cannot be found in the mail mapping file if one is provided 
  with the `-m <mailMappingFile>` command line option.
* Skip files with specific extensions using the `-x <extensions>` option.

The first three modes can:

* Add new commits to an existing repository using `-R` (resume) option.
* Specify the Git branch name with the `-H <head>` option. Uses the view name by default.

#### Latest Revisions Mode

Latest revisions mode is the default.

This mode imports the latest revision of each file accessible from the
view.

Equivalent revisions will grouped into a single Git commit. Two
revisions are considered equivalent when:

1.  The revisions were created by the same user.
2.  Either of the revisions has an empty comment or they have the same
    comment, ignoring case.
3.  No revision was created between them in time.

For example:

    file1.c: user1  3:50pm  Message 1
    file2.c: user1  3:51pm  <empty log message>
    file3.c: user2  3:52pm  <empty log message>
    file4.c: user1  3:53pm  <empty log message>

file1.c and file2.c will be grouped into a git commit, file3.c will be
in another commit by itself, and file4.c will be in another commit by
itself. If file3.c had not been checked in between file2.c and
file4.c, then file1.c, file2.c, and file4.c would have all been
grouped into a single commit.

When the `-R` option is used, all commits will be made on top of the
branch specified by the `-H <head>` option.

The `-t` option is not valid in latest revisions mode.

#### Label-by-Label Mode

The `-L` option enables label-by-label mode.

This mode imports the file revisions of each successive label. The
file revisions will be grouped into commits as in latest revisions
mode.

When the `-R` option is used, only labels created after the date of
the last commit on the branch specified by the `-H <head>` option will
be imported. If the `-t <time>` option is also used with `-R`, then
only labels created after the given time will be imported.

A lightweight Git tag will be created for each StarTeam label.

#### Day-by-Day Mode

The `-T` option enables day-by-day mode.

This mode imports the file revisions at the end (23:59:59) of each
successive day. The file revisions will be grouped into commits as in
latest revisions mode.

When the `-R` option is used, the first day imported will be the date
of the last commit on the branch specified by the `-H <head>` option.

The `-t` option can be used to specify the starting time directly.

#### All-Views Mode

The `-A` option enables all-views mode.

All-views mode does a label-by-label import of every derived view from the
root view. The root view may be passed in with the `-v <view>` option or
will be automatically found by traversing up the view tree from the default
project view if no `-v <view>` option is given.

The `--skip-views <regex>` option may be given to prevent specific views
from being imported. All of the views that will be imported and skipped will
be logged at the beginning of the import run so that you may kill the import
and make changes if the list is not what you intend.

All-views is intended for one-time import of old project history. After the
initial all-views import, one of the other modes may be used with the `-R`
option to keep the git branches in sync with StarTeam.

Examples
--------

### All-Views Mode Script

        #!/bin/sh
        
        # kill existing run, cleanup it's temp files
        pkill -9 -U $LOGNAME -f org.sync.MainEntry
        sleep 1
        rm -f /tmp/Star*.tmp
        
        REPO=/pub/gittrees/vlc2android.git
        
        rm -rf $REPO
        git init --bare $REPO
        
        (
            setsid java \
                -classpath $(echo *.jar | tr ' ' :) \
                -Xms2000M \
                -Xmx2000M \
                org.sync.MainEntry \
              -h 192.0.1.102 -P 49203 \
              -p Prime -A --skip-views '^xToDelete' \
              -d gmail.com \
              -f Src/apps/vlc2android \
              -U UserName \
              -W $REPO \
              --verbose &
        ) >vlc2android-all.log 2>&1 </dev/null

### Add mapping of user names to actual email address

In case the user performing the conversion does not have StarTeam user account admin access,
the emails can be specified using a mapping file as follow:

1.  Create mapping file:

          echo "John Smith = jsmith@acme.com"               >  mailmap
          echo "J. Random Hacker = jrhacker@hackersoft.com" >> mailmap
          ...
          
2.  Execute importation with additional `-m <mailmap>` command line option. If the `-d <domain>` is
    also provided, users which are not found in `mailmap` will get an email using the default domain provided. 

### Latest Revisions Mode

1.  Create `bin/.git/` folder from a StarTeam project named `Prime` with `Prime` view:

        cd ~/proj/git-starteam/bin/
        java -cp jargs.jar:starteam110.jar:syncronizer.jar org.sync.MainEntry -h 192.0.1.102 -P 49203 -p Prime -v Prime -H master -U UserName -d gmail.com -f Src/apps/vlc2android/ -c

    Or, it's a good idea to add `.gitignore` first, so you can do below instead:

        cd ~/proj/git-starteam/bin/
        echo "*.o" > .gitignore
        git add .gitignore
        git commit -m "Init with .gitignore"
        java -cp jargs.jar:starteam110.jar:syncronizer.jar org.sync.MainEntry -h 192.0.1.102 -P 49203 -p Prime -v Prime -H master -U UserName -d gmail.com -f Src/apps/vlc2android/ -R

2.  Add other branch to `bin/.git/` folder from other view:

    In `gitk --all`, create `OtherView` branch base on the commit which time is nearest before `StarTeam->View->Properties->Type->Parent Configuration` of the Other View.

        java -cp jargs.jar:starteam110.jar:syncronizer.jar org.sync.MainEntry -h 192.0.1.102 -P 49203 -p Prime -v OtherView -H other-view -U UserName -d gmail.com -f Src/apps/vlc2android -R

3.  Get the repository:

        cd /pub/gittrees/
        git clone --bare ~/proj/git-starteam/bin/.git vlc2android.git

4.  Delete bin/.git/ folder

        cd ~/proj/git-starteam/bin/
        rm -rf .git/

Build with Fake-StarTeam
------------------------
Fake-StarTeam may be used to test Git-StarTeam itself. To build it, run ant
as follows:

    cd ~/proj/git-starteam/
    ant -Djargs.jar=/path/to/jargs.jar -Dstarteam.jar=bin/fake-starteam.jar fake-starteam jar

Run Unit Tests
--------------
The unit tests currently assume they will be run from the syncronizer subdirectory and in the UTC-0500 timezone.

    cd ~/proj/git-starteam/syncronizer
    TZ=America/Montreal ant -f ../build.xml -Dstarteam.bin=bin/fake-starteam clean fake-starteam test

TODO
----
* Push to StarTeam

License
-------
Git-StarTeam is convered under the GNU General Public License Version 3.

Fake-StarTeam is convered under a personal license and is included
only for testing purpose. The project shall not be used for any other
purpose than easly testing your interaction with a fake StarTeam server.

Similar Projects
----------------
[git-st](https://code.google.com/p/git-st/) is a Git remote helper for StarTeam.
