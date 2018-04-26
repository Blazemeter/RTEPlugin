FROM maven:3.5

# install tightvnc and execute-on-vnc script to be able to run UI unit tests with assertjswing
ENV USER="root" \
  PASSWORD="password"

# we update each time the apt-get to avoid potential issues with cached updates (instead of latest ones) when install is changed https://docs.docker.com/develop/develop-images/dockerfile_best-practices/#apt-get
# additionally we keep installs separated to keep related things together and properly identifiable with the root cause for the requirement (in a preluding comment)
RUN apt-get -y update \
  && apt-get -y install tightvncserver \
  && mkdir /root/.vnc \
  && echo $PASSWORD | vncpasswd -f > /root/.vnc/passwd \
  && chmod 0600 /root/.vnc/passwd

COPY execute-on-vnc.sh /usr/share/execute-on-vnc.sh

RUN ln -s /usr/share/execute-on-vnc.sh /usr/bin/execute-on-vnc

# install taurus for integration tests with blazemeter and different jmeter versions
RUN apt-get -y update \
  && apt-get -y install python default-jre-headless python-tk python-pip python-dev libxml2-dev libxslt-dev zlib1g-dev net-tools \
  && pip install bzt
