This project aims to build a bridge between [Git](http://git-scm.com)
and [StarTeam](http://www.borland.com/products/starteam/).

Current Goal
------------
Bring the capacity of converting StarTeam project to Git with the help
of the fast-import capacity. I'll be doing this with the help of a
fake layer that emulate the behavior of the StarTeam SDK. This layer
could be used as a test platform.

Future Goal
-----------
Give the capacity of making git a StarTeam compatible client with the
added value of using Git. This could also be used for Mercurial and
Bazaar with the help of the fast-export capacity.

License
-------
Git-StarTeam is convered under the General Public License Version 3.
Fake-StarTeam is convered under a personal license and is included
only for testing purpose. The project shall not be used for any other
purpose than easly testing your interaction with a fake StarTeam server.

Building
--------
Git-StarTeam use an ant script to build its jar file.

Build with StarTeam API
-----------------------
    cd ~/proj/git-starteam/
    ant -Djars.jar=/path/to/jargs.jar -Dstarteam.jar=/path/to/starteam.jar jar

StarTeam SDK versions 8.0, 10.4 and 11.0 are known to work. Version 12.5 is
not compatible.

Build Fake-StarTeam
-------------------
Fake-StarTeam is build using an sconscript. You can use the latest version of scons at: http://www.scons.org/

Run `scons --jargs /path/to/jargs.jar bin/syncronizer.jar` in the root
directory where you have cloned the repository to build Git-StarTeam using
Fake-StarTeam rather than the StarTeam SDK.

Prepare before Run
------------------
In Linux, the default max "open files" is 1024 (`ulimit -n`). Maybe you will get `java.io.IOException: Too many open files` if your StarTeam project had checked in too many files in one time, then you should add `* - nofile 65536` line to `/etc/security/limits.conf` and re-login to shell.

In Windows, the default max "open files" is 16384.

Run with StarTeam API
-----------------------
1.  Create `bin/.git/` folder from a StarTeam project named `Prime` with `Prime` view:

        cd ~/proj/git-starteam/bin/
        java -cp .:/usr/share/java/jargs.jar:/path/to/starteam.jar:./syncronizer.jar:../lib/org.eclipse.jgit-0.12.1.jar org.sync.MainEntry -h 192.0.1.102 -P 49203 -p Prime -v Prime -H master -U UserName -d gmail.com -f Src/apps/vlc2android/ -c

    Or, it's a good idea to add `.gitignore` first, so you can do below instead:

        cd ~/proj/git-starteam/bin/
        echo "*.o" > .gitignore
        git add .gitignore
        git commit -m "Init with .gitignore"
        java -cp .:/usr/share/java/jargs.jar:/path/to/starteam.jar:./syncronizer.jar:../lib/org.eclipse.jgit-0.12.1.jar org.sync.MainEntry -h 192.0.1.102 -P 49203 -p Prime -v Prime -H master -U UserName -d gmail.com -f Src/apps/vlc2android/ -R

2.  Add other branch to `bin/.git/` folder from other view:

    In `gitk --all`, create `OtherView` branch base on the commit which time is nearest before `StarTeam->View->Properties->Type->Parent Configuration` of the Other View.

        java -cp .:/usr/share/java/jargs.jar:/path/to/starteam.jar:./syncronizer.jar:../lib/org.eclipse.jgit-0.12.1.jar:/usr/lib/jvm/java-1.6.0-sun/jre/lib/ org.sync.MainEntry -h 192.0.1.102 -P 49203 -p Prime -v "Other View" -H other-view -U UserName -d gmail.com -f Src/apps/vlc2android -R

3.  Get the repository:

        cd /pub/gittrees/
        git clone --bare ~/proj/git-starteam/bin/.git vlc2android.git

4. Delete bin/.git/ folder

        cd ~/proj/git-starteam/bin/
        rm .git/ -fr

TODO
----
* Finish basic support of file query in the Fake StarTeam API.
 - Support for folder creation *Done*
 - Support for file creation *Done*
 - Support for acurrate file status *Done*
 - Support for label creation *Not Started*
 - Support for file deletion *Done*
 - Support for folder moving *Done*
 - Support for file renaming *Done*
* Create the fast-export based on the StarTeam API.
 - Create basic object for fast-export stream. *Done*
 - Create CLI for importing from a Project / View / View Configuration (Time) / Folder.
  * Syncronization of the tip *Done*
  * Syncronization by time of commit *Done*
  * Syncronization by Folder *Done*
  * Syncronization by branched View *Done*
  * Syncronization by Label *In progress on label-support branch*
* Push to StarTeam
 - Post-receiver example
