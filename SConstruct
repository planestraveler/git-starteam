"""
/*****************************************************************************
    This file is part of Git-Starteam.

    Git-Starteam is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Git-Starteam is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Git-Starteam.  If not, see <http://www.gnu.org/licenses/>.
******************************************************************************/
"""

AddOption('--jargs', dest='jargs', type='string', nargs=1, action='store', metavar='DIR', help='jargs jar path')
AddOption('--starteam', dest='starteam', type='string', nargs=1, action='store', metavar='DIR', help='starteam sdk location (default to fake-starteam if not specified)')
AddOption('--javasdkhome', dest='javasdkhome', type='string', nargs=1, action='store', metavar='DIR', help='Java SE SDK Home')

JARGS = GetOption('jargs')
STARTEAM = GetOption('starteam')
JAVASDKHOME = GetOption('javasdkhome')

if not GetOption('clean'):
    if not GetOption('help'):
        if not JARGS:
            print "Need --jargs arguments"
            exit(1)

env = Environment(JAVACLASSPATH = [JARGS])
if JAVASDKHOME:
    env.Append(PATH = [JAVASDKHOME + "/bin"])

env.Append(JAVACFLAGS = ['-Xlint:unchecked'])
if not STARTEAM:
    fakeStarteamTarget = 'bin/fake-starteam.jar'
    fakeclasses = env.Java(target = 'fake-starteam/classes', source = 'fake-starteam/src')
    fakeclasses.append('syncronizer.mf')
    env.Jar(target = fakeStarteamTarget, source = fakeclasses)
    env.Append(JAVACLASSPATH = [fakeStarteamTarget])
else:
    env.Append(JAVACLASSPATH = [STARTEAM])

syncclasses = env.Java(target = 'syncronizer/classes', source = 'syncronizer/src')
env.Jar(target = 'bin/syncronizer.jar', source = syncclasses)

