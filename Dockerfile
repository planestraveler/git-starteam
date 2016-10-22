# Base docker image to run a preconfigured git-starteam software and simplify the
# setup and execution. The alpine linux x86 version is used because most of the
# time the SDK provided by Borland is for i686 processor.
#
# Missing, Starteam SDK runtime must be installed into the image and the CLASSPATH
# be set acordingly.

FROM multiarch/alpine:x86-latest-stable

MAINTAINER s.tousignant@gmail.com

RUN echo "http://nl.alpinelinux.org/alpine/latest-stable/community" >> /etc/apk/repositories

RUN apk add --no-cache openjdk8 && java -version
RUN apk add --no-cache git && git --version

COPY ["bin/syncronizer.jar", "lib/jargs.jar", "/opt/git-starteam/"]

RUN ls /opt/git-starteam

ENV CLASSPATH=/opt/git-starteam/syncronizer.jar:/opt/git-starteam/jargs.jar

ENTRYPOINT ["java", "org.sync.MainEntry"]
CMD ["--help"]

